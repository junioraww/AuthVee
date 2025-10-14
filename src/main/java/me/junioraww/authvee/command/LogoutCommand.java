package me.junioraww.authvee.command;

import com.velocitypowered.api.command.RawCommand;
import com.velocitypowered.api.proxy.Player;
import me.junioraww.authvee.AuthVee;
import me.junioraww.authvee.auth.CachedPlayer;

public class LogoutCommand implements RawCommand {
    private final AuthVee plugin;
    public LogoutCommand(AuthVee plugin) {
        this.plugin = plugin;
    }

    @Override
    public void execute(final Invocation invocation) {
        if(invocation.source() instanceof Player player) {
            CachedPlayer cachedPlayer = plugin.playerCache.getPlayer(player);
            cachedPlayer.logout();
            plugin.handlePlayer(player, cachedPlayer);
        }
    }
}
