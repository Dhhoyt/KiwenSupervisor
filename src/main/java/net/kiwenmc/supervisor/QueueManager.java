package net.kiwenmc.supervisor;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

import net.kiwenmc.supervisor.datastructures.Party;
import net.kiwenmc.supervisor.enums.GameType;
import net.kiwenmc.supervisor.enums.ServerSize;

public class QueueManager {

    private static QueueManager instance;

    private final Map<GameType, ArrayList<Integer>> queue = new EnumMap<>(GameType.class);
    private final Map<Integer, Party> parties = new HashMap<>(); // A hashset wouldn't work here because
    private final Map<UUID, Integer> playerPartyId = new HashMap<>();

    final long inviteTimeout = 60 * 5 * 1000;

    private int nextFreeParty = 0;

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
    public int queueGame(UUID player, GameType game) {
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
        parties.put(nextFreeParty, new Party(player));
        playerPartyId.put(player, nextFreeParty);
        nextFreeParty += 1;
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

    /**
     * Makes a player leave a party. This function does not create a new party as it
     * may be called when a player leaves or when a player joins an existing party.
     * 
     * @param player The player that is getting removed for the party
     */
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

    /**
     * Returns true if the player is the only player in the party
     * 
     * @param player The player to check
     * @return true if the player is the only player in the party
     */
    public boolean isOnlyMember(UUID player) {
        return parties.get(playerPartyId.get(player)).players.size() == 1;
    }

    /**
     * Returns true if the player is the leader of their party
     * 
     * @param player The player to check
     * @return true if the player is the leader of their party
     */
    public boolean isLeader(UUID player) {
        int partyID = playerPartyId.get(player);
        return parties.get(partyID).players.get(0) == player;
    }

    /**
     * Disbands a party and puts all the players in a new party
     * 
     * @param player Any player in the party
     */
    public void disband(UUID player) {
        Integer partyID = playerPartyId.get(player);
        for (UUID i : parties.get(partyID).players) {
            newParty(i);
        }
        parties.remove(partyID);
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

    /**
     * Gets parties queueing for a game
     * 
     * @param game What game to check for
     * @return List of parties queueing for a game
     */
    public List<Party> getQueueingParties(GameType game) {
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

    public void notifyParty() {

    }

    /**
     * Returns the longest queue of a game that uses the specified size.
     * 
     * @param serverSize The specified size
     * @return The gametype with the longest wait
     */
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

    /**
     * Gets the number of players waiting in a queue
     * 
     * @param type The game type
     * @return The number of players waiting in that queue
     */
    public int getQueueSize(GameType type) {
        int size = 0;
        for (int i : queue.get(type)) {
            size += parties.get(i).players.size();
        }
        return size;
    }

    /**
     * Adds an invite. The UUID who is sending it must be privileged as this method
     * does not check
     * 
     * @param from The player sending the invite
     * @param to   The player being invited
     */
    public void addInvite(UUID from, UUID to) {
        long time = System.currentTimeMillis();
        int partyID = playerPartyId.get(from);
        Party party = parties.get(partyID);
        party.invites.put(to, time);
    }

    /**
     * 
     */
    public void acceptInvite(UUID inviting, UUID accepting) {
        int partyID = playerPartyId.get(inviting);
        Party party = parties.get(partyID);
        if (party.invites.containsKey(inviting)) {

        }
    }

    public final class RemovedInvite {
        Party invitingParty;
        UUID invitedPlayer;

        private RemovedInvite(Party invitingParty, UUID invitedPlayer) {
            this.invitingParty = invitingParty;
            this.invitedPlayer = invitedPlayer;
        }
    }

    /**
     * Clears and returns the invites that have timed out
     * 
     * @return The invites that have timed out
     */
    public List<RemovedInvite> clearOldInvites() {
        List<RemovedInvite> removedInvites = new ArrayList<>();
        long time = System.currentTimeMillis();
        for (Entry<Integer, Party> party : parties.entrySet()) {
            for (Entry<UUID, Long> invite : party.getValue().invites.entrySet()) {
                if (invite.getValue() + inviteTimeout < time) {
                    removedInvites.add(new RemovedInvite(party.getValue(), invite.getKey()));
                    party.getValue().invites.remove(invite.getKey());
                }
            }
        }
        return removedInvites;
    }
}
