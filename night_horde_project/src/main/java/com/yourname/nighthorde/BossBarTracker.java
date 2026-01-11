package com.yourname.nighthorde;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.boss.ServerBossBar;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/** Shows a Wither-style top-screen boss bar when players are near the boss. */
public final class BossBarTracker {

    private record ActiveBoss(UUID uuid, ServerBossBar bar) {}

    private final NightHordeConfig cfg;
    private final List<ActiveBoss> active = new ArrayList<>();

    public BossBarTracker(NightHordeConfig cfg) {
        this.cfg = cfg;
    }

    public void registerBoss(LivingEntity boss) {
        if (!cfg.bossBar.enabled) return;

        ServerBossBar bar = new ServerBossBar(
                boss.getDisplayName(),
                BossBar.Color.RED,
                BossBar.Style.PROGRESS
        );
        bar.setVisible(true);
        bar.setDarkenSky(cfg.bossBar.darkenSky);
        bar.setDragonMusic(cfg.bossBar.playMusic);

        active.add(new ActiveBoss(boss.getUuid(), bar));
    }

    public void tick(ServerWorld world) {
        if (!cfg.bossBar.enabled || active.isEmpty()) return;

        int r = Math.max(8, cfg.bossBar.showWithinBlocks);
        double r2 = r * (double) r;

        Iterator<ActiveBoss> it = active.iterator();
        while (it.hasNext()) {
            ActiveBoss ab = it.next();

            var e = world.getEntity(ab.uuid());
            if (!(e instanceof LivingEntity boss) || boss.isDead()) {
                ab.bar().clearPlayers();
                it.remove();
                continue;
            }

            ab.bar().setName(boss.getDisplayName());

            float max = boss.getMaxHealth();
            float hp = boss.getHealth();
            ab.bar().setPercent(max <= 0f ? 0f : (hp / max));

            for (ServerPlayerEntity p : world.getPlayers()) {
                if (p.squaredDistanceTo(boss) <= r2) ab.bar().addPlayer(p);
                else ab.bar().removePlayer(p);
            }
        }
    }
}
