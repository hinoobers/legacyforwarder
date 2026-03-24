package org.hinoob.legacyforwarder;

import com.github.retrooper.packetevents.protocol.player.TextureProperty;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.login.client.WrapperLoginClientLoginStart;
import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.util.List;
import java.util.UUID;

public class DataResponse {

    private final UserProfile profile;
    private final String address;
    public boolean identSpoofed = false;
    public WrapperLoginClientLoginStart loginStart;

    public DataResponse(String username, UUID uuid, List<TextureProperty> properties, String address) {
        this.profile = new UserProfile(uuid, username, properties);
        this.address = address;
    }

    public UserProfile getProfile() {
        return profile;
    }

    public String getAddress() {
        return address;
    }
}
