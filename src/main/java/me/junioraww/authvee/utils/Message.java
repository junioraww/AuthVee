package me.junioraww.authvee.utils;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

public class Message {
    public static final LegacyComponentSerializer serializer = LegacyComponentSerializer.legacyAmpersand();

    public static void send(Player player, String message) {
        var formatted = serializer.deserialize(message);
        player.sendMessage(formatted);
    }

    public static void sendActionbar(Player player, String message) {
        var formatted = serializer.deserialize(message);
        player.sendActionBar(formatted);
    }

    public static Component get(String message) {
        return serializer.deserialize(message);
    }
}
