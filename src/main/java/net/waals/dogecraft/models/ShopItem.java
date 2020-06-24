package net.waals.dogecraft.models;

import java.util.HashMap;

public class ShopItem {

    private HashMap<String, Integer> enchantments;
    private int amount;
    private String material;
    private String potionType;
    private boolean potionExtended;
    private boolean potionUpgraded;

    public ShopItem(HashMap<String, Integer> enchantments, int amount, String material, String potionType, boolean potionExtended, boolean potionUpgraded) {

        this.enchantments = enchantments;
        this.amount = amount;
        this.material = material;
        this.potionType = potionType;
        this.potionExtended = potionExtended;
        this.potionUpgraded = potionUpgraded;
    }

    public void setEnchantments(HashMap<String, Integer> enchantments) {
        this.enchantments = enchantments;
    }

    public HashMap<String, Integer> getEnchantments() {
        return enchantments;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public void setMaterial(String material) {
        this.material = material;
    }

    public int getAmount() {
        return amount;
    }

    public String getMaterial() {
        return material;
    }

    public String getPotionType() {
        return potionType;
    }

    public void setPotionType(String potionType) {
        this.potionType = potionType;
    }

    public boolean isPotionUpgraded() {
        return potionUpgraded;
    }

    public void setPotionUpgraded(boolean potionUpgraded) {
        this.potionUpgraded = potionUpgraded;
    }

    public boolean isPotionExtended() {
        return potionExtended;
    }

    public void setPotionExtended(boolean potionExtended) {
        this.potionExtended = potionExtended;
    }
}
