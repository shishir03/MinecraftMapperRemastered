package io.github.shishir03.minecraftmapper;

import org.bukkit.Material;
import org.bukkit.block.Biome;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.*;
import org.bukkit.World;
import org.bukkit.generator.ChunkGenerator;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.Random;

public class MapGenerator extends ChunkGenerator {
    private final double[] coords = coords(getSeed());
    // private final long[][] elevations = loadElevationData();
    private final double[][] precipData = loadClimateAvgs("yly_pcpn");
    private final double[][] tempData = loadClimateAvgs("yly_avgt");

    @Override
    public ChunkData generateChunkData(World w, Random rand, int chunkX, int chunkZ, BiomeGrid bg) {
        ChunkData chunk = createChunkData(w);

        double latMin = coords[0];
        double longMin = coords[1];
        double latMax = coords[2];
        double longMax = coords[3];

        NetcdfFile f = null;
        try {
            String jarPath = MapGenerator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            String separator = System.getProperty("file.separator");
            String filepath = jarPath.substring(0, jarPath.lastIndexOf(separator) + 1);
            f = NetcdfFile.open(filepath + "GEBCO_2021.nc");
        } catch(Exception e) {
            e.printStackTrace();
        }

        assert f != null;
        Variable lat = f.findVariable("lat");
        Variable lon = f.findVariable("lon");
        Variable elevation = f.findVariable("elevation");

        int rank = elevation.getRank();
        int[] shape = elevation.getShape();
        int nRows = shape[0];
        int nCols = shape[1];

        int[] readOrigin = new int[rank];
        int[] readShape = new int[rank];

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
                    int row = (int)Math.floor((currentLat + 90) * 240);
                    int col = (int)Math.floor((currentLong + 180) * 240);

                    readOrigin[0] = row;
                    readOrigin[1] = col;
                    readShape[0] = 1;
                    readShape[1] = 1;

                    Array a = null;
                    try {
                        a = elevation.read(readOrigin, readShape);
                    } catch(Exception e) {
                        e.printStackTrace();
                    }

                    assert a != null;
                    height = (short)(Math.ceil(a.getShort(0)/100.0) + 128);

                    int[] neighbor1 = {(int)(-worldZ*0.1), (int)(worldX*0.1)};
                    int[] neighbor2 = {(int)(-worldZ*0.1), (int)(worldX*0.1) + 1};
                    int[] neighbor3 = {(int)(-worldZ*0.1) + 1, (int)(worldX*0.1)};
                    int[] neighbor4 = {(int)(-worldZ*0.1) + 1, (int)(worldX*0.1) + 1};

                    double[] current = {-worldZ*0.1, worldX*0.1};

                    double weight1 = (1 - Math.abs(current[0] - neighbor1[0]))*(1 - Math.abs(current[1] - neighbor1[1]));
                    double weight2 = (1 - Math.abs(current[0] - neighbor2[0]))*(1 - Math.abs(current[1] - neighbor2[1]));
                    double weight3 = (1 - Math.abs(current[0] - neighbor3[0]))*(1 - Math.abs(current[1] - neighbor3[1]));
                    double weight4 = (1 - Math.abs(current[0] - neighbor4[0]))*(1 - Math.abs(current[1] - neighbor4[1]));

                    assert tempData != null;
                    double t1 = tempData[neighbor1[0]][neighbor1[1]];
                    double t2 = tempData[neighbor2[0]][neighbor2[1]];
                    double t3 = tempData[neighbor3[0]][neighbor3[1]];
                    double t4 = tempData[neighbor4[0]][neighbor4[1]];

                    assert precipData != null;
                    double p1 = precipData[neighbor1[0]][neighbor1[1]];
                    double p2 = precipData[neighbor2[0]][neighbor2[1]];
                    double p3 = precipData[neighbor3[0]][neighbor3[1]];
                    double p4 = precipData[neighbor4[0]][neighbor4[1]];

                    if(p1 < 0 || p2 < 0 || p3 < 0 || p4 < 0) {
                        currentTemp = tempData[(int)Math.round(-worldZ*0.1)][(int)Math.round(worldX*0.1)];
                        currentPrecip = precipData[(int)Math.round(-worldZ*0.1)][(int)Math.round(worldX*0.1)];
                    } else {
                        currentTemp = weight1*t1 + weight2*t2 + weight3*t3 + weight4*t4;
                        currentPrecip = weight1*p1 + weight2*p2 + weight3*p3 + weight4*p4;
                    }
                }

