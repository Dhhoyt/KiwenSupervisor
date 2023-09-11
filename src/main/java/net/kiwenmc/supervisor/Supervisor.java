package net.kiwenmc.supervisor;

import com.google.inject.Inject;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.proxy.ProxyServer;
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
}
