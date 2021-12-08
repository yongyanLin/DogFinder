package com.example.dogfinder.Utils;


import java.util.Comparator;


public class DistanceComparator implements Comparator<Integer> {
    private final Double[] array;

    public DistanceComparator(Double[] array)
    {
        this.array = array;
    }

    public Integer[] createIndexArray()
    {
        Integer[] indexes = new Integer[array.length];
        for (int i = 0; i < array.length; i++)
        {
            indexes[i] = i;
        }
        return indexes;
    }

    @Override
    public int compare(Integer o1, Integer o2) {
        return array[o1].compareTo(array[o2]);
    }
}
