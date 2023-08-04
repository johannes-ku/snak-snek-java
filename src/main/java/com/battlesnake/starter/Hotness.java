package com.battlesnake.starter;

public class Hotness {
    public Coordinate coordinate;
    public byte depth;

    public Hotness(Coordinate coordinate, byte depth) {
        this.coordinate = coordinate;
        this.depth = depth;
    }
}
