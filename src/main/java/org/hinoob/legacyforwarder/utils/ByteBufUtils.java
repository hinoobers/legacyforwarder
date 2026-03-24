package org.hinoob.legacyforwarder.utils;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class ByteBufUtils {

    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value & 0x7F);
    }

    public static void writeString(ByteBuf buf, String s) {
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }

    public static String readString(ByteBuf buf) {
        // 1. Read the length as a VarInt
        int length = readVarInt(buf);

        // 2. Read the bytes and convert to UTF-8
        byte[] bytes = new byte[length];
        buf.readBytes(bytes);

        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static int readVarInt(ByteBuf buf) {
        int numRead = 0;
        int result = 0;
        byte read;
        do {
            read = buf.readByte();
            int value = (read & 0b01111111);
            result |= (value << (7 * numRead));

            numRead++;
            if (numRead > 5) {
                throw new RuntimeException("VarInt is too big");
            }
        } while ((read & 0b10000000) != 0);

        return result;
    }
}
