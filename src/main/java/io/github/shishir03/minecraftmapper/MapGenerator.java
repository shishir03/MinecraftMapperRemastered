package io.github.shishir03.minecraftmapper;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import java.util.Random;

public class MapGenerator extends ChunkGenerator {
    private final double latMin, longMin, latMax, longMax;
    private final double[][] precipData, tempData;
    private final ElevationDataLoader edl;

    public MapGenerator(double latMin, double longMin, double latMax, double longMax, ElevationDataLoader e) {
        this.latMin = latMin;
        this.latMax = latMax;
        this.longMin = longMin;
        this.longMax = longMax;

        edl = e;

        ClimateDataLoader cdlPrecip = new ClimateDataLoader("yly_pcpn", longMin, latMin, longMax, latMax);
        ClimateDataLoader cdlTemp = new ClimateDataLoader("yly_avgt", longMin, latMin, longMax, latMax);

        precipData = cdlPrecip.loadClimateAvgs();
        tempData = cdlTemp.loadClimateAvgs();
    }

    @Override
    public ChunkData generateChunkData(World w, Random rand, int chunkX, int chunkZ, BiomeGrid bg) {
        ChunkData chunk = createChunkData(w);

        for(int x = 0; x < 16; x++) {
            for(int z = 0; z < 16; z++) {
                int worldX = chunkX*16 + x;
                int worldZ = chunkZ*16 + z;
                short height = 0;

                double currentTemp = -999;
                double currentPrecip = -999;

                double currentLat = latMin - worldZ/240.0;
                double currentLong = longMin + worldX/240.0;

                if(currentLat >= latMin && currentLat < latMax && currentLong >= longMin && currentLong < longMax) {
                    height = (short)(Math.ceil(edl.getElevation(currentLat, currentLong)/100.0) + 128);

                    WeightedAverage tempWeights = new WeightedAverage(tempData, -worldZ*0.1 + 1, worldX*0.1);
                    WeightedAverage precipWeights = new WeightedAverage(precipData, -worldZ*0.1 + 1, worldX*0.1);

                    double[] wp = precipWeights.weightPoints();
                    if(wp[0] < 0 || wp[1] < 0 || wp[2] < 0 || wp[3] < 0) {
                        currentTemp = tempData[(int)Math.round(-worldZ*0.1)][(int)Math.round(worldX*0.1)];
                        currentPrecip = precipData[(int)Math.round(-worldZ*0.1)][(int)Math.round(worldX*0.1)];
                    } else {
                        currentTemp = tempWeights.weight();
                        currentPrecip = precipWeights.weight();
                    }
                }

                boolean noData = currentPrecip < 0;

                chunk.setBlock(x, 0, z, Material.BEDROCK);
                if(noData) {
                    if(height <= 128) bg.setBiome(x, z, Biome.OCEAN);
                    else {
                        chunk.setBlock(x, height, z, Material.GRASS_BLOCK);
                        if(height < 136) bg.setBiome(x, z, Biome.PLAINS);
                        else if(height < 178) bg.setBiome(x, z, Biome.WOODED_HILLS);
                        else {
                            bg.setBiome(x, z, Biome.SNOWY_MOUNTAINS);
                            chunk.setBlock(x, height, z, Material.SNOW_BLOCK);
                        }
                    }
                } else {
                    if(height > 128) chunk.setBlock(x, height, z, Material.GRASS_BLOCK);
                    if(currentTemp < 45) {
                        bg.setBiome(x, z, Biome.SNOWY_MOUNTAINS);
                        if(height > 0) chunk.setBlock(x, height, z, Material.SNOW_BLOCK);
                    } else if(currentTemp < 70) {
                        if(currentPrecip < 8) {
                            bg.setBiome(x, z, Biome.DESERT);
                            if(height > 0) chunk.setBlock(x, height, z, Material.SAND);
                        } else if(currentPrecip < 15) bg.setBiome(x, z, Biome.SAVANNA);
                        else if(currentPrecip < 30) bg.setBiome(x, z, Biome.PLAINS);
                        else bg.setBiome(x, z, Biome.FOREST);
                    } else {
                        if(currentPrecip < 15) {
                            bg.setBiome(x, z, Biome.DESERT);
                            chunk.setBlock(x, height, z, Material.SAND);
                        } else if(currentPrecip < 30) bg.setBiome(x, z, Biome.SAVANNA);
                        else bg.setBiome(x, z, Biome.JUNGLE);
                    }
                }

                if(height > 128) chunk.setBlock(x, height - 1, z, Material.DIRT);
                for(int h = height - 2; h > 0; h--) chunk.setBlock(x, h, z, Material.STONE);
                if(height <= 128) {
                    chunk.setBlock(x, Math.max(height, 1), z, Material.SAND);
                    chunk.setBlock(x, Math.max(height - 1, 1), z, Material.SAND);
                    for(int h = 129; h > height; h--) chunk.setBlock(x, h, z, Material.WATER);
                }
            }
        }

        return chunk;
    }
}
