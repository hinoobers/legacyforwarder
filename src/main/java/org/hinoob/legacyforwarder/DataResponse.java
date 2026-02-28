package org.hinoob.legacyforwarder;

import com.google.common.net.InetAddresses;

import java.net.InetAddress;
import java.util.UUID;

public class DataResponse {

    private final String username;
    private final UUID uuid;
    private final InetAddress address;

    public DataResponse(String username, UUID uuid, InetAddress address) {
        this.username = username;
        this.uuid = uuid;
        this.address = address;
    }

    public String getUsername() {
        return username;
    }

    public UUID getUUID() {
        return uuid;
    }

    public InetAddress getAddress() {
        return address;
    }
}
