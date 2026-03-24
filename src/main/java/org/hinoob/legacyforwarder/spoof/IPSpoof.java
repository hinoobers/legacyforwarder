package org.hinoob.legacyforwarder.spoof;

import com.github.retrooper.packetevents.protocol.player.User;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.hinoob.legacyforwarder.LegacyForwarder;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class IPSpoof {

    public static void spoof(User user, String ip) {
        InetSocketAddress address = new InetSocketAddress(ip, 2522); // port is irellevant im pretty sure

        try {
            Channel channel = (Channel) user.getChannel();
            ChannelHandlerContext networkManagerContext = channel.pipeline().context("packet_handler");
            if (networkManagerContext != null) {
                Object networkManager = networkManagerContext.handler();
                // Search for SocketAddress fields within the NetworkManager class
                for (Field field : networkManager.getClass().getDeclaredFields()) {
                    if (SocketAddress.class.isAssignableFrom(field.getType())) {
                        field.setAccessible(true);
                        field.set(networkManager, address);
                    }
                }
            }

            Field addy = getField(SocketAddress.class, AbstractChannel.class, 1);
            addy.set(channel, address);
        } catch (IllegalAccessException e) {
            LegacyForwarder.getPlugin(LegacyForwarder.class).getLogger().info("Failed to spoof player's ip, error: " + e.getMessage());
        }
    }

    private static Field getField(Class<?> type, Class<?> sourceClass, int index) {
        int i = 0;
        for(Field field : sourceClass.getDeclaredFields()) {
            if(type.isAssignableFrom(field.getType())) {
                if(index == i) {
                    field.setAccessible(true);
                    return field;
                }
                i++;
            }
        }

        return null;
    }
}
