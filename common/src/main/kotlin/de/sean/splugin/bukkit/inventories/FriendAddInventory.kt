package de.sean.splugin.bukkit.inventories

import org.bukkit.Bukkit

object FriendAddInventory {
    const val INVENTORY_NAME = "Add Friend"
    /// The content needs to be added when opened.
    var inventory = Bukkit.createInventory(null, 9 * 3, INVENTORY_NAME)
}