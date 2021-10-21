package com.mike_caron.cdutson_craft;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class CDutsonCraft extends JavaPlugin implements Listener
{
    public static Logger logger;

    @Override
    public void onEnable()
    {
        super.onEnable();

        logger = this.getLogger();
        CustomSword.registerItems(this);
        Bukkit.getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable()
    {
        super.onDisable();

        CustomSword.unregisterItems(this);
    }

    @EventHandler
    public void onPrepareAnvil(PrepareAnvilEvent event)
    {
        CustomSword.checkAllEvents(event);
    }

    @EventHandler
    public void onSwordDamage(EntityDamageByEntityEvent event)
    {
        CustomSword.checkAllEvents(event);
    }
}
