package com.divinxxii.chunkswap.timer;

import com.divinxxii.chunkswap.ChunkSwapMod;
import com.divinxxii.chunkswap.util.ChunkSwapper;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class SwapScheduler {

    private boolean running = false;
    private int intervalTicks = 0;
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

    public boolean isRunning() { return running; }
    public int getSecondsRemaining() { return (int) Math.ceil(ticksRemaining / 20.0); }

    private void onServerTick(MinecraftServer server) {
        if (server.getPlayerManager().getPlayerList().isEmpty()) return;

        // Send HUD update every 20 ticks (1 second)
        if (server.getTicks() % 20 == 0) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ServerPlayNetworking.send(player,
                    new ChunkSwapMod.TimerPayload(getSecondsRemaining(), !running));
            }
        }

        if (!running) return;
        ticksRemaining--;

        int secondsLeft = getSecondsRemaining();
        if (ticksRemaining % 20 == 0 && secondsLeft <= 3 && secondsLeft > 0) {
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                player.getWorld().playSound(null, player.getBlockPos(),
                    SoundEvents.BLOCK_NOTE_BLOCK_PLING.value(),
                    SoundCategory.PLAYERS, 1.0f, 0.5f + (0.5f * (3 - secondsLeft)));
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
                    Text.literal("⚡ Chunk swapped!").formatted(Formatting.GREEN, Formatting.BOLD), true);
            }
        }
    }
}
