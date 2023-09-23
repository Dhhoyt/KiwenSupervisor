package net.kiwenmc.supervisor.enums;

public enum GameType {
    PARKOUR_RACE(ServerSize.MEDIUM, 25),
    BEDWARS(ServerSize.MEDIUM, 16),
    WALLS(ServerSize.LARGE, 80),
    DUELS(ServerSize.TINY, 2);

    public ServerSize size;
    public int maxPlayers;

    GameType(ServerSize inSize, int maxPlayers) {
        this.size = inSize;
        this.maxPlayers = maxPlayers;
    }
}
