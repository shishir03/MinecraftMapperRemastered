package io.github.shishir03.minecraftmapper;

import ucar.ma2.Array;
import ucar.nc2.NetcdfFile;
import ucar.nc2.Variable;

public class ElevationDataLoader {
    private final Variable elevation;
    private NetcdfFile f;

    public ElevationDataLoader() {
        try {
            String jarPath = MapGenerator.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            String separator = System.getProperty("file.separator");
            String filepath = jarPath.substring(0, jarPath.lastIndexOf(separator) + 1);
            f = NetcdfFile.open(filepath + "GEBCO_2021.nc");
        } catch(Exception e) {
            e.printStackTrace();
        }

        assert f != null;
        elevation = f.findVariable("elevation");
    }

    public short getElevation(double latitude, double longitude) {
        try {
            Array a = elevation.read(new int[] {(int)Math.floor((latitude + 90) * 240), (int)Math.floor((longitude + 180) * 240)}, new int[] {1, 1});
            return (short)(Math.ceil(a.getShort(0)/100.0) + 128);
        } catch(Exception e) {
            e.printStackTrace();
        }

        return (short)-32768;
    }

    public void close() {
        try {
            f.close();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
