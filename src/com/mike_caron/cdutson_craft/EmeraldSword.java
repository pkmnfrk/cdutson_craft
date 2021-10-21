package com.mike_caron.cdutson_craft;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EmeraldSword
{
    public static final int MAX_DURABILITY = 3000;
    public static final int REPAIR_DURABILITY = MAX_DURABILITY / 4;
    public static final int MODEL_DATA = 700001;

    public static ItemStack get()
    {
        ItemStack ret = new ItemStack(Material.DIAMOND_SWORD);
        Damageable meta = (Damageable) ret.getItemMeta();
        assert meta != null;

        meta.setDisplayName(ChatColor.GREEN + "Emerald Sword");
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);
        meta.setCustomModelData(MODEL_DATA);
        updateLore(meta);
        ret.setItemMeta(meta);
        return ret;
    }

    public static void updateLore(Damageable meta)
    {
        List<String> lore = new ArrayList<>();
        lore.add("Durability: " + (MAX_DURABILITY - meta.getDamage()) + "/" + MAX_DURABILITY);
        meta.setLore(lore);
    }

    public static NamespacedKey getKey(CDutsonCraft plugin)
    {
        return new NamespacedKey(plugin, "emerald_sword");
    }

    public static Recipe getRecipe(CDutsonCraft plugin)
    {
        ItemStack emerald_sword = get();
        ShapedRecipe recipe = new ShapedRecipe(getKey(plugin), emerald_sword);

        recipe.shape(" E ", " E ", " S ");
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('S', Material.STICK);
        return recipe;
    }

    public static boolean checkEvent(EntityDamageByEntityEvent event)
    {
        //CDutsonCraft.logger.info("checking damage event");
        if (event.getDamager() instanceof Player player)
        { //If the damager is player
            // CDutsonCraft.logger.info("it's a player!");
            ItemStack weapon = player.getInventory().getItemInMainHand();
            if (weapon.getType() == Material.DIAMOND_SWORD)
            {
                // CDutsonCraft.logger.info("it's a diamond sword!");
                Damageable meta = (Damageable) weapon.getItemMeta();
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == MODEL_DATA)
                {
                    //  CDutsonCraft.logger.info("It's the emerald sword!");
                    meta.setDamage(meta.getDamage() + 1);

                    if (meta.getDamage() >= MAX_DURABILITY)
                    {
                        weapon = null;
                    }
                    else
                    {
                        updateLore(meta);
                        weapon.setItemMeta(meta);
                    }
                    player.getInventory().setItemInMainHand(weapon);
                    player.updateInventory();

                    return true;
                }
            }
        }
        return false;
    }

    private static int combineDamage(Damageable meta1, Damageable meta2)
    {
        int dur1 = MAX_DURABILITY - meta1.getDamage();
        int dur2 = MAX_DURABILITY - meta2.getDamage();

        int totalDur = (int) ((dur1 + dur2) * 1.12);
        return MAX_DURABILITY - Math.min(totalDur, MAX_DURABILITY);
    }

    private static int combineEnchantements(ItemStack target, ItemMeta resultMeta, ItemStack sacrifice)
    {
        ItemMeta meta1 = target.getItemMeta();
        assert meta1 != null;
        Map<Enchantment, Integer> levels1 = new HashMap<>(meta1.getEnchants());
        Map<Enchantment, Integer> levels2;
        ItemMeta meta2 = sacrifice.getItemMeta();
        assert meta2 != null;
        if (meta2 instanceof EnchantmentStorageMeta enchMeta2)
        {
            if (enchMeta2.hasStoredEnchants())
            {
                levels2 = enchMeta2.getStoredEnchants();
            }
            else
            {
                levels2 = meta2.getEnchants();
            }
        }
        else
        {
            levels2 = meta2.getEnchants();
        }

        CDutsonCraft.logger.info("Left: " + levels1.size() + ", right: " + levels2.size());

        int cost = 0;

        for (Map.Entry<Enchantment, Integer> e : levels2.entrySet())
        {
            Enchantment enchantment = e.getKey();
            Integer level = e.getValue();
            if (levels1.containsKey(enchantment))
            {
                if (level > levels1.get(enchantment))
                {
                    levels1.put(enchantment, level);
                    cost += level * 2;
                }
                else if (level.equals(levels1.get(enchantment)))
                {
                    levels1.put(enchantment, Math.min(level + 1, enchantment.getMaxLevel()));
                    cost += (level + 1) * 2;
                }
            }
            else
            {
                if (enchantment.canEnchantItem(target))
                {
                    boolean conflicts = false;
                    for (Enchantment ench : levels1.keySet())
                    {
                        if (enchantment.conflictsWith(ench))
                        {
                            cost += 1;
                            conflicts = true;
                            break;
                        }
                    }
                    if (!conflicts)
                    {
                        levels1.put(enchantment, level);
                        cost += (level + 1) * 2;
                    }

                }
            }
        }

        for (Map.Entry<Enchantment, Integer> entry : levels1.entrySet())
        {
            Enchantment enchantment = entry.getKey();
            if (!levels2.containsKey(enchantment))
            {
                cost += entry.getValue() * 2;
            }
        }

        levels1.forEach((enchantment, integer) -> resultMeta.addEnchant(enchantment, integer, true));

        CDutsonCraft.logger.info("Resulting meta: " + resultMeta);
        return cost;
    }

    public static boolean checkEvent(PrepareAnvilEvent event)
    {
        CDutsonCraft.logger.info("Anvil event");
        ItemStack firstItem = event.getInventory().getItem(0);
        ItemStack secondItem = event.getInventory().getItem(1);
        if (firstItem != null && firstItem.getType() == Material.DIAMOND_SWORD)
        {
            Damageable meta = (Damageable) firstItem.getItemMeta();
            if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == MODEL_DATA)
            {
                if (secondItem != null)
                {
                    if (secondItem.getType() == Material.EMERALD && meta.getDamage() > 0)
                    {
                        ItemStack result = firstItem.clone();
                        int consumedItems = (meta.getDamage() / REPAIR_DURABILITY) + 1;

                        meta.setDamage(Math.max(0, meta.getDamage() - consumedItems * REPAIR_DURABILITY));
                        updateLore(meta);
                        result.setItemMeta(meta);
                        event.setResult(result);
                        event.getInventory().setRepairCost(consumedItems);
                        return true;
                    }
                    else if (secondItem.getType() == Material.DIAMOND_SWORD)
                    {
                        Damageable meta2 = (Damageable) secondItem.getItemMeta();
                        if (meta2 != null && meta2.hasCustomModelData() && meta2.getCustomModelData() == MODEL_DATA)
                        {
                            ItemStack result = firstItem.clone();
                            Damageable resultMeta = meta.clone();
                            int cost = 0;
                            if (meta.getDamage() > 0)
                            {
                                cost += 2;
                                resultMeta.setDamage(combineDamage(meta, meta2));
                            }

                            cost += combineEnchantements(result, resultMeta, secondItem);

                            updateLore(resultMeta);
                            result.setItemMeta(resultMeta);
                            event.getInventory().setRepairCost(cost);
                            event.setResult(result);
                            return true;
                        }
                    }
                }
            }
            else
            {
                event.setResult(null);
                return true;
            }
        }
        return false;
    }
}
