package com.mike_caron.cdutson_craft;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;

public class EmeraldSword
{
    public static ItemStack get() {
        ItemStack ret = new ItemStack(Material.DIAMOND_SWORD);
        ItemMeta meta = ret.getItemMeta();

        meta.setDisplayName(ChatColor.GREEN + "Emerald Sword");
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);
        meta.setCustomModelData(700001);
        ret.setItemMeta(meta);
        return ret;
    }

    public static NamespacedKey getKey(CDutsonCraft plugin) {
        return new NamespacedKey(plugin, "emerald_sword");
    }

    public static Recipe getRecipe(CDutsonCraft plugin) {
        ItemStack emerald_sword = get();
        ShapedRecipe recipe = new ShapedRecipe(getKey(plugin), emerald_sword);

        recipe.shape(" E ", " E ", " S ");
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('S', Material.STICK);
        return recipe;
    }
}
