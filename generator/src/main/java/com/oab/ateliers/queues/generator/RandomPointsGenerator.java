package com.oab.ateliers.queues.generator;

import java.util.Random;

public class RandomPointsGenerator {

    private final Random random = new Random();

    public Point generate() {
           return new Point(random.nextFloat(), random.nextFloat());
       }
}
