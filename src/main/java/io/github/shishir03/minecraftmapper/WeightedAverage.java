package io.github.shishir03.minecraftmapper;

public class WeightedAverage {
    private final double[][] points;
    private final double z, x;

    public WeightedAverage(double[][] points, double z, double x) {
        this.points = points;
        this.z = z;
        this.x = x;
    }

    public double weight() {
        double[] p = weightPoints();
        double[] weights = weights();
        return p[0]*weights[0] + p[1]*weights[1] + p[2]*weights[2] + p[3]*weights[3];
    }

    private int[][] neighbors() {
        int[] neighbor1 = {(int)z, (int)x};
        int[] neighbor2 = {(int)z, (int)x + 1};
        int[] neighbor3 = {(int)z + 1, (int)x};
        int[] neighbor4 = {(int)z + 1, (int)x + 1};

        return new int[][] {neighbor1, neighbor2, neighbor3, neighbor4};
    }

    private double[] weights() {
        double[] current = {z, x};

        int[][] neighbors = neighbors();
        int[] neighbor1 = neighbors[0];
        int[] neighbor2 = neighbors[1];
        int[] neighbor3 = neighbors[2];
        int[] neighbor4 = neighbors[3];

        double weight1 = (1 - Math.abs(current[0] - neighbor1[0]))*(1 - Math.abs(current[1] - neighbor1[1]));
        double weight2 = (1 - Math.abs(current[0] - neighbor2[0]))*(1 - Math.abs(current[1] - neighbor2[1]));
        double weight3 = (1 - Math.abs(current[0] - neighbor3[0]))*(1 - Math.abs(current[1] - neighbor3[1]));
        double weight4 = (1 - Math.abs(current[0] - neighbor4[0]))*(1 - Math.abs(current[1] - neighbor4[1]));

        return new double[] {weight1, weight2, weight3, weight4};
    }

    public double[] weightPoints() {
        int[][] neighbors = neighbors();
        int[] neighbor1 = neighbors[0];
        int[] neighbor2 = neighbors[1];
        int[] neighbor3 = neighbors[2];
        int[] neighbor4 = neighbors[3];

        double p1 = points[neighbor1[0]][neighbor1[1]];
        double p2 = points[neighbor2[0]][neighbor2[1]];
        double p3 = points[neighbor3[0]][neighbor3[1]];
        double p4 = points[neighbor4[0]][neighbor4[1]];

        return new double[] {p1, p2, p3, p4};
    }
}
