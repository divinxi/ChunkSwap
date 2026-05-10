package com.divinxxii.chunkswap.timer;

import com.divinxxii.chunkswap.ChunkSwapMod;
import com.divinxxii.chunkswap.util.ChunkSwapper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SwapScheduler {

    private boolean running = false;
    private int intervalTicks = 0;   // interval in ticks (20 ticks = 1 second)
    private int ticksRemaining = 0;

    public SwapScheduler() {
        ServerTickEvents.END_SERVER_TICK.register(this::onServerTick);
    }

    public void start(int intervalSeconds) {
        this.intervalTicks = intervalSeconds * 20;
        this.ticksRemaining = this.intervalTicks;
        this.running = true;
    }

    public void pause() {
        this.running = false;
    }

    public boolean isRunning() {
        return running;
    }

    public int getIntervalSeconds() {
        return intervalTicks / 20;
    }

    public int getSecondsRemaining() {
        return (int) Math.ceil(ticksRemaining / 20.0);
    }

    private void onServerTick(MinecraftServer server) {
        if (!running) return;
        if (server.getPlayerManager().getPlayerList().isEmpty()) return;

        ticksRemaining--;

        // Warning sounds at 3, 2, 1
        int secondsLeft = getSecondsRemaining();
        if (ticksRemaining % 20 == 0 && secondsLeft <= 3 && secondsLeft > 0) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.playSound(
                    net.minecraft.sound.SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                    net.minecraft.sound.SoundCategory.PLAYERS,
                    1.0f,
                    0.5f + (0.5f * (3 - secondsLeft))
                );
            }
        }

        if (ticksRemaining <= 0) {
            ticksRemaining = intervalTicks;
            performSwap(server);
        }
    }

    private void performSwap(MinecraftServer server) {
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            ServerWorld world = player.getServerWorld();
            boolean success = ChunkSwapper.swapChunk(player, world);

            if (success) {
                player.sendMessage(
                    Text.literal("⚡ Chunk swapped!").formatted(Formatting.GREEN, Formatting.BOLD),
                    true
                );
            } else {
                ChunkSwapMod.LOGGER.warn("[ChunkSwap] Swap failed for player {}", player.getName().getString());
            }
        }
    }
}
