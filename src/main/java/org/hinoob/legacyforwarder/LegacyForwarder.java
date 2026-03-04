package org.hinoob.legacyforwarder;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.protocol.player.User;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;
import io.github.retrooper.packetevents.util.SpigotReflectionUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LegacyForwarder extends JavaPlugin implements Listener {

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
        getServer().getPluginManager().registerEvents(this,this);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        User user = PacketEvents.getAPI().getProtocolManager().getUser(SpigotReflectionUtil.getChannel(event.getPlayer()));
        //IPSpoof.spoof(event.getPlayer(), PacketListener.responseMap.get(user).getAddress());
    }
}
