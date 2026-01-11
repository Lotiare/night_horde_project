package com.yourname.nighthorde;

import net.minecraft.entity.EntityType;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;

import java.util.ArrayList;
import java.util.List;

/** Picks mobs by weight from config. */
public final class WeightedMobPicker {

    private record Entry(String mobId, int weight) {}

    private final List<Entry> entries = new ArrayList<>();
    private int totalWeight = 0;

    public WeightedMobPicker(List<NightHordeConfig.MobEntry> table) {
        if (table == null) return;
        for (NightHordeConfig.MobEntry e : table) {
            if (e == null || e.mobId == null) continue;
            int w = Math.max(0, e.weight);
            if (w == 0) continue;
            entries.add(new Entry(e.mobId, w));
            totalWeight += w;
        }
    }

    public EntityType<?> resolve(String idString) {
        try {
            Identifier id = Identifier.of(idString);
            return Registries.ENTITY_TYPE.get(id);
        } catch (Exception ignored) {
            return null;
        }
    }

    public EntityType<?> pickEntityType(ServerWorld world) {
        if (entries.isEmpty() || totalWeight <= 0) return null;

        int roll = world.getRandom().nextInt(totalWeight) + 1;
        int sum = 0;
        for (Entry e : entries) {
            sum += e.weight;
            if (roll <= sum) return resolve(e.mobId);
        }
        return null;
    }
}
