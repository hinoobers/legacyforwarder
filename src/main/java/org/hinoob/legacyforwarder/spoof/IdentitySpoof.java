package org.hinoob.legacyforwarder.spoof;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import io.netty.channel.AbstractChannel;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.hinoob.legacyforwarder.DataResponse;
import org.hinoob.legacyforwarder.LegacyForwarder;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.UUID;

public class IdentitySpoof {

    // [20:28:06 INFO]: Network manager: net.minecraft.server.v1_8_R3.NetworkManager@4c4b81f1
    //
    //[20:28:06 INFO]: interface org.apache.logging.log4j.Logger
    //
    //[20:28:06 INFO]: interface org.apache.logging.log4j.Marker
    //
    //[20:28:06 INFO]: interface org.apache.logging.log4j.Marker
    //
    //[20:28:06 INFO]: class io.netty.util.AttributeKey
    //
    //[20:28:06 INFO]: class net.minecraft.server.v1_8_R3.LazyInitVar
    //
    //[20:28:06 INFO]: class net.minecraft.server.v1_8_R3.LazyInitVar
    //
    //[20:28:06 INFO]: class net.minecraft.server.v1_8_R3.LazyInitVar
    //
    //[20:28:06 INFO]: class net.minecraft.server.v1_8_R3.EnumProtocolDirection
    //
    //[20:28:06 INFO]: interface java.util.Queue
    //
    //[20:28:06 INFO]: class java.util.concurrent.locks.ReentrantReadWriteLock
    //
    //[20:28:06 INFO]: interface io.netty.channel.Channel
    //
    //[20:28:06 INFO]: class java.net.SocketAddress
    //
    //[20:28:06 INFO]: class java.util.UUID
    //
    //[20:28:06 INFO]: class [Lcom.mojang.authlib.properties.Property;
    //
    //[20:28:06 INFO]: boolean
    //
    //[20:28:06 INFO]: interface net.minecraft.server.v1_8_R3.PacketListener
    //
    //[20:28:06 INFO]: interface net.minecraft.server.v1_8_R3.IChatBaseComponent
    //
    //[20:28:06 INFO]: boolean
    //
    //[20:28:06 INFO]: boolean

    public static void spoof(User user, DataResponse response, Object packetHandler, Runnable runnable){
        for(Field field : packetHandler.getClass().getDeclaredFields()) {
            field.setAccessible(true);
            if(field.getType().toString().contains(".PacketListener")) {
                try {
                    Object list = field.get(packetHandler);

                    for(Field subfield : list.getClass().getDeclaredFields()) {
                        subfield.setAccessible(true);
                        if(subfield.getType().toString().toLowerCase().contains("gameprofile")) {
                            Object gameprofile = subfield.get(list);

                            System.out.println("Gameprofile: " + gameprofile);
                            for(Field subsubfield : gameprofile.getClass().getDeclaredFields()) {
                                subsubfield.setAccessible(true);

                                if(subsubfield.getType().equals(UUID.class)) {
                                    subsubfield.set(gameprofile, response.getProfile().getUUID());
                                } else if(subsubfield.getType().equals(String.class)) {
                                    subsubfield.set(gameprofile, response.getProfile().getName());
                                }
                            }

                        }
                    }
                    response.identSpoofed = true;
                    runnable.run();
                } catch(IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
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
