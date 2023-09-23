package net.kiwenmc.supervisor.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Optional;
import java.util.UUID;

import net.kiwenmc.supervisor.QueueManager;

public final class PartyCommand {
    public static BrigadierCommand createBrigadierCommand(final ProxyServer proxy) {
        LiteralCommandNode<CommandSource> node = LiteralArgumentBuilder.<CommandSource>literal("party")
                .requires(source -> source instanceof Player)
                .executes(context -> {
                    // Here you get the subject that executed the command
                    CommandSource source = context.getSource();

                    Component message = Component.text("No arguments provided", NamedTextColor.RED);
                    source.sendMessage(message);

                    // Returning Command.SINGLE_SUCCESS means that the execution was successful
                    // Returning BrigadierCommand.FORWARD will send the command to the server
                    return Command.SINGLE_SUCCESS;
                })
                .then(LiteralArgumentBuilder.<CommandSource>literal("invite").then(
                        RequiredArgumentBuilder
                                .<CommandSource, String>argument("player",
                                        StringArgumentType.word())
                                .executes(context -> {
                                    String argumentProvided = context.getArgument(
                                            "player", String.class);
                                    Optional<Player> player = proxy
                                            .getPlayer(argumentProvided);
                                    CommandSource source = context.getSource();

                                    if (player.isEmpty()) {
                                        Component message = Component.text(
                                                argumentProvided + " is not online!",
                                                NamedTextColor.RED);
                                        ((Player) source).sendMessage(message);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    UUID playerID = player.get().getUniqueId();
                                    UUID sourceID = ((Player) source).getUniqueId();
                                    Component partyMessage = Component.text(
                                            argumentProvided + " has been invited to the party!", NamedTextColor.AQUA);
                                    QueueManager.getInstance().getParty(sourceID).notify(partyMessage, proxy);
                                    Component playerMessage = Component.text(
                                            "You have been invited to " + argumentProvided + "'s party!",
                                            NamedTextColor.AQUA);
                                    player.get().sendMessage(playerMessage);
                                    QueueManager.getInstance().addInvite(sourceID, playerID);

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .executes(context -> {
                            // Here you get the subject that executed the command
                            CommandSource source = context.getSource();

                            Component message = Component.text(
                                    "You must speciy a player to invite.",
                                    NamedTextColor.RED);
                            source.sendMessage(message);

                            // Returning Command.SINGLE_SUCCESS means that the execution was
                            // successful
                            // Returning BrigadierCommand.FORWARD will send the command to
                            // the server
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(LiteralArgumentBuilder.<CommandSource>literal("invite").then(
                        RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                .executes(context -> {
                                    String argumentProvided = context.getArgument(
                                            "player", String.class);
                                    Optional<Player> player = proxy
                                            .getPlayer(argumentProvided);
                                    CommandSource source = context.getSource();

                                    if (player.isPresent()) {
                                        Component message = Component.text(
                                                argumentProvided + " is not online!",
                                                NamedTextColor.RED);
                                        source.sendMessage(message);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    UUID playerID = player.get().getUniqueId();
                                    UUID sourceID = ((Player) source).getUniqueId();
                                    if (!QueueManager.getInstance().exisitsInvite(sourceID, playerID)) {
                                        Component message = Component.text("You are not invited to that party.",
                                                NamedTextColor.RED);
                                        source.sendMessage(message);
                                        return Command.SINGLE_SUCCESS;
                                    }
                                    Component partyMessage = Component.text(argumentProvided + " has joined the party!",
                                            NamedTextColor.AQUA);
                                    QueueManager.getInstance().getParty(sourceID).notify(partyMessage, proxy);
                                    Component playerMessage = Component.text(
                                            "You have joined " + argumentProvided + "'s party!", NamedTextColor.AQUA);
                                    player.get().sendMessage(playerMessage);
                                    QueueManager.getInstance().joinParty(sourceID, playerID);
                                    return Command.SINGLE_SUCCESS;
                                }))
                        .executes(context -> {
                            // Here you get the subject that executed the command
                            CommandSource source = context.getSource();

                            Component message = Component.text(
                                    "You must speciy a member of the party to join.",
                                    NamedTextColor.RED);
                            source.sendMessage(message);

                            // Returning Command.SINGLE_SUCCESS means that the execution was
                            // successful
                            // Returning BrigadierCommand.FORWARD will send the command to
                            // the server
                            return Command.SINGLE_SUCCESS;
                        }))
                .then(LiteralArgumentBuilder.<CommandSource>literal("invite").executes(context -> {
                    CommandSource source = context.getSource();
                    Component message = Component.text(
                            "You have left the party.",
                            NamedTextColor.AQUA);
                    source.sendMessage(message);
                    UUID sourceID = ((Player) source).getUniqueId();
                    QueueManager.getInstance().leaveParty(sourceID);
                    QueueManager.getInstance().newParty(sourceID);
                    return Command.SINGLE_SUCCESS;
                }))
                .build();

        return new BrigadierCommand(node);
    }
}
