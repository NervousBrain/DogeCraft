package net.waals.dogecraft.wallet;

import com.dogecoin.protocols.payments.Protos;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import net.waals.dogecraft.managers.DogePlayerManager;
import net.waals.dogecraft.managers.EconomyManager;
import net.waals.dogecraft.models.DogePlayer;
import org.bitcoinj.core.*;
import org.bitcoinj.crypto.KeyCrypter;
import com.google.common.util.concurrent.Service.State;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;


import javax.annotation.Nullable;

import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.wallet.SendRequest;
import org.bitcoinj.wallet.Wallet;
import org.bitcoinj.wallet.listeners.AbstractWalletEventListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.spongycastle.crypto.params.KeyParameter;


import org.bitcoinj.core.listeners.AbstractPeerEventListener;
import org.bitcoinj.utils.Threading;
import org.bitcoinj.wallet.WalletTransaction;
import org.bitcoinj.crypto.KeyCrypterException;

import org.libdohj.params.DogecoinMainNetParams;

import static com.google.api.client.util.Preconditions.checkNotNull;

public class CoreWallet {

    private CoreWalletAppKit appKit = null;
    private int synchronizing = 0;
    private boolean dirty = false;
    private String walletFilePath;
    private DogePlayerManager dogePlayerManager;
    private EconomyManager economyManager;
    private ArrayList<String> withdraws;


    public CoreWallet(DogePlayerManager dogePlayerManager, EconomyManager economyManager) {
        this.dogePlayerManager = dogePlayerManager;
        this.economyManager = economyManager;
        this.withdraws = new ArrayList<String>();
    }

    public void run() throws Exception {
        if (getWalletFilePath() == null)
            run(new File("./coins"), "wallet.dat");
        else {
            File f = new File(getWalletFilePath());
            run(f.getParentFile(), f.getName());
        }
    }

