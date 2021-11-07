package io.github.shishir03.minecraftmapper;

import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

public final class MinecraftMapper extends JavaPlugin {
    private double latMin, longMin, latMax, longMax;

    @Override
    public void onEnable() {
        // Plugin startup logic
        double[] coords = coords(getSeed());
        latMin = coords[0];
        longMin = coords[1];
        latMax = coords[2];
        longMax = coords[3];
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    @Override
    public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
        return new MapGenerator(latMin, longMin, latMax, longMax);
    }

    // Obtains the seed value from server.properties
    private String getSeed() {
        try {
            String jarPath = MinecraftMapper.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            jarPath = jarPath.substring(0, jarPath.length() - 1);

            String separator = System.getProperty("file.separator");
            String propertiesPath = jarPath.substring(0, jarPath.lastIndexOf(separator,
                    jarPath.lastIndexOf(separator) - 1));
            InputStream input = new FileInputStream(propertiesPath + separator + "server.properties");

            Properties p = new Properties();
            p.load(input);
            return p.getProperty("level-seed");
        } catch(Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    // Converts the String provided in the world seed to an array of coords
    private double[] coords(String seed) {
        String[] numbers = seed.split(",");
        double[] coords = new double[4];

        if(numbers.length != 4) throw new IllegalArgumentException("Invalid world seed");
        else {
            for(int i = 0; i < 4; i++) coords[i] = Double.parseDouble(numbers[i]);
        }

        return coords;
    }
}
