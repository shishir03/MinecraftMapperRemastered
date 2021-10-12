package io.github.shishir03.minecraftmapper;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.io.*;
import java.net.URL;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

public class MapGenerator extends ChunkGenerator {
    private final double[] coords = coords(getSeed());

    private final double latMin = coords[0];
    private final double longMin = coords[1];
    private final double latMax = coords[2];
    private final double longMax = coords[3];

    private final long[][] elevations = loadData(latMin, longMin, latMax, longMax);

    @Override
    public ChunkData generateChunkData(World w, Random rand, int chunkX, int chunkZ, BiomeGrid bg) {
        ChunkData chunk = createChunkData(w);

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int worldX = chunkX*16 + x;
                int worldZ = chunkZ*16 + z;
                int height = 0;

                if(worldX >= 0 && worldZ <= 0 && worldX < elevations[0].length && worldZ > -elevations.length) {
                    height = (int)(Math.ceil(elevations[-worldZ][worldX]/50.0));
                }

                chunk.setBlock(x, 0, z, Material.BEDROCK);
                if(height <= 0) {
                    chunk.setBlock(x, 1, z, Material.WATER);
                    bg.setBiome(x, z, Biome.OCEAN);
                } else {
                    chunk.setBlock(x, height, z, Material.GRASS_BLOCK);

                    if(height < 8) bg.setBiome(x, z, Biome.PLAINS);
                    else if(height < 50) bg.setBiome(x, z, Biome.WOODED_HILLS);
                    else {
                        bg.setBiome(x, z, Biome.SNOWY_MOUNTAINS);
                        chunk.setBlock(x, height, z, Material.SNOW_BLOCK);
                    }


                    if(height > 1) chunk.setBlock(x, height - 1, z, Material.DIRT);
                    for(int h = height - 2; h > 0; h--) {
                        chunk.setBlock(x, h, z, Material.STONE);
                    }
                }
            }
        }

        return chunk;
    }

    private long[][] loadData(double latMin, double longMin, double latMax, double longMax) {
        // Each gridpoint is spaced 0.01 degrees apart for both latitude and longitude
        int numLatPoints = (int)Math.round((latMax - latMin)*100);
        int numLongPoints = (int)Math.round((longMax - longMin)*100);
        long[][] elevations = new long[numLatPoints][numLongPoints];

        double latitude = latMin;

        for(int i = 0; i < numLatPoints; i++) {
            ArrayList<String> urls = apiCalls(latitude, longMin, longMax);

            int j = 0;

            for(String u : urls) {
                try {
                    URL api = new URL(u);
                    Scanner input = new Scanner(api.openStream());
                    StringBuilder line = new StringBuilder();
                    while (input.hasNext()) line.append(input.nextLine());
                    String l = line.toString();

                    JSONObject obj = (JSONObject) new JSONParser().parse(l);

                    int numPoints = u.split(",").length - 1;
                    for(int k = 0; k < numPoints; k++) {
                        JSONObject output = (JSONObject) ((JSONArray) (obj.get("results"))).get(k);
                        long elevation = (long) output.get("elevation");
                        elevations[i][j] = elevation;
                        j++;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            latitude += 0.01;
        }

        return elevations;
    }

    // Returns an ArrayList of the appropriate api calls for a given line of latitude and a range of longitudes
    private ArrayList<String> apiCalls(double latitude, double longMin, double longMax) {
        ArrayList<String> apiCalls = new ArrayList<>();

        for(double startingLongitude = longMin; startingLongitude < longMax; startingLongitude++) {
            StringBuilder url = new StringBuilder("https://api.open-elevation.com/api/v1/lookup?locations=");
            String separator = "";

            for (double lg = startingLongitude; lg < Math.min(longMax, startingLongitude + 1); lg += 0.01) {
                url.append(separator);
                url.append(latitude).append(",").append(lg);
                separator = "|";
            }

            apiCalls.add(url.toString());
        }

        return apiCalls;
    }

    private String getSeed() {
        // jarPath = "~/Games/minecraftmapper_test/plugins/jarnameorwhatever.jar"
        // what we want: "~/Games/minecraftmapper_test"
        try {
            String jarPath = MapGenerator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
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
