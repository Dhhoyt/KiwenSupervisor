package net.kiwenmc.supervisor.commands;

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
import com.mojang.brigadier.Command;

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
                                    CommandSource source = context.getSource();

                                    Component message = Component.text("Invited", NamedTextColor.AQUA);
                                    source.sendMessage(message);

                                    // Returning Command.SINGLE_SUCCESS means that the execution was successful
                                    // Returning BrigadierCommand.FORWARD will send the command to the server
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
