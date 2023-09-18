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
                        RequiredArgumentBuilder.<CommandSource, String>argument("player", StringArgumentType.word())
                                .executes(context -> {
                                    String argumentProvided = context.getArgument("player", String.class);
                                    Optional<Player> player = proxy.getPlayer(argumentProvided);
                                    CommandSource source = context.getSource();

                                    if (player.isPresent()) {
                                        Component partyMessage = Component.text(
                                                argumentProvided + " has been invited to the party!",
                                                NamedTextColor.AQUA);
                                        QueueManager.getInstance().getParty(((Player) source).getUniqueId())
                                                .notify(partyMessage, proxy);
                                        Component playerMessage = Component.text(
                                                "Invited " + argumentProvided + " to the party!", NamedTextColor.AQUA);
                                        player.get().sendMessage(playerMessage);
                                    } else {
                                        Component message = Component.text(
                                                argumentProvided + " is not online!",
                                                NamedTextColor.RED);
                                        ((Player) source).sendMessage(message);
                                    }

                                    return Command.SINGLE_SUCCESS;
                                }))
                        .executes(context -> {
                            // Here you get the subject that executed the command
                            CommandSource source = context.getSource();

                            Component message = Component.text("You must speciy a player to invite.",
                                    NamedTextColor.RED);
                            source.sendMessage(message);

                            // Returning Command.SINGLE_SUCCESS means that the execution was successful
                            // Returning BrigadierCommand.FORWARD will send the command to the server
                            return Command.SINGLE_SUCCESS;
                        }))

                .build();

        return new BrigadierCommand(node);
    }
}
