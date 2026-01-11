package com.yourname.nighthorde;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/** Spawns a boss mob with boosted stats, glow, and red name. */
public final class BossFactory {

    private BossFactory() {}

    public static LivingEntity spawnBoss(ServerWorld world, BlockPos lodestone, NightHordeConfig cfg, WeightedMobPicker picker) {

        EntityType<?> bossType =
                "FORCED_MOB".equalsIgnoreCase(cfg.boss.howToChooseBoss)
                        ? picker.resolve(cfg.boss.forcedBossMobId)
                        : picker.pickEntityType(world);

        if (bossType == null) return null;

        var ent = bossType.create(world);
        if (!(ent instanceof LivingEntity boss)) return null;

        BlockPos spawn = lodestone.up();
        float yaw = world.getRandom().nextFloat() * 360f;
        boss.refreshPositionAndAngles(spawn.getX() + 0.5, spawn.getY(), spawn.getZ() + 0.5, yaw, 0f);

        if (cfg.boss.glowing) boss.setGlowing(true);

        applyStats(boss, cfg);

        Text name = Text.literal(cfg.boss.nameColorCode + cfg.boss.namePrefix + prettyName(bossType));
        boss.setCustomName(name);
        boss.setCustomNameVisible(true);

        if (world.isSpaceEmpty(boss)) {
            world.spawnEntity(boss);
            return boss;
        }
        return null;
    }

    private static void applyStats(LivingEntity boss, NightHordeConfig cfg) {
        var maxHealth = boss.getAttributeInstance(EntityAttributes.GENERIC_MAX_HEALTH);
        if (maxHealth != null) {
            double base = maxHealth.getBaseValue();
            double boosted = Math.max(1.0, base * cfg.boss.healthMultiplier);
            maxHealth.setBaseValue(boosted);
            boss.setHealth((float) boosted);
        }

        var attack = boss.getAttributeInstance(EntityAttributes.GENERIC_ATTACK_DAMAGE);
        if (attack != null) {
            double base = attack.getBaseValue();
            attack.setBaseValue(Math.max(0.0, base * cfg.boss.damageMultiplier));
        }
    }

    private static String prettyName(EntityType<?> type) {
        String id = type.toString();
        int colon = id.indexOf(':');
        String name = (colon >= 0) ? id.substring(colon + 1) : id;
        name = name.replace('_', ' ');
        return name.isEmpty() ? "Boss" : Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }
}
