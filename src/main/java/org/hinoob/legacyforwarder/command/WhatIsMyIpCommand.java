package org.hinoob.legacyforwarder.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

public class WhatIsMyIpCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String s, @NonNull String[] args) {
        if(sender instanceof Player player) {
            player.sendMessage("Server sees your IP as: " + player.getAddress().getHostString());
            player.sendMessage("Server sees your UUID as: " + player.getUniqueId());
        }

        return true;
    }
}
