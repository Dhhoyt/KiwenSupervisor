package net.kiwenmc.supervisor;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;

import net.kiwenmc.supervisor.commands.PartyCommand;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kiwenmc.supervisor.QueueManager.RemovedInvite;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;

@Plugin(id = "kiwensupervisor", name = "Kiwen Supervisor", version = "0.1.0-SNAPSHOT", url = "https://kiwenmc.net", description = "Manages backend servers for Kiwen servers", authors = {
        "Boxman" })
public class Supervisor {

    private final ProxyServer server;
    private final Logger logger;

    @Inject
    public Supervisor(ProxyServer server, Logger logger) {
        this.server = server;
        this.logger = logger;
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        // Register commands
        CommandManager commandManager = server.getCommandManager();
        CommandMeta commandMeta = commandManager.metaBuilder("party")
                .aliases("p")
                .plugin(this)
                .build();
        BrigadierCommand partyCommand = PartyCommand.createBrigadierCommand(server);
        commandManager.register(commandMeta, partyCommand);

        // Start the system that clears the old player invites
        Runnable clearInvites = new Runnable() {
            public void run() {
                List<RemovedInvite> oldInvites = QueueManager.getInstance().clearOldInvites();
                for (RemovedInvite i : oldInvites) {
                    // TODO: Add formatting so the names are displayed
                    i.invitingParty.notify(
                            Component.text("The invite to that one party has timed out", NamedTextColor.RED), server);
                    server.getPlayer(i.invitedPlayer).ifPresent(player -> player.sendMessage(
                            Component.text("The invite from that one party has timed out", NamedTextColor.RED)));
                }
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(clearInvites, 5, 5, TimeUnit.SECONDS);
    }
}
