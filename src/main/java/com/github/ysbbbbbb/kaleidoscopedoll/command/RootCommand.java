package com.github.ysbbbbbb.kaleidoscopedoll.command;

import com.github.ysbbbbbb.kaleidoscopedoll.KaleidoscopeDoll;
import com.github.ysbbbbbb.kaleidoscopedoll.command.subcommand.ReloadCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandSourceStack;

public final class RootCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> root = LiteralArgumentBuilder.<CommandSourceStack>literal(KaleidoscopeDoll.MOD_ID)
                .requires((source -> net.minecraft.commands.Commands.LEVEL_ADMINS.check(source.permissions())));
        root.then(ReloadCommand.get());
        dispatcher.register(root);
    }
}
