package io.github.shishir03.minecraftmapper;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

public final class MinecraftMapper extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
       return new MapGenerator();
    }
}
