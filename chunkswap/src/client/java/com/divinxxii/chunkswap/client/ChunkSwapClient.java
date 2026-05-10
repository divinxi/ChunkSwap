package com.divinxxii.chunkswap.client;

import com.divinxxii.chunkswap.ChunkSwapMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

@Environment(EnvType.CLIENT)
public class ChunkSwapClient implements ClientModInitializer {

    private static boolean paused = false;
    private static int secondsRemaining = 0;
    private static boolean active = false;

    @Override
    public void onInitializeClient() {
        ClientPlayNetworking.registerGlobalReceiver(ChunkSwapMod.TIMER_PACKET_ID, (payload, context) -> {
            secondsRemaining = payload.seconds();
            paused = payload.paused();
            active = true;
        });

        HudRenderCallback.EVENT.register(this::renderHud);
    }

    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !active) return;

        String text;
        int color;

        if (paused) {
            text = "⏸ Paused";
            color = 0xAAAAAA;
        } else if (secondsRemaining <= 3 && secondsRemaining > 0) {
            text = "⚡ " + secondsRemaining + "s";
            color = 0xFF4444;
        } else {
            text = "Chunk Swap: " + secondsRemaining + "s";
            color = 0x55FF55;
        }

        int screenWidth = context.getScaledWindowWidth();
        int textWidth = client.textRenderer.getWidth(text);
        int x = (screenWidth - textWidth) / 2;
        int y = context.getScaledWindowHeight() - 58;

        context.fill(x - 6, y - 3, x + textWidth + 6, y + 11, 0x55000000);
        context.drawTextWithShadow(client.textRenderer, text, x, y, color);
    }
}
