package me.junioraww.authvee.auth;

import com.velocitypowered.api.proxy.Player;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

public class PlayerCache {
    private static final HashMap<String, CachedPlayer> playerCache = new HashMap<>();

    public CachedPlayer getPlayer(Player player) { // needs to be in lowercase!
        String playerName = player.getUsername().toLowerCase();
        if(!playerCache.containsKey(playerName)) playerCache.put(playerName, new CachedPlayer(player));
        return playerCache.get(playerName);
    }

    public CachedPlayer getCachedPlayer(Player player) {
        return playerCache.get(player.getUsername().toLowerCase());
    }

    public void clearExpired() {
        Thread thread = new Thread(() -> {
            long unixTime = System.currentTimeMillis() / 1000L;
            Logger.getLogger("Auth Thread playerCache").info(playerCache.keySet().toString());
            for(Iterator<Map.Entry<String, CachedPlayer>> it = playerCache.entrySet().iterator(); it.hasNext(); ) {
                Map.Entry<String, CachedPlayer> entry = it.next();
                CachedPlayer cachedPlayer = entry.getValue();
                if(!cachedPlayer.isOnline() && unixTime > cachedPlayer.getExpiresAfter()) it.remove();
            }
        });

        thread.start();
    }
}
