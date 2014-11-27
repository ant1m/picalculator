package com.oab.ateliers.queues.generator;

public class Point {
    public float x;
    public float y;

    public Point(float x, float y) {
        this.x = x;
        this.y = y;
    }

    public String toJson(){
        return "{\"x\":"+x+", \"y\":"+y+"}";
    }
}
