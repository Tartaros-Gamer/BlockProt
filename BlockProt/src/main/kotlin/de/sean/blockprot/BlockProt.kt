package de.sean.blockprot

import de.sean.blockprot.bukkit.events.*
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.event.Listener
import org.bukkit.plugin.PluginManager
import org.bukkit.plugin.java.JavaPlugin

class BlockProt : JavaPlugin() {
    companion object {
        lateinit var instance: BlockProt
        lateinit var metrics: Metrics
        const val pluginId: Int = 9999;
    }

    override fun onEnable() {
        this.also { instance = it }.saveDefaultConfig()

        /* bStats Metrics */
        metrics = Metrics(this, pluginId)

        /* Register Events */
        val pm = Bukkit.getServer().pluginManager
        registerEvent(pm, BlockEvent(this))
        registerEvent(pm, ExplodeEvent())
        registerEvent(pm, HopperEvent())
        registerEvent(pm, InteractEvent())
        registerEvent(pm, InventoryEvent())

        super.onEnable()
    }

    private fun registerEvent(pm: PluginManager, listener: Listener) {
        pm.registerEvents(listener, this)
    }
}