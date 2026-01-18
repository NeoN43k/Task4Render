package com.cgvsu.affine_transformations;

import com.cgvsu.math.matrix.Matrix4f;

public class Transfer implements Affine{
    private float transferX;
    private float transferY;
    private float transferZ;
    public Transfer(float x, float y, float z) {
        transferX = x;
        transferY = y;
        transferZ = z;
    }
    @Override
    public AbstractTransformation getTransformation() {
        Matrix4f transferMatrix = new Matrix4f(true);
        transferMatrix.setCell(0,3, transferX);
        transferMatrix.setCell(1,3, transferY);
        transferMatrix.setCell(2,3, transferZ);
        return new MatrixTransformation(transferMatrix);
    }
}
