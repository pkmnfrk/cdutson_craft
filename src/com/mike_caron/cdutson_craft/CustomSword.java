package com.mike_caron.cdutson_craft;

import org.bukkit.Bukkit;
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

public class CustomSword
{
    public static CustomSword emeraldSword;

    private final String key;
    private final String name;
    private final ChatColor color;
    private final int customModelData;
    private final int maxDurability;

    public CustomSword(String key, String name, ChatColor color, int customModelData, int maxDurability)
    {
        this.key = key;
        this.name = name;
        this.color = color;
        this.customModelData = customModelData;
        this.maxDurability = maxDurability;
    }

    public static void registerItems(CDutsonCraft plugin)
    {
        emeraldSword = new CustomSword("emerald_sword", "Emerald Sword", ChatColor.GREEN, 700001, 3000);

        Bukkit.addRecipe(emeraldSword.getRecipe(plugin));
    }

    public static void unregisterItems(CDutsonCraft plugin)
    {
        Bukkit.removeRecipe(emeraldSword.getKey(plugin));

        emeraldSword = null;
    }

    public static boolean checkAllEvents(EntityDamageByEntityEvent event)
    {
        return emeraldSword.checkEvent(event);
    }

    public static boolean checkAllEvents(PrepareAnvilEvent event)
    {
        return emeraldSword.checkEvent(event);
    }

    public ItemStack get()
    {
        ItemStack ret = new ItemStack(Material.DIAMOND_SWORD);
        Damageable meta = (Damageable) ret.getItemMeta();
        assert meta != null;

        meta.setDisplayName(this.color + this.name);
        meta.addItemFlags(ItemFlag.HIDE_UNBREAKABLE);
        meta.setUnbreakable(true);
        meta.setCustomModelData(this.customModelData);
        updateLore(meta);
        ret.setItemMeta(meta);
        return ret;
    }

    public void updateLore(Damageable meta)
    {
        List<String> lore = new ArrayList<>();
        lore.add("Durability: " + (maxDurability - meta.getDamage()) + "/" + maxDurability);
        meta.setLore(lore);
    }

    public NamespacedKey getKey(CDutsonCraft plugin)
    {
        return new NamespacedKey(plugin, key);
    }

    public Recipe getRecipe(CDutsonCraft plugin)
    {
        ItemStack emerald_sword = get();
        ShapedRecipe recipe = new ShapedRecipe(getKey(plugin), emerald_sword);

        recipe.shape(" E ", " E ", " S ");
        recipe.setIngredient('E', Material.EMERALD);
        recipe.setIngredient('S', Material.STICK);
        return recipe;
    }

    public boolean checkEvent(EntityDamageByEntityEvent event)
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
                if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData)
                {
                    //  CDutsonCraft.logger.info("It's the emerald sword!");
                    meta.setDamage(meta.getDamage() + 1);

                    if (meta.getDamage() >= maxDurability)
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

    private int combineDamage(Damageable meta1, Damageable meta2)
    {
        int dur1 = maxDurability - meta1.getDamage();
        int dur2 = maxDurability - meta2.getDamage();

        int totalDur = (int) ((dur1 + dur2) * 1.12);
        return maxDurability - Math.min(totalDur, maxDurability);
    }

    private int combineEnchantements(ItemStack target, ItemMeta resultMeta, ItemStack sacrifice)
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

        //CDutsonCraft.logger.info("Resulting meta: " + resultMeta);
        return cost;
    }

    public boolean checkEvent(PrepareAnvilEvent event)
    {
        //CDutsonCraft.logger.info("Anvil event");
        ItemStack firstItem = event.getInventory().getItem(0);
        ItemStack secondItem = event.getInventory().getItem(1);
        if (firstItem != null && firstItem.getType() == Material.DIAMOND_SWORD)
        {
            Damageable meta = (Damageable) firstItem.getItemMeta();
            if (meta != null && meta.hasCustomModelData() && meta.getCustomModelData() == customModelData)
            {
                if (secondItem != null)
                {
                    if (secondItem.getType() == Material.EMERALD && meta.getDamage() > 0)
                    {
                        ItemStack result = firstItem.clone();
                        int consumedItems = (meta.getDamage() / (maxDurability / 4)) + 1;

                        meta.setDamage(Math.max(0, meta.getDamage() - consumedItems * (maxDurability / 4)));
                        updateLore(meta);
                        result.setItemMeta(meta);
                        event.setResult(result);
                        event.getInventory().setRepairCost(consumedItems);
                        return true;
                    }
                    else if (secondItem.getType() == Material.DIAMOND_SWORD)
                    {
                        Damageable meta2 = (Damageable) secondItem.getItemMeta();
                        if (meta2 != null && meta2.hasCustomModelData() && meta2.getCustomModelData() == customModelData)
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
