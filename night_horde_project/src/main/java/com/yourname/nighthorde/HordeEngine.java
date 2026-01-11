package com.yourname.nighthorde;

import net.minecraft.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main logic:
 * - Rolls once at night start in overworld
 * - Requirements:
 *   - at least one online player who is near a lodestone AND above Y threshold
 * - 2% base chance; doubles if 2+ players qualify
 * - Spawns 15-50 hostile mobs near the lodestone from a weighted mob table
 * - Optional boss with top-screen boss health bar near it
 */
public final class HordeEngine {

    private final NightHordeConfig cfg;
    private final WeightedMobPicker picker;
    private final BossBarTracker bossBars;

    private long lastProcessedDay = -1;
    private ServerWorld lastWorld = null;

    public HordeEngine(NightHordeConfig cfg) {
        this.cfg = cfg;
        this.picker = new WeightedMobPicker(cfg.mobTable);
        this.bossBars = new BossBarTracker(cfg);
    }

    public void tick(MinecraftServer server) {
        ServerWorld world = server.getOverworld();
        if (world == null) return;

        lastWorld = world;

        // Keep boss bars updated
        bossBars.tick(world);

        long timeOfDay = world.getTimeOfDay() % 24000L;
        long dayNumber = world.getTimeOfDay() / 24000L;

        if (timeOfDay != (long) cfg.raid.nightStartTick) return;
        if (lastProcessedDay == dayNumber) return;
        lastProcessedDay = dayNumber;

        List<QualifiedPlayer> qualified = findQualifiedPlayers(world);
        if (qualified.isEmpty()) return;

        double chance = cfg.raid.baseNightChancePercent;
        if (cfg.raid.doubleChanceIf2OrMoreQualifiedPlayers && qualified.size() >= 2) chance *= 2.0;

        if (world.getRandom().nextDouble() * 100.0 > chance) return;

        triggerRaid(world, qualified);
    }

    /** /nighthorde test uses this */
    public boolean forceTestRaid() {
        if (lastWorld == null) return false;
        List<QualifiedPlayer> qualified = findQualifiedPlayers(lastWorld);
        if (qualified.isEmpty()) return false;
        triggerRaid(lastWorld, qualified);
        return true;
    }

    private void triggerRaid(ServerWorld world, List<QualifiedPlayer> qualified) {
        Random rng = world.getRandom();
        QualifiedPlayer anchor = qualified.get(rng.nextInt(qualified.size()));
        BlockPos lodestone = anchor.nearestLodestone;

        announce(world, qualified);

        int count = randInt(rng, cfg.raid.minMobs, cfg.raid.maxMobs);

        boolean spawnBoss = cfg.boss.enabled && (rng.nextDouble() * 100.0 <= cfg.boss.chancePerRaidPercent);

        spawnHorde(world, lodestone, count, spawnBoss);
    }

    private void announce(ServerWorld world, List<QualifiedPlayer> qualified) {
        Random rng = world.getRandom();
        String msg = cfg.chat.messages.get(rng.nextInt(cfg.chat.messages.size()));
        Text t = Text.literal(cfg.chat.messageColorCode + msg);

        if ("QUALIFIED".equalsIgnoreCase(cfg.chat.announceTo)) {
            for (QualifiedPlayer qp : qualified) qp.player.sendMessage(t, false);
        } else {
            for (ServerPlayerEntity p : world.getPlayers()) p.sendMessage(t, false);
        }
    }

    private List<QualifiedPlayer> findQualifiedPlayers(ServerWorld world) {
        List<QualifiedPlayer> out = new ArrayList<>();
        for (ServerPlayerEntity p : world.getPlayers()) {
            if (p.isSpectator()) continue;

            BlockPos pos = p.getBlockPos();
            if (pos.getY() <= cfg.raid.minY) continue;

            BlockPos lodestone = findNearestLodestone(world, pos, cfg.raid.lodestoneRadiusBlocks);
            if (lodestone == null) continue;

            out.add(new QualifiedPlayer(p, lodestone));
        }
        return out;
    }

    private BlockPos findNearestLodestone(ServerWorld world, BlockPos center, int radius) {
        BlockPos nearest = null;
        double best = Double.MAX_VALUE;

        BlockPos.Mutable m = new BlockPos.Mutable();
        int r = Math.max(1, radius);

        for (int dx = -r; dx <= r; dx++) {
            for (int dy = -r; dy <= r; dy++) {
                for (int dz = -r; dz <= r; dz++) {
                    m.set(center.getX() + dx, center.getY() + dy, center.getZ() + dz);
                    if (world.getBlockState(m).isOf(Blocks.LODESTONE)) {
                        double d2 = m.getSquaredDistance(center);
                        if (d2 < best) {
                            best = d2;
                            nearest = m.toImmutable();
                        }
                    }
                }
            }
        }
        return nearest;
    }

    private void spawnHorde(ServerWorld world, BlockPos lodestone, int totalMobs, boolean includeBoss) {
        Random rng = world.getRandom();

        if (includeBoss) {
            var boss = BossFactory.spawnBoss(world, lodestone, cfg, picker);
            if (boss != null) bossBars.registerBoss(boss);
        }

        for (int i = 0; i < totalMobs; i++) {
            var type = picker.pickEntityType(world);
            if (type == null) continue;

            for (int attempt = 0; attempt < cfg.raid.maxSpawnAttemptsPerMob; attempt++) {
                BlockPos spawn = findSpawnSurfaceNear(world, lodestone, cfg.raid.spawnRadiusAroundLodestoneBlocks, rng);
                var e = type.create(world);
                if (e == null) continue;

                e.refreshPositionAndAngles(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5,
                        rng.nextFloat() * 360f, 0f);

                if (!world.isSpaceEmpty(e)) continue;

                world.spawnEntity(e);
                break;
            }
        }
    }

    private BlockPos findSpawnSurfaceNear(ServerWorld world, BlockPos center, int radius, Random rng) {
        int r = Math.max(2, radius);
        int x = center.getX() + rng.nextInt(r * 2 + 1) - r;
        int z = center.getZ() + rng.nextInt(r * 2 + 1) - r;
        BlockPos top = world.getTopPosition(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, new BlockPos(x, 0, z));
        return top.up();
    }

    private static int randInt(Random rng, int min, int max) {
        int a = Math.min(min, max);
        int b = Math.max(min, max);
        return a + rng.nextInt(b - a + 1);
    }

    private record QualifiedPlayer(ServerPlayerEntity player, BlockPos nearestLodestone) {}
}
