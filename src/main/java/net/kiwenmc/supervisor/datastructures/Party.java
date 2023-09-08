package net.kiwenmc.supervisor.datastructures;

import java.util.List;
import java.util.ArrayList;

import java.util.UUID;

import net.kiwenmc.supervisor.enums.GameType;

public class Party{
    public List<UUID> players;
    public GameType queuedGame;
    public Party(UUID leader){
        players = new ArrayList<>();
        players.add(leader);
    }
}
