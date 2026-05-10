package com.divinxxii.chunkswap.client;

import com.divinxxii.chunkswap.ChunkSwapMod;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;

@Environment(EnvType.CLIENT)
public class ChunkSwapClient implements ClientModInitializer {

    // These are updated via network packet or reflected from server state
    public static boolean hudVisible = false;
    public static boolean paused = false;
    public static int secondsRemaining = 0;

    @Override
    public void onInitializeClient() {
        HudRenderCallback.EVENT.register(this::renderHud);
    }

    private void renderHud(DrawContext context, RenderTickCounter tickCounter) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null || !hudVisible) return;

        String text;
        int color;

        if (paused) {
            text = "§7⏸ Paused";
            color = 0xAAAAAA;
        } else if (secondsRemaining <= 3) {
            text = "§c⚡ " + secondsRemaining + "s";
            color = 0xFF4444;
        } else {
            text = "§aChunk Swap: §f" + secondsRemaining + "s";
            color = 0xFFFFFF;
        }

        int screenWidth = context.getScaledWindowWidth();
        int textWidth = client.textRenderer.getWidth(text);
        int x = (screenWidth - textWidth) / 2;
        int y = context.getScaledWindowHeight() - 58; // just above hotbar

        // Draw a subtle dark background pill
        context.fill(x - 6, y - 3, x + textWidth + 6, y + 11, 0x55000000);
        context.drawTextWithShadow(client.textRenderer, text, x, y, color);
    }
}
