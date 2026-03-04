package org.hinoob.legacyforwarder;

import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.util.internal.ReflectionUtil;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetAddress;

public class IPSpoof {

    public static void spoof(Player player, InetAddress address) {
        try {
            Object networkManager = SpigotReflectionUtil.getNetworkManager(player);
            Field addressField = networkManager.getClass().getDeclaredField("socketAddress");
            addressField.setAccessible(true);
            addressField.set(networkManager, address);
        } catch(NoSuchFieldException ex) {
            ex.printStackTrace();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
