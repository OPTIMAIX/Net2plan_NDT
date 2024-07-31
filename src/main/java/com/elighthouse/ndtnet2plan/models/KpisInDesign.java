package com.elighthouse.ndtnet2plan.models;

public class KpisInDesign {
    public double[][] offeredTrafficMatrixInGbps;
    public double[][] carriedTrafficMatrixInGbps;
    public double[][] blockedTrafficMatrixInGbps;
    public double[][] lostTrafficMatrixInGbps;
    public double[][] capacityMatrixInGbps;
    public double[][] latencyMatrixInMs;

    public KpisInDesign(int size) {
        this.offeredTrafficMatrixInGbps = new double[size][size];
        this.carriedTrafficMatrixInGbps = new double[size][size];
        this.blockedTrafficMatrixInGbps = new double[size][size];
        this.lostTrafficMatrixInGbps = new double[size][size];
        this.capacityMatrixInGbps = new double[size][size];
        this.latencyMatrixInMs = new double[size][size];
    }
}
