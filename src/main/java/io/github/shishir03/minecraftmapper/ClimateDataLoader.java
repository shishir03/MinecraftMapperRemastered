package io.github.shishir03.minecraftmapper;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ClimateDataLoader {
    private final String param;
    private final double longMin, longMax, latMin, latMax;

    public ClimateDataLoader(String s, double longMin, double latMin, double longMax, double latMax) {
        param = s;
        this.longMin = longMin;
        this.latMin = latMin;
        this.longMax = longMax;
        this.latMax = latMax;
    }

    private String getClimateJSON() {
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

        return "";
    }

    // Possible params: "yly_pcpn", "yly_avgt"
    public double[][] loadClimateAvgs() {
        // JSON format: { data: [["1981", [[...]]], ["1982", [[...]]], ...] }
        String json = getClimateJSON();

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

        return new double[0][0];
    }
}
