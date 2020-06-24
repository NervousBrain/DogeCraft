package net.waals.dogecraft.util;

import com.gmail.filoghost.holographicdisplays.api.Hologram;
import com.gmail.filoghost.holographicdisplays.api.HologramsAPI;
import net.waals.dogecraft.DogeCraft;
import net.waals.dogecraft.models.Shop;
import net.waals.dogecraft.models.ShopItem;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionType;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;

public class ShopUtil {

    private DogeCraft plugin;
    private HashMap<Shop, String> stacks;
    private HashMap<Shop, ArrayList<String>> stands;
    private ArrayList<Shop> toSpawn;
    private AreaUtil areaUtil;

    public ShopUtil(DogeCraft plugin, AreaUtil areaUtil) {
        this.plugin = plugin;
        this.areaUtil = areaUtil;
        stacks = new HashMap<>();
        this.toSpawn = new ArrayList<>();
        stands = new HashMap<>();
    }

    public void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, new Runnable() {
            @Override
            public void run() {
                for(Shop shop : toSpawn) {
                    spawnShopCosmetics(shop);
                }
                toSpawn.clear();
            }
        }, 0L, 2*20L);
    }

    public ShopItem itemStackToShopItem(ItemStack itemStack) {
        int amount = itemStack.getAmount();
        String material = itemStack.getType().toString();
        HashMap<String, Integer> enchantments = new HashMap<>();
        ShopItem item = null;
        for(Enchantment currentEnchantment : itemStack.getEnchantments().keySet()) {
            enchantments.put(currentEnchantment.getKey().getKey(), itemStack.getEnchantmentLevel(currentEnchantment));
        }
        if(itemStack.getType() == Material.POTION || itemStack.getType() == Material.LINGERING_POTION || itemStack.getType() == Material.SPLASH_POTION) {
            PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
            PotionData potionData = potionMeta.getBasePotionData();
            item = new ShopItem(enchantments, amount, material, potionData.getType().toString(), potionData.isExtended(), potionData.isUpgraded());
        } else {
            item = new ShopItem(enchantments, amount, material, null, false, false);
        }

        return item;
    }

    public ItemStack buildItemStack(ShopItem shopItem) {
        ItemStack itemStack = new ItemStack(Material.valueOf(shopItem.getMaterial()));
        itemStack.setAmount(shopItem.getAmount());
        if(itemStack.getType() == Material.POTION || itemStack.getType() == Material.LINGERING_POTION || itemStack.getType() == Material.SPLASH_POTION) {
            PotionMeta potion = (PotionMeta) itemStack.getItemMeta();
            PotionData potionData = new PotionData(PotionType.valueOf(shopItem.getPotionType()), shopItem.isPotionExtended(), shopItem.isPotionUpgraded());
            potion.setBasePotionData(potionData);
            itemStack.setItemMeta(potion);
        }
        for(String currentEnchantment : shopItem.getEnchantments().keySet()) {
            itemStack.addUnsafeEnchantment(Enchantment.getByKey(NamespacedKey.minecraft(currentEnchantment.replaceAll("minecraft:", ""))), shopItem.getEnchantments().get(currentEnchantment));
        }
        return itemStack;
    }

    public void spawnShopCosmetics(Shop shop) {

        Hologram holo = HologramsAPI.createHologram(plugin, areaUtil.pointToLocation(shop.getLocation()).add(new Vector(0.5, 2.3, 0.5)));
        holo.appendTextLine(ChatColor.GOLD + "" + ChatColor.BOLD + shop.getItem().getMaterial() + " (" + shop.getItem().getAmount() + "x)");
        for(String enchantment : shop.getItem().getEnchantments().keySet()) {
            holo.appendTextLine(ChatColor.GREEN + enchantment.replaceAll("minecraft:", "") + " " + shop.getItem().getEnchantments().get(enchantment));
        }
        if(shop.getItem().getPotionType() != null) {
            holo.appendTextLine(ChatColor.GREEN + shop.getItem().getPotionType());
        }
        if(shop.getBuyPrice() == 0) {
            holo.appendTextLine(ChatColor.GOLD + "Buy: " + shop.getSellPrice() + " Đ");
        } else if(shop.getSellPrice() == 0) {
            holo.appendTextLine(ChatColor.GOLD + "Sell: " + shop.getBuyPrice() + " Đ");
        } else {
            holo.appendTextLine( ChatColor.GOLD + "Sell: " + shop.getBuyPrice() + " Đ | " + "Buy: " + shop.getSellPrice() + " Đ");
        }

        holo.appendItemLine(buildItemStack(shop.getItem()));
        holo.getVisibilityManager().setVisibleByDefault(true);
        /*ItemStack itemStack = buildItemStack(shop.getItem());
        Location loc = new Location(plugin.getServer().getWorld("world"), shop.getLocation().getX()+0.5, shop.getLocation().getY()+1, shop.getLocation().getZ()+0.5);
        Item item = plugin.getServer().getWorld("world").dropItem(loc, itemStack);
        item.setMetadata("toKill", new FixedMetadataValue(plugin, true));
        item.setVelocity(new Vector(0,0,0));
        item.setInvulnerable(true);
        stacks.put(shop, item.getUniqueId().toString());
        ArrayList<String> shopStands = new ArrayList<>();
        ArmorStand price = (ArmorStand) plugin.getServer().getWorld("world").spawnEntity(loc.add(new Vector(0, -1.75, 0)), EntityType.ARMOR_STAND);
        if(shop.getBuyPrice() == 0) {
            price.setCustomName(ChatColor.GOLD + "Buy: " + shop.getSellPrice() + " Đ");
        } else if(shop.getSellPrice() == 0) {
            price.setCustomName(ChatColor.GOLD + "Sell: " + shop.getBuyPrice() + " Đ");
        } else {
            price.setCustomName( ChatColor.GOLD + "Sell: " + shop.getBuyPrice() + " Đ | " + "Buy: " + shop.getSellPrice() + " Đ");
        }
        price.setGravity(false);
        price.setCanPickupItems(false);
        price.setCustomNameVisible(true);
        price.setVisible(false);
        for(String enchantment : shop.getItem().getEnchantments().keySet()) {
            ArmorStand enchant = (ArmorStand) plugin.getServer().getWorld("world").spawnEntity(loc.add(new Vector(0, 0.25, 0)), EntityType.ARMOR_STAND);
            enchant.setGravity(false);
            enchant.setCanPickupItems(false);
            enchant.setCustomName(ChatColor.GRAY + enchantment.replaceAll("minecraft:", "") + " " + shop.getItem().getEnchantments().get(enchantment));
            enchant.setCustomNameVisible(true);
            enchant.setVisible(false);
            shopStands.add(enchant.getUniqueId().toString());
            enchant.setMetadata("toKill", new FixedMetadataValue(plugin, true));
        }
        ArmorStand name = (ArmorStand) plugin.getServer().getWorld("world").spawnEntity(loc.add(new Vector(0, 0.25, 0)), EntityType.ARMOR_STAND);
        name.setGravity(false);
        name.setCanPickupItems(false);
        name.setCustomName(ChatColor.GOLD + "" + ChatColor.BOLD + shop.getItem().getMaterial() + " (" + shop.getItem().getAmount() + "x)");
        name.setCustomNameVisible(true);
        name.setVisible(false);
        shopStands.add(price.getUniqueId().toString());
        shopStands.add(name.getUniqueId().toString());
        price.setMetadata("toKill", new FixedMetadataValue(plugin, true));
        name.setMetadata("toKill", new FixedMetadataValue(plugin, true));
        stands.put(shop, shopStands);*/

    }

    public void addShopCosmetics(Shop shop) {
        this.toSpawn.add(shop);
    }

    public ArrayList<String> getStacks() {
        return new ArrayList<String>(stacks.values());
    }

    public void removeShopCosmetics() {
        System.out.println("Removing shop cosmetics..");
        for(Entity e : Bukkit.getWorld("world").getEntities()) {
            if(e instanceof Item) {
                for(String i : stacks.values()) {
                    if(i.equalsIgnoreCase(e.getUniqueId().toString())) {
                        e.remove();
                        System.out.println("Removed " + i);
                        break;
                    }
                }
            }
            if(e instanceof ArmorStand) {
                for(ArrayList<String> s : stands.values()) {
                    for(String as : s) {
                        if(as.equalsIgnoreCase(e.getUniqueId().toString())) {
                            e.remove();
                            System.out.println("Removed " + as);
                            break;
                        }
                    }
                }
            }
        }
    }

    public boolean removeItems(Inventory inventory, ItemStack stack) {
        int amount = stack.getAmount();
        int size = inventory.getSize();
        ItemStack[] backup = inventory.getContents();
        for (int slot = 0; slot < size; slot++) {
            ItemStack is = inventory.getItem(slot);
            if (is == null) continue;
            if (is.isSimilar(stack)) {
               if(is.getAmount() > amount) {
                   is.setAmount(is.getAmount()-amount);
                   amount = 0;
               } else {
                   inventory.clear(slot);
                   amount -= is.getAmount();
               }
               if(amount == 0) break;
            }
        }
        if(amount > 0) {
            inventory.setContents(backup);
            return false;
        } else {
            return true;
        }
    }

    public void removeShopCosmetics(Shop shop) {
        for(Hologram holo : HologramsAPI.getHolograms(plugin)) {
            Location loc = areaUtil.pointToLocation(shop.getLocation()).add(new Vector(0.5, 2.3, 0.5));
            if(holo.getLocation().equals(loc)) {
                holo.delete();
                break;
            }
        }
        /*for(Entity e : Bukkit.getWorld("world").getEntities()) {
            for(String as : stands.get(shop)) {
                if(as.equalsIgnoreCase(e.getUniqueId().toString())) {
                    e.remove();
                    break;
                }
            }
            if(e.getUniqueId().toString().equalsIgnoreCase(stacks.get(shop))) {
                e.remove();
            }
        }
        stands.remove(shop);
        stacks.remove(shop);*/
    }
}
