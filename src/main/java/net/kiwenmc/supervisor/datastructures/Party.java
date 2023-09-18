package net.kiwenmc.supervisor.datastructures;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import com.velocitypowered.api.proxy.ProxyServer;

import net.kiwenmc.supervisor.enums.GameType;
import net.kyori.adventure.text.Component;

public class Party {
    public List<UUID> players = new ArrayList<>();
    public Map<UUID, Long> invites = new HashMap<>();
    public GameType queuedGame;

    public Party(UUID leader) {
        players.add(leader);
    }

    public void notify(Component message, ProxyServer proxy) {
        for (UUID id : players) {
            proxy.getPlayer(id).ifPresent(player -> player.sendMessage(message));
        }
    }
}
