package com.mike_caron.cdutson_craft;

import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class CDutsonCraft extends JavaPlugin
{
    @Override
    public void onEnable()
    {
        super.onEnable();

        Bukkit.addRecipe(EmeraldSword.getRecipe(this));
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        Bukkit.removeRecipe(EmeraldSword.getKey(this));
    }
}
