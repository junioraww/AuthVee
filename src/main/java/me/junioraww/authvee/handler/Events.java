package me.junioraww.authvee.handler;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.proxy.Player;
import me.junioraww.authvee.AuthVee;
import me.junioraww.authvee.auth.CachedPlayer;
import me.junioraww.authvee.utils.Titles;
import net.elytrium.limboapi.api.event.LoginLimboRegisterEvent;

public class Events {
    private final AuthVee plugin;

    public Events(AuthVee plugin) {
        this.plugin = plugin;
    }

    @Subscribe
    public void onProxyDisconnect(DisconnectEvent event) {
        CachedPlayer cachedPlayer = plugin.playerCache.getCachedPlayer(event.getPlayer());
        if(cachedPlayer != null) cachedPlayer.left();
    }

    @Subscribe
    public void onLoginLimboRegister(LoginLimboRegisterEvent event) {
        Player player = event.getPlayer();
        CachedPlayer cachedPlayer = plugin.playerCache.getPlayer(player);
        /*var globalBan = cachedPlayer.getBanned();
        if(globalBan != null) {
            player.disconnect(Titles.blocked(globalBan.getReason(), globalBan.getUntil()));
        }*/
        if(cachedPlayer.requireAuth(player)) {
            event.addOnJoinCallback(() -> plugin.handlePlayer(player, cachedPlayer));
        } else cachedPlayer.joined();
    }
}
