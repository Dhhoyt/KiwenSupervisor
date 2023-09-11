package net.kiwenmc.supervisor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import net.kiwenmc.supervisor.datastructures.Party;
import net.kiwenmc.supervisor.enums.GameType;
import net.kiwenmc.supervisor.enums.ServerSize;

public class QueueManager {

    private static QueueManager instance;

    private final Map<GameType, ArrayList<Integer>> queue = new EnumMap<>(GameType.class);
    private final ArrayList<Party> parties = new ArrayList<>();
    private final Map<UUID, Integer> playerPartyId = new HashMap<>();

    private QueueManager() {
        for (GameType i : GameType.values()) {
            instance.queue.put(i, new ArrayList<Integer>());
        }
    }

    public static QueueManager getInstance() {
        if (instance == null) {
            instance = new QueueManager();
        }
        return instance;
    }

    /**
     * A user requests a game
     * 
     * @param player the UUID of the player requesting the game
     * @param game   what game the party wants to join
     * @return 0 if the players position hasn't changed, otherwise the new queue
     *         position
     */
    public int requestGame(UUID player, GameType game) {
        int partyID = playerPartyId.get(player);
        Party party = parties.get(partyID);
        // Party is already in that queue
        if (party.queuedGame == game) {
            return 0;
        }
        // Party is in another queue
        if (party.queuedGame != null) {
            queue.get(party.queuedGame).remove(Integer.valueOf(partyID));
        }
        // Add party to queue
        party.queuedGame = game;
        queue.get(game).add(partyID);
        return queue.get(game).indexOf(partyID) + 1;
    }

    /**
     * Creates a new party
     * 
     * @param player the leader of the new party
     */
    public void newParty(UUID player) {
        // Look for a blank spot
        for (int i = 0; i < parties.size(); i++) {
            if (parties.get(i) == null) {
                parties.set(i, new Party(player));
                playerPartyId.put(player, i);
                return;
            }
        }
        // Theres no blank
        parties.add(new Party(player));
        playerPartyId.put(player, parties.size() - 1);
    }

    /**
     * Transfers a player to a new party
     * 
     * @param player    the id of the player joinng the other party
     * @param joiningID the player of leader of the new party
     */
    public void joinParty(UUID player, UUID joiningID) {
        leaveParty(player);
        playerPartyId.put(player, playerPartyId.get(joiningID));
        Party newParty = parties.get(playerPartyId.get(joiningID));
        newParty.players.add(player);
    }

    public void leaveParty(UUID player) {
        Party party = parties.get(playerPartyId.get(player));
        party.players.remove(player);
        if (party.players.size() == 0) {
            if (party.queuedGame != null) {
                Integer playerID = Integer.valueOf(playerPartyId.get(player));
                queue.get(party.queuedGame).remove((Integer) playerID);
            }
        }
        playerPartyId.remove(player);
    }

    public boolean isOnlyMember(UUID player) {
        return parties.get(playerPartyId.get(player)).players.size() == 1;
    }

    public boolean isLeader(UUID player) {
        int partyID = playerPartyId.get(player);
        return parties.get(partyID).players.get(0) == player;
    }

    public void disband(UUID player) {
        Integer partyID = playerPartyId.get(player);
        for (UUID i : parties.get(partyID).players) {
            newParty(i);
        }
        parties.set(partyID, null);
    }

    /**
     * Gets the position of a player's party in the queue
     * 
     * @param player the UUID of the player
     * @return -1 if there was an error, 0 if the player isn't in a queue, otherwise
     *         the postion in the queue
     */
    public int getQueuePos(UUID player) {
        try {
            Integer partyID = playerPartyId.get(player);
            GameType game = parties.get(partyID).queuedGame;
            if (game != null) {
                return queue.get(game).indexOf(partyID) + 1;
            } else {
                return 0;
            }
        } catch (Exception e) {
            return -1;
        }
    }

    public List<Party> getPlayers(GameType game) {
        List<Party> players = new ArrayList<>();
        int numPlayers = 0;
        for (Integer i : queue.get(game)) {
            Party current = parties.get(i);
            if (numPlayers + current.players.size() <= game.maxPlayers) {
                players.add(current);
                numPlayers += current.players.size();
            }
            if (numPlayers == game.maxPlayers) {
                return players;
            }
        }
        return players;
    }

    public GameType getLongestWait(ServerSize serverSize) {
        GameType longestQueue = null;
        int longestQueueSize = 0;
        for (GameType type : GameType.values()) {
            if (type.size == serverSize) {
                int length = getQueueSize(type);
                if (longestQueue == null) {
                    longestQueue = type;
                    longestQueueSize = length;
                } else if (longestQueueSize < length) {
                    longestQueue = type;
                    longestQueueSize = length;
                }
            }
        }
        return longestQueue;
    }

    public int getQueueSize(GameType type) {
        int size = 0;
        for (int i : queue.get(type)) {
            size += parties.get(i).players.size();
        }
        return size;
    }

}
