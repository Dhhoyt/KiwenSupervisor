package net.kiwenmc.supervisor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.kiwenmc.supervisor.datastructures.Party;
import net.kiwenmc.supervisor.enums.GameType;

public class QueueManager {

    private static QueueManager instance;

    private static final Map<GameType, ArrayList<Integer>> queue = new EnumMap<>(GameType.class);
    private static final ArrayList<Party> parties = new ArrayList<>();
    private static final Map<UUID, Integer> playerPartyId = new HashMap<>();
    

    private QueueManager() {

    }

    public static void init() {
        instance = new QueueManager();
    }
    public static QueueManager getInstance() {
        return instance;
    }
}
