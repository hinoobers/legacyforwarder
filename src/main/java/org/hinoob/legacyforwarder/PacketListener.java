package org.hinoob.legacyforwarder;

import com.github.retrooper.packetevents.event.*;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientPluginResponse;
import com.github.retrooper.packetevents.wrapper.login.server.WrapperLoginServerPluginRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.hinoob.legacyforwarder.spoof.IPSpoof;
import org.hinoob.legacyforwarder.spoof.IdentitySpoof;
import org.hinoob.legacyforwarder.utils.ByteBufUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PacketListener extends PacketListenerAbstract {

    private final String secretKey;

    public PacketListener(String key) {
        super(PacketListenerPriority.HIGHEST);
        this.secretKey = key;
    }

    private final Map<Integer, String> idMap = new HashMap<>();
    public static final Map<User, DataResponse> responseMap = new HashMap<>();


    @Override
    public void onPacketReceive(PacketReceiveEvent event) {
        if(event.getPacketType() == PacketType.Login.Client.LOGIN_START) {
            WrapperLoginClientLoginStart w = new WrapperLoginClientLoginStart(event);
            byte[] data = new byte[]{4};

            int randomID = ThreadLocalRandom.current().nextInt(32767);
            while (idMap.containsKey(randomID)) {
                randomID = ThreadLocalRandom.current().nextInt(32767);
            }
            idMap.put(randomID, w.getUsername());
            Channel channel = (Channel) event.getUser().getChannel();
            if (channel.pipeline().get("packet_sniffer") == null) {
                channel.pipeline().addAfter("splitter", "packet_sniffer", new ChannelInboundHandlerAdapter() {
                    @Override
                    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                        if (msg instanceof ByteBuf) {
                            ByteBuf buf = (ByteBuf) msg;

                            ByteBuf copy = buf.slice();
                            copy.markReaderIndex();

                            try {
                                int packetId = ByteBufUtils.readVarInt(copy);

                                if (packetId == 2) { // Login Plugin Response
                                    int messageId = ByteBufUtils.readVarInt(copy);
                                    boolean successful = copy.readBoolean();

                                    if (successful) {
                                        byte[] data = new byte[copy.readableBytes()];
                                        copy.readBytes(data);
                                        handleResponse(messageId, data, event.getUser());

                                        // Causes weird results
                                        buf.release();
                                        return;
                                    }
                                }
                            } catch (Exception e) {
                                // Not the packet we wanted, just ignore and let it pass
                            }
                        }
                        super.channelRead(ctx, msg);
                    }
                });
            }

            WrapperLoginServerPluginRequest request = new WrapperLoginServerPluginRequest(randomID, "velocity:player_info", data);
            request.prepareForSend(channel, true);

            channel.eventLoop().execute(() -> {
                ChannelHandlerContext targetContext = channel.pipeline().context("encoder");
                if (targetContext != null) {
                    targetContext.writeAndFlush(request.getBuffer());
                } else {
                    channel.pipeline().lastContext().writeAndFlush(request.getBuffer());
                }
            });

        } else if (event.getPacketType() == PacketType.Login.Client.LOGIN_PLUGIN_RESPONSE) {
            WrapperLoginClientPluginResponse wrapper = new WrapperLoginClientPluginResponse(event);
        }
    }

    public void handleResponse(int messageId, byte[] dataFromVelocity, User user) {
        if (idMap.containsKey(messageId)) {
            if (dataFromVelocity.length < 32) return;

            byte[] signature = new byte[32];
            System.arraycopy(dataFromVelocity, 0, signature, 0, 32);
            System.out.println(Arrays.toString(signature));

            int dataLength = dataFromVelocity.length - 32;
            byte[] signedData = new byte[dataLength];
            System.arraycopy(dataFromVelocity, 32, signedData, 0, dataLength);

            try {
                Mac mac = Mac.getInstance("HmacSHA256");
                mac.init(new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
                byte[] mySignature = mac.doFinal(signedData);

                if (!MessageDigest.isEqual(signature, mySignature)) {
                    System.out.println("Signature mismatch! Check your secret key or buffer offsets.");
                    user.closeConnection();
                    return;
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            ByteBuf readBuf = Unpooled.wrappedBuffer(signedData);
            System.out.println(readBuf.readableBytes() + " " + readBuf.copy().readByte());
            try {
                // forwarding protocol version
                int version = ByteBufUtils.readVarInt(readBuf);
                String ip = ByteBufUtils.readString(readBuf);
                UUID uuid = new UUID(readBuf.readLong(), readBuf.readLong());
                String username = ByteBufUtils.readString(readBuf);
                responseMap.put(user, new DataResponse(username, uuid, new ArrayList<>(), ip));
                IPSpoof.spoof(user, ip);
                Object packetHandler = ((Channel)user.getChannel()).pipeline().get("packet_handler");
                IdentitySpoof.spoof(user, responseMap.get(user), packetHandler, () -> {
                    // This still causes console to say wrong UUID, but it's a PIA for me to figure out now.
                });
            } finally {
                readBuf.release();
            }
        }
    }
}
