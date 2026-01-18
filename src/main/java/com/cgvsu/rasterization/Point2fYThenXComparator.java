package com.cgvsu.rasterization;

import com.cgvsu.math.point.Point2f;
import java.util.Comparator;

public class Point2fYThenXComparator implements Comparator<Point2f> {
    @Override
    public int compare(Point2f d1, Point2f d2) {
        if (d1.y - d2.y != 0){
            return (int) (d1.y - d2.y);
        } else
        {
            return (int) (d1.x - d2.x);
        }
    }
}