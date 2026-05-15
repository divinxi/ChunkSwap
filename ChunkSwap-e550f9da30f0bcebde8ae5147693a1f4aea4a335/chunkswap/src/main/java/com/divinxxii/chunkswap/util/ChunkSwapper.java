package com.divinxxii.chunkswap.util;

import com.divinxxii.chunkswap.ChunkSwapMod;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ChunkSwapper {

    private static final Random RANDOM = new Random();
    private static final int MIN_DISTANCE_CHUNKS = 32;   // ~512 blocks away minimum
    private static final int MAX_RADIUS_CHUNKS = 6250;   // ~100,000 blocks radius
    private static final int MAX_RETRY_ATTEMPTS = 10;

    /**
     * Performs the chunk swap for the given player in the given world.
     * Copies the random chunk's blocks into the player's current chunk.
     * Player position is unchanged.
     */
    public static boolean swapChunk(ServerPlayerEntity player, ServerWorld world) {
        ChunkPos playerChunkPos = player.getChunkPos();
        ChunkPos targetChunkPos = findRandomChunk(world, playerChunkPos);

        if (targetChunkPos == null) {
            ChunkSwapMod.LOGGER.warn("[ChunkSwap] Could not find a valid remote chunk after {} attempts.", MAX_RETRY_ATTEMPTS);
            return false;
        }

        try {
            WorldChunk playerChunk = world.getChunk(playerChunkPos.x, playerChunkPos.z);
            WorldChunk remoteChunk = world.getChunk(targetChunkPos.x, targetChunkPos.z);

            ChunkSnapshot playerSnapshot = snapshotChunk(world, playerChunk, playerChunkPos);
            ChunkSnapshot remoteSnapshot = snapshotChunk(world, remoteChunk, targetChunkPos);

            applySnapshot(world, remoteSnapshot, playerChunkPos);
            applySnapshot(world, playerSnapshot, targetChunkPos);

            // Refresh lighting and clients
            world.getLightingProvider().checkBlock(playerChunk.getPos().getStartPos());
            world.getLightingProvider().checkBlock(remoteChunk.getPos().getStartPos());

            ChunkSwapMod.LOGGER.info("[ChunkSwap] Swapped chunk {} <-> {}", playerChunkPos, targetChunkPos);
            return true;

        } catch (Exception e) {
            ChunkSwapMod.LOGGER.error("[ChunkSwap] Error during chunk swap: ", e);
            return false;
        }
    }

    /**
     * Find a random, valid chunk far from the player's current chunk.
     */
    private static ChunkPos findRandomChunk(ServerWorld world, ChunkPos playerChunk) {
        int worldBorderChunks = (int) (world.getWorldBorder().getSize() / 2.0 / 16.0);
        int maxRadius = Math.min(MAX_RADIUS_CHUNKS, worldBorderChunks - 1);

        for (int attempt = 0; attempt < MAX_RETRY_ATTEMPTS; attempt++) {
            int offsetX = (RANDOM.nextInt(maxRadius * 2) - maxRadius);
            int offsetZ = (RANDOM.nextInt(maxRadius * 2) - maxRadius);

            // Ensure minimum distance from player chunk
            if (Math.abs(offsetX) < MIN_DISTANCE_CHUNKS && Math.abs(offsetZ) < MIN_DISTANCE_CHUNKS) {
                continue;
            }

            ChunkPos candidate = new ChunkPos(offsetX, offsetZ);

            // Check within world border
            if (!world.getWorldBorder().contains(candidate.getStartPos())) {
                continue;
            }

            return candidate;
        }
        return null;
    }

    // -------------------------------------------------------------------------
    // Snapshot system
    // -------------------------------------------------------------------------

    private static ChunkSnapshot snapshotChunk(ServerWorld world, WorldChunk chunk, ChunkPos pos) {
        ChunkSnapshot snapshot = new ChunkSnapshot();
        int startX = pos.getStartX();
        int startZ = pos.getStartZ();
        int minY = world.getBottomY();
        int maxY = world.getTopY();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    BlockPos bp = new BlockPos(startX + x, y, startZ + z);
                    BlockState state = world.getBlockState(bp);
                    snapshot.blocks.add(new BlockSnapshot(x, y, z, state));

                    BlockEntity be = world.getBlockEntity(bp);
                    if (be != null) {
                        NbtCompound nbt = be.createNbt(world.getRegistryManager());
                        snapshot.blockEntities.add(new BlockEntitySnapshot(x, y, z, nbt));
                    }
                }
            }
        }

        return snapshot;
    }

    private static void applySnapshot(ServerWorld world, ChunkSnapshot snapshot, ChunkPos targetPos) {
        int startX = targetPos.getStartX();
        int startZ = targetPos.getStartZ();

        // Clear existing block entities in target chunk first
        WorldChunk chunk = world.getChunk(targetPos.x, targetPos.z);

        for (BlockSnapshot bs : snapshot.blocks) {
            BlockPos bp = new BlockPos(startX + bs.localX, bs.y, startZ + bs.localZ);
            world.setBlockState(bp, bs.state, 2 | 16); // flag 2 = send to client, 16 = no re-render
        }

        for (BlockEntitySnapshot bes : snapshot.blockEntities) {
            BlockPos bp = new BlockPos(startX + bes.localX, bes.y, startZ + bes.localZ);
            BlockEntity be = world.getBlockEntity(bp);
            if (be != null) {
                be.read(bes.nbt, world.getRegistryManager());
                be.markDirty();
            }
        }
    }

    // -------------------------------------------------------------------------
    // Data holders
    // -------------------------------------------------------------------------

    private static class ChunkSnapshot {
        List<BlockSnapshot> blocks = new ArrayList<>();
        List<BlockEntitySnapshot> blockEntities = new ArrayList<>();
    }

    private static class BlockSnapshot {
        final int localX, y, localZ;
        final BlockState state;
        BlockSnapshot(int x, int y, int z, BlockState state) {
            this.localX = x; this.y = y; this.localZ = z; this.state = state;
        }
    }

    private static class BlockEntitySnapshot {
        final int localX, y, localZ;
        final NbtCompound nbt;
        BlockEntitySnapshot(int x, int y, int z, NbtCompound nbt) {
            this.localX = x; this.y = y; this.localZ = z; this.nbt = nbt;
        }
    }
}
