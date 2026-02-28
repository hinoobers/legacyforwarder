package org.hinoob.legacyforwarder;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import org.bukkit.plugin.java.JavaPlugin;

public class LegacyForwarder extends JavaPlugin {

    @Override
    public void onLoad() {
        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(this));
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().getEventManager().registerListener(new PacketListener(getConfig().getString("secret-key")));
    }

    @Override
    public void onEnable() {
        saveDefaultConfig();
        PacketEvents.getAPI().init();
    }
}
