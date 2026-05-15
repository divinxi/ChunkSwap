package com.divinxxii.chunkswap;

import com.divinxxii.chunkswap.command.ChunkSwapCommand;
import com.divinxxii.chunkswap.timer.SwapScheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.minecraft.network.codec.PacketCodec;

public class ChunkSwapMod implements ModInitializer {

    public static final String MOD_ID = "chunkswap";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static SwapScheduler scheduler;

    public static final CustomPayload.Id<TimerPayload> TIMER_PACKET_ID =
        new CustomPayload.Id<>(Identifier.of(MOD_ID, "timer"));

    public record TimerPayload(int seconds, boolean paused) implements CustomPayload {
        public static final PacketCodec<net.minecraft.network.PacketByteBuf, TimerPayload> CODEC =
            PacketCodec.tuple(
                PacketCodecs.INTEGER, TimerPayload::seconds,
                PacketCodecs.BOOL, TimerPayload::paused,
                TimerPayload::new
            );
        @Override public CustomPayload.Id<TimerPayload> getId() { return TIMER_PACKET_ID; }
    }

    @Override
    public void onInitialize() {
        scheduler = new SwapScheduler();

        PayloadTypeRegistry.playS2C().register(TIMER_PACKET_ID, TimerPayload.CODEC);

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            ChunkSwapCommand.register(dispatcher));

        LOGGER.info("[ChunkSwap] Mod initialized!");
    }
}
