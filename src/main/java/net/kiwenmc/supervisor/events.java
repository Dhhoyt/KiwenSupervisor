package net.kiwenmc.supervisor;

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;

public class events {
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        QueueManager.getInstance().newParty(event.getPlayer().getUniqueId());
    }

    public void onDisconnect(DisconnectEvent event) {
        QueueManager.getInstance().leaveParty(event.getPlayer().getUniqueId());
    }
}