    public void run(final File directory, final String fileName) throws Exception {
        synchronizing = 0;
        NetworkParameters params = DogecoinMainNetParams.get();

        walletFilePath = new File(directory, fileName).getAbsolutePath();
        String spvFilePath = "./coins/chain.spvchain";
        System.out.println("SPVFilePath: " + spvFilePath);
        boolean exists = new File(walletFilePath).exists();

        // If wallet files does not exist, try to create wallet file to make sure it is in writable location. Before doing anything else.
        if (!exists) {
            File temp = new File(directory, fileName);
            Wallet vWallet = new Wallet(params);
            vWallet.saveToFile(temp);
        }

        appKit = new CoreWalletAppKit(params, directory, fileName, spvFilePath) {

//			protected void addWalletExtensions() throws Exception {
//				File temp = new File(vWalletFile.getParentFile(), "TEMP" + vWalletFile.getName());
//				vWallet.saveToFile(temp, vWalletFile);
//			}

            @Override
            protected void onSetupCompleted() {
                if (wallet().getImportedKeys().size() < 1) {
                }
//				if (wallet().getKeychainSize() < 1) {
//					ECKey key = new ECKey();
//					wallet().addKey(key);
//				}

                setWalletFilePath(walletFilePath);

                peerGroup().setConnectTimeoutMillis(1000);
                //peerGroup().setFastCatchupTimeSecs(0);//wallet().getEarliestKeyCreationTime());

                System.out.println(appKit.wallet());

                peerGroup().addEventListener(new AbstractPeerEventListener() {
                    @Override
                    public void onPeerConnected(Peer peer, int peerCount) {
                        super.onPeerConnected(peer, peerCount);
                        System.out.println(String.format("onPeerConnected: %s %s",peer,peerCount));
                    }
                    @Override
                    public void onPeerDisconnected(Peer peer, int peerCount) {
                        super.onPeerDisconnected(peer, peerCount);
                        System.out.println(String.format("onPeerDisconnected: %s %s",peer,peerCount));
                    }
                    @Override public void onBlocksDownloaded(Peer peer, Block block, @Nullable FilteredBlock filteredBlock, int blocksLeft) {
                        super.onBlocksDownloaded(peer, block, filteredBlock, blocksLeft);
                        synchronizing = blocksLeft;
                    }
//					@Override public void onBlocksDownloaded(Peer peer, Block block, int blocksLeft) {
//						super.onBlocksDownloaded(peer, block, blocksLeft);
//						synchronizing = blocksLeft;
//						System.out.println(String.format("%s blocks left (downloaded %s)",blocksLeft,block.getHashAsString()));
//					}

                    @Override public Message onPreMessageReceived(Peer peer, Message m) {
                        System.out.println(String.format("%s -> %s",peer,m.getClass()));
                        return super.onPreMessageReceived(peer, m);
                    }
                },Threading.SAME_THREAD);

                wallet().addEventListener(new AbstractWalletEventListener() {
                    @Override
                    public void onWalletChanged(Wallet wallet) {

                    }

                    @Override
                    public void onCoinsReceived(Wallet wallet, Transaction tx, Coin prevBalance, Coin newBalance) {
                        for(TransactionOutput output : tx.getOutputs()) {
                            String address = output.getAddressFromP2PKHScript(params).toString();
                            DogePlayer dogePlayer = null;
                            try {
                                dogePlayer = dogePlayerManager.getPlayerByAddress(address);
                            } catch (ExecutionException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(dogePlayer != null && !withdraws.contains(tx.getHashAsString())) {
                                DogePlayer finalDogePlayer = dogePlayer;
                                if(Bukkit.getServer().getPlayer(UUID.fromString(finalDogePlayer.getUuid())) != null && Bukkit.getServer().getPlayer(UUID.fromString(finalDogePlayer.getUuid())).isOnline()) {
                                    Bukkit.getServer().getPlayer(UUID.fromString(finalDogePlayer.getUuid())).sendMessage(ChatColor.GOLD + ">> Received transaction https://dogechain.info/tx/" + tx.getHashAsString());
                                    Bukkit.getServer().getPlayer(UUID.fromString(finalDogePlayer.getUuid())).sendMessage(ChatColor.GOLD + ">> DOGE will be deposited to your account after 3 confirmations.");
                                }
                                Futures.addCallback(tx.getConfidence().getDepthFuture(3), new FutureCallback<TransactionConfidence>() {
                                    @Override
                                    public void onSuccess(TransactionConfidence result) {

                                        Coin coin = output.getValue();
                                        Long longAmount = coin.divide(100000000).value;
                                        double amount = longAmount.doubleValue();
                                        economyManager.deposit(finalDogePlayer, amount);
                                        dogePlayerManager.storePlayer(finalDogePlayer);
                                        if(Bukkit.getServer().getPlayer(UUID.fromString(finalDogePlayer.getUuid())) != null && Bukkit.getServer().getPlayer(UUID.fromString(finalDogePlayer.getUuid())).isOnline()) {
                                            Bukkit.getServer().getPlayer(UUID.fromString(finalDogePlayer.getUuid())).sendMessage(ChatColor.GOLD + ">> " + amount + " DOGE have been deposited to your account.");
                                        }
                                    }

                                    @Override
                                    public void onFailure(Throwable t) {
                                        // This kind of future can't fail, just rethrow in case something weird happens.
                                        throw new RuntimeException(t);
                                    }
                                });
                            }
                            if(withdraws.contains(tx.getHashAsString())) {
                                withdraws.remove(tx.getHashAsString());
                            }
                        }
                    }
//		            @Override
//		            public void onCoinsReceived(Wallet wallet, Transaction tx, java.math.BigInteger prevBalance, java.math.BigInteger newBalance) {
//		            	playSoundFile("/org/wowdoge/coins-drop-1.wav");
//		            }

                    @Override
                    public void onKeysAdded(List<ECKey> keys) {
                        dirty = true;
                    }
//		            @Override
//		            public void onKeysAdded(Wallet wallet, java.util.List<ECKey> keys) {
//		            	dirty = true;
//		            }
                });
                dirty = true;
            }
        };

        if (!exists)
            appKit.setCheckpoints(CoreWallet.class.getResourceAsStream("./coins/checkpoints"));

        //appKit.saveWallet();

        appKit.setBlockingStartup(false);
        //appKit.startAndWait(); //startAndWait();

        // Now we start the kit and sync the blockchain.
        // bitcoinj is working a lot with the Google Guava libraries. The WalletAppKit extends the AbstractIdleService. Have a look at the introduction to Guava services: https://code.google.com/p/guava-libraries/wiki/ServiceExplained
        appKit.startAsync();
        appKit.awaitRunning();
    }

    public String getWalletFilePath() {
        walletFilePath = "./coins/wallet.dat";
        return walletFilePath;
    }

    public void setWalletFilePath(String path) {
        walletFilePath = path;
    }

    public String getSPVFilePath(String path) {
        path = "./coins/chain.spvchain";
        return path;
    }

    public void stop() {
        appKit.stopAsync();
        appKit.awaitTerminated();
        //appKit.stopAndWait();
    }

//	public void shutDown() {
//		try {
//			appKit.stopAndWait();
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//		try {
//			appKit.store().close();
//		} catch (Exception e) {
//		}
//		appKit.reset();
//	}

    public void open(File f) throws Exception {
        stop();
        run(f.getParentFile(), f.getName());
    }

    public boolean isDirty() {
        if (appKit == null)
            return false;
        if (appKit.state() == State.STOPPING || appKit.state() == State.TERMINATED)
            return false;
        else
            return dirty;
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public NetworkParameters getNetworkParameters() {
        return appKit.wallet().getNetworkParameters();
    }

    public Wallet getWallet() {
        return appKit.wallet();
    }

    public boolean isEncrypted() {
        return appKit.wallet().isEncrypted();
    }

    public void encrypt(CharSequence password) throws KeyCrypterException {
        appKit.wallet().encrypt(password);
    }

    public void decrypt(CharSequence password) throws KeyCrypterException {
        appKit.wallet().decrypt(appKit.wallet().getKeyCrypter().deriveKey(password));
    }

    public void createNewKeys(int number) throws KeyCrypterException {
        createNewKeys(number, null);
    }

    //System.out.println("keyProto.getPrivateKey().toByteArray() :" + keyProto.getPrivateKey());
    //System.out.println("keyProto.getEncryptedPrivateKey(): " + keyProto.getEncryptedPrivateKey());
    public void createNewKeys(int number, final CharSequence walletPassword) throws KeyCrypterException {
        List<ECKey> keys = new ArrayList<ECKey>();
        for (int i = 0; i < number; i++) {
            ECKey key = new ECKey();
            if (isEncrypted()) {
                final KeyCrypter walletKeyCrypter = getWallet().getKeyCrypter();
                KeyParameter aesKey = walletKeyCrypter.deriveKey(walletPassword);
                key = key.encrypt(walletKeyCrypter, aesKey);
            }
            keys.add(key);
        }
        appKit.wallet().addKeys(keys);
    }

    public java.util.List<ECKey> getKeys() {
        return appKit.wallet().getImportedKeys(); //appKit.wallet().getKeys();
    }

//	public java.util.List<ECKey> getKeys(CharSequence password) throws KeyCrypterException {
//		if (password != null)
//			decrypt(password);
//		List<ECKey> keys = appKit.wallet().getKeys();
//		if (password != null)
//			encrypt(password);
//		return keys;
//	}


    public void sync() throws Exception { //Date from
//		System.out.println("About to restart PeerGroup.");   
//		appKit.peerGroup().stopAndWait();
//		System.out.println("PeerGroup is now stopped.");
//		// Close the blockstore and recreate a new one.
//		appKit.store()
        //synchronizing = 0;

        stop();
        File f = new File(getWalletFilePath());
        String spvFilePath = "./coins/chain.spvchain"; //"dogecoins.dogespvchain"
        new File(spvFilePath).delete();
        run();
    }

    public Coin getBalance() {
        return appKit.wallet().getBalance();
    }

    public java.lang.Iterable<WalletTransaction> getWalletTransactions() {
        return appKit.wallet().getWalletTransactions();
    }

    public List<Transaction> getTransactionsByTime() {
        return appKit.wallet().getTransactionsByTime();
    }

    public Coin getBalance(String address) { //ECKey key) {
        Coin balance = Coin.ZERO; //BigInteger.ZERO;
        List<Transaction> transactions = getTransactionsByTime();
        for (Transaction t: transactions) {
            Coin value = t.getValue(appKit.wallet());
            if (value.isNegative()) { //value.compareTo(BigInteger.ZERO) < 0) {
                List<TransactionInput> ins = t.getInputs();
                for (TransactionInput i : ins) {
                    try {
                        TransactionOutput output = i.getOutpoint().getConnectedOutput();
                        if (i.getFromAddress().toString().equals(address)) {
                            balance = balance.subtract(output.getValue());
                        }
                    } catch (ScriptException e) {
                        e.printStackTrace();
                        //tempText = tempText + "COINBASE" + " ";
                    }
                }
                List<TransactionOutput> outs = t.getOutputs();
                for (TransactionOutput o : outs) {
                    if  (o.getScriptPubKey().getToAddress(o.getParams()).toString().equals(address)) {
                        balance = balance.add(o.getValue());
                    }
                }
            } else {
                List<TransactionOutput> outs = t.getOutputs();
                for (TransactionOutput o : outs) {
                    if  (o.getScriptPubKey().getToAddress(o.getParams()).toString().equals(address)) {
                        balance = balance.add(o.getValue());
                    }
                }
            }
        }
        return balance;
    }

    public Wallet.SendResult sendCoins(Address targetAddress, Coin amountToSend) throws InsufficientMoneyException {
        //BigInteger value = Utils.toNanoCoins(new Float(amount).toString());
        // Send with a small fee attached to ensure rapid confirmation.
        //final BigInteger amountToSend = value; //value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
        //final Wallet.SendResult sendResult = appKit.wallet().sendCoins(appKit.peerGroup(), address, amountToSend);
        SendRequest request = SendRequest.to(targetAddress, amountToSend);
        withdraws.add(request.tx.getHashAsString());
        request.feePerKb = Coin.valueOf(100000000);
        request.changeAddress = Address.fromBase58(appKit.params(), "DJRXKqrHzhD94nKtbnADF6X1JaGaRWbfa3");

        final Wallet.SendResult sendResult = appKit.wallet().sendCoins(appKit.peerGroup(), request);

        return sendResult;
    }


    public Wallet.SendResult sendCoins(Address address, Coin amountToSend, Address changeAddress, CharSequence password) throws InsufficientMoneyException {
        //BigInteger value = Utils.toNanoCoins(new Float(amount).toString());
        // Send with a small fee attached to ensure rapid confirmation.
        //final BigInteger amountToSend = value; //value.subtract(Transaction.REFERENCE_DEFAULT_MIN_TX_FEE);
        // Make sure this code is run in a single thread at once.
        SendRequest request = SendRequest.to(address, amountToSend);
        request.changeAddress = changeAddress;
        // The SendRequest object can be customized at this point to modify how the transaction will be created.
        request.aesKey = appKit.wallet().getKeyCrypter().deriveKey(password);
        final Wallet.SendResult sendResult = appKit.wallet().sendCoins(appKit.peerGroup(), request);
        return sendResult;
    }

    public Wallet.SendResult sendAll(Address address, Address changeAddress) throws InsufficientMoneyException {
        SendRequest request = SendRequest.emptyWallet(address);
        request.changeAddress = changeAddress;
        // The SendRequest object can be customized at this point to modify how the transaction will be created.
        final Wallet.SendResult sendResult = appKit.wallet().sendCoins(appKit.peerGroup(), request);
        return sendResult;
    }

    public Wallet.SendResult sendAll(Address address, CharSequence password, Address changeAddress) throws InsufficientMoneyException {
        SendRequest request = SendRequest.emptyWallet(address);
        request.changeAddress = changeAddress;
        // The SendRequest object can be customized at this point to modify how the transaction will be created.
        request.aesKey = appKit.wallet().getKeyCrypter().deriveKey(password);
        final Wallet.SendResult sendResult = appKit.wallet().sendCoins(appKit.peerGroup(), request);
        return sendResult;
    }

    public boolean checkPassword(CharSequence password) {
        return appKit.wallet().checkAESKey(appKit.wallet().getKeyCrypter().deriveKey(password));
    }

    public void saveToFile(java.io.File temp, java.io.File destFile) throws java.io.IOException {
        appKit.wallet().saveToFile(temp, destFile);
    }

    public final State state() {
        if (appKit != null)
            return appKit.state();
        else
            return State.NEW;
    }

    public final boolean isRunning() {
        if (appKit != null)
            return appKit.isRunning();
        else
            return false;
    }

    public final int isSynchronizing() {
        return synchronizing;
    }

    public final boolean isStoreFileLocked() {
        try {
            if (appKit != null)
                return appKit.isChainFileLocked();
            else
                return false;
        } catch (IOException e) {
            e.printStackTrace();
            return true;
        }
//		if (appKit != null)
//			return appKit.isStoreFileLocked();
//		else
//			return false;
    }
}