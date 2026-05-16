package com.divinxxii.chunkswap.command;

import com.divinxxii.chunkswap.ChunkSwapMod;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class ChunkSwapCommand {

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {

        // /start <seconds>
        dispatcher.register(
            CommandManager.literal("start")
                .requires(src -> src.hasPermissionLevel(2))
                .then(CommandManager.argument("seconds", IntegerArgumentType.integer(5, 3600))
                    .executes(ctx -> {
                        int seconds = IntegerArgumentType.getInteger(ctx, "seconds");
                        ChunkSwapMod.scheduler.start(seconds);

                        ctx.getSource().sendFeedback(
                            () -> Text.literal("⏱ Chunk swap started! Swapping every ")
                                .formatted(Formatting.GREEN)
                                .append(Text.literal(seconds + " seconds").formatted(Formatting.YELLOW, Formatting.BOLD)),
                            true
                        );
                        return 1;
                    })
                )
        );

        // /pause
        dispatcher.register(
            CommandManager.literal("pause")
                .requires(src -> src.hasPermissionLevel(2))
                .executes(ctx -> {
                    if (!ChunkSwapMod.scheduler.isRunning()) {
                        ctx.getSource().sendFeedback(
                            () -> Text.literal("⚠ Challenge is not currently running.").formatted(Formatting.RED),
                            false
                        );
                        return 0;
                    }

                    ChunkSwapMod.scheduler.pause();
                    ctx.getSource().sendFeedback(
                        () -> Text.literal("⏸ Chunk swap paused.").formatted(Formatting.YELLOW),
                        true
                    );
                    return 1;
                })
        );
    }
}
