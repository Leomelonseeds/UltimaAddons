package com.leomelonseeds.ultimaaddons.utils;

import java.util.NavigableMap;
import java.util.Random;
import java.util.TreeMap;

public class RandomCollection<E> {
    private final NavigableMap<Double, E> map = new TreeMap<>();
    private final Random random;

    public RandomCollection() {
        random = new Random();
    }

    public void add(double weight, E result) {
        if (weight <= 0) return;
        map.put(weight * random.nextDouble(), result);
    }

    public E next() {
        return map.pollLastEntry().getValue();
    }
}