                boolean noData = currentPrecip < 0;

                chunk.setBlock(x, 0, z, Material.BEDROCK);
                if(noData) {
                    if(height <= 128) {
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
                    }
                } else {
                    if(height <= 0) chunk.setBlock(x, 1, z, Material.WATER);
                    else chunk.setBlock(x, height, z, Material.GRASS_BLOCK);

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
                            if(height > 0) chunk.setBlock(x, height, z, Material.SAND);
                        } else if(currentPrecip < 30) bg.setBiome(x, z, Biome.SAVANNA);
                        else bg.setBiome(x, z, Biome.JUNGLE);
                    }
                }

                if(height > 1) chunk.setBlock(x, height - 1, z, Material.DIRT);
                for(int h = height - 2; h > 0; h--) {
                    chunk.setBlock(x, h, z, Material.STONE);
                }
            }
        }

        try {
            f.close();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return chunk;
    }

    // Obtains the seed value from server.properties
    private String getSeed() {
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

    private String getClimateJSON(String param) {
        double latMin = coords[0];
        double longMin = coords[1];
        double latMax = coords[2];
        double longMax = coords[3];

        try {
            URL u = new URL("http://data.rcc-acis.org/GridData");
            HttpURLConnection c = (HttpURLConnection) u.openConnection();
            c.setRequestMethod("POST");
            c.setRequestProperty("Content-Type", "application/json; utf-8");
            c.setRequestProperty("Accept", "application/json");
            c.setDoOutput(true);

            OutputStream o = c.getOutputStream();
            String params = "{\"bbox\":\"" + longMin + "," + latMin + "," + longMax + "," + latMax +
                    "\",\"sdate\":\"1981\",\"edate\":\"2010\",\"grid\":\"21\",\"elems\":\"" + param + "\"}";
            byte[] input = params.getBytes(StandardCharsets.UTF_8);
            o.write(input, 0, input.length);
            o.close();

            BufferedReader b = new BufferedReader(new InputStreamReader(c.getInputStream(), StandardCharsets.UTF_8));
            StringBuilder response = new StringBuilder();
            String line = null;

            while((line = b.readLine()) != null) response.append(line.trim());
            b.close();

            return response.toString();
        } catch(Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    // Possible params: "yly_pcpn", "yly_avgt"
    private double[][] loadClimateAvgs(String param) {
        // JSON format: { data: [["1981", [[...]]], ["1982", [[...]]], ...] }
        String json = getClimateJSON(param);

        try {
            JSONObject obj = (JSONObject) new JSONParser().parse(json);

            JSONArray years = (JSONArray) (obj.get("data"));
            JSONArray output = (JSONArray) ((JSONArray) years.get(0)).get(1);
            int innerSize = ((JSONArray) output.get(0)).size();
            double[][] climateGrid = new double[output.size()][innerSize];

            for(int i = 0; i < 30; i++) {
                output = (JSONArray) ((JSONArray) years.get(i)).get(1);

                for(int j = 0; j < output.size(); j++) {
                    JSONArray innerOutput = (JSONArray) output.get(j);
                    for(int k = 0; k < innerSize; k++) {
                        try {
                            climateGrid[j][k] += (Double) innerOutput.get(k);
                        } catch(ClassCastException e) {
                            climateGrid[j][k] = -30000.0;
                        }
                    }
                }
            }

            for(int i = 0; i < output.size(); i++) {
                for(int j = 0; j < innerSize; j++) {
                    double avg = climateGrid[i][j] / 30;
                    double roundOff = Math.round(avg * 100.0) / 100.0;
                    climateGrid[i][j] = roundOff;
                }
            }

            return climateGrid;
        } catch(ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
