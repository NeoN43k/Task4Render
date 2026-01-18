package com.cgvsu.affine_transformations;

import com.cgvsu.math.matrix.Matrix4f;

public class Scale implements Affine{
    private float multiplierX;
    private float multiplierY;
    private float multiplierZ;


    public Scale(float x, float y,float z) {
        multiplierX = x;
        multiplierY = y;
        multiplierZ = z;
    }
    @Override
    public AbstractTransformation getTransformation() {
        Matrix4f scaleMatrix = new Matrix4f();
        scaleMatrix.setCell(0,0, multiplierX);
        scaleMatrix.setCell(1,1, multiplierY);
        scaleMatrix.setCell(2,2, multiplierZ);
        return new MatrixTransformation(scaleMatrix);
    }
}
