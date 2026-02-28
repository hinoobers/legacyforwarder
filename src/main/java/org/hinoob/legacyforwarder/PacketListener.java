package org.hinoob.legacyforwarder;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.netty.buffer.UnpooledByteBufAllocationHelper;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.wrapper.PacketWrapper;
import com.github.retrooper.packetevents.wrapper.handshaking.client.WrapperHandshakingClientHandshake;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientPluginResponse;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerPluginRequest;
import com.github.retrooper.packetevents.wrapper.play.client.WrapperPlayClientPluginMessage;
import com.google.common.net.InetAddresses;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

public class PacketListener extends PacketListenerAbstract {

    private final String secretKey;

    public PacketListener(String key) {
        super(PacketListenerPriority.HIGHEST);
        this.secretKey = key;
    }

    private final Map<Integer, String> idMap = new HashMap<>();

    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if(event.getPacketType() == PacketType.Login.Client.LOGIN_START) {
            WrapperLoginClientLoginStart w = new WrapperLoginClientLoginStart(event);

            ByteBuf buf = Unpooled.buffer();
            buf.writeByte(4);

            byte[] data = new byte[buf.readableBytes()];
            buf.readBytes(data);

            int randomID = new Random().nextInt(32767);
            while(idMap.containsKey(randomID)) {
                randomID = new Random().nextInt(32767);
            }
            idMap.put(randomID, w.getUsername());
            WrapperLoginServerPluginRequest wrapper = new WrapperLoginServerPluginRequest(randomID, "velocity:player_info", data);
            event.getUser().sendPacket(wrapper);
        } else if(event.getPacketType() == PacketType.Login.Client.LOGIN_PLUGIN_RESPONSE) {
            WrapperLoginClientPluginResponse wrapper = new WrapperLoginClientPluginResponse(event);
            if(idMap.containsKey(wrapper.getMessageId())) {
                ByteBuf buf = Unpooled.copiedBuffer(wrapper.getData());

                byte[] signature = new byte[32];
                buf.readBytes(signature);

                byte[] data = new byte[buf.readableBytes()];
                buf.getBytes(buf.readerIndex(), data);


                try {
                    Mac mac = Mac.getInstance("HmacSHA256");
                    mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                    byte[] mySignature = mac.doFinal(data);
                    boolean signatureEqual = MessageDigest.isEqual(signature, mySignature);
                    if(!signatureEqual) {
                        event.getUser().closeConnection();
                        return;
                    }
                } catch (NoSuchAlgorithmException | InvalidKeyException var5) {
                    event.getUser().closeConnection();
                }

                PacketWrapper<?> reader = PacketWrapper.createUniversalPacketWrapper(buf.slice(32, buf.readableBytes()));
                int version = reader.readVarInt();
                InetAddress address = InetAddresses.forString(reader.readString(32767));
                UUID uuid = reader.readUUID();
                String username = reader.readString();


            }
        }
    }
}
