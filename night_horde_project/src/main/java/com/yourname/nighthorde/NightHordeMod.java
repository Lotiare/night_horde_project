package com.yourname.nighthorde;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import java.nio.file.Path;

/**
 * Night Horde (Fabric 1.21.11) - SERVER SIDE.
 *
 * Non-coders: edit /config/night_horde.json only.
 */
public final class NightHordeMod implements ModInitializer {

    public static final String MOD_ID = "night_horde";

    private HordeEngine engine;

    @Override
    public void onInitialize() {
        // Load config after server starts (we need run directory).
        ServerLifecycleEvents.SERVER_STARTED.register(this::onServerStarted);

        // Tick logic.
        ServerTickEvents.START_SERVER_TICK.register(this::onServerTick);

        // Commands:
        // /nighthorde reload
        // /nighthorde test
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                NightHordeCommands.register(dispatcher, this)
        );
    }

    private void onServerStarted(MinecraftServer server) {
        Path configDir = server.getRunDirectory().toPath().resolve("config");
        reloadConfig(configDir);
    }

    private void onServerTick(MinecraftServer server) {
        if (engine == null) return;
        engine.tick(server);
    }

    /** Reloads /config/night_horde.json (creates it if missing). */
    public void reloadConfig(Path configDir) {
        NightHordeConfig cfg = ConfigIO.loadOrCreate(configDir);
        this.engine = new HordeEngine(cfg);
    }

    /** Used by /nighthorde test */
    public boolean tryForceTestRaid() {
        return engine != null && engine.forceTestRaid();
    }
}
