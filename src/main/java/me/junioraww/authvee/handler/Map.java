package me.junioraww.authvee.handler;

import com.velocitypowered.api.network.ProtocolVersion;
import net.elytrium.limboapi.api.material.VirtualItem;
import net.elytrium.limboapi.api.material.WorldVersion;

public class Map implements VirtualItem {
    @Override
    public short getID(ProtocolVersion protocolVersion) {
        return 1;
    }

    @Override
    public short getID(WorldVersion worldVersion) {
        return 1;
    }

    @Override
    public boolean isSupportedOn(ProtocolVersion protocolVersion) {
        return true;
    }

    @Override
    public boolean isSupportedOn(WorldVersion worldVersion) {
        return true;
    }

    @Override
    public String getModernID() {
        return "minecraft:filled_map";
    }
}
