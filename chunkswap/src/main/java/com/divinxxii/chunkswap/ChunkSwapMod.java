package com.divinxxii.chunkswap;

import com.divinxxii.chunkswap.command.ChunkSwapCommand;
import com.divinxxii.chunkswap.timer.SwapScheduler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChunkSwapMod implements ModInitializer {

    public static final String MOD_ID = "chunkswap";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static SwapScheduler scheduler;

    @Override
    public void onInitialize() {
        scheduler = new SwapScheduler();

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ChunkSwapCommand.register(dispatcher);
        });

        LOGGER.info("[ChunkSwap] Mod initialized. Ready for chaos, Divinxxii!");
    }
}
