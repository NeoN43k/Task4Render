package com.cgvsu.affine_transformations;

import com.cgvsu.math.Global;
import com.cgvsu.math.matrix.Matrix4f;

import java.util.ArrayDeque;

public class Rotate implements Affine{
    private float rotateX;
    private float rotateY;
    private float rotateZ;
    private Axis rotateAxis;
    private boolean isOneAxis;

    public Rotate(float x, float y, float z){
        rotateX = x;
        rotateY = y;
        rotateZ = z;
        isOneAxis = false;
    }
    public Rotate(float rotate, Axis axis){
       rotateX = rotate;
       rotateAxis = axis;
       isOneAxis = true;
    }
    private Matrix4f makeRotateMatrix(float rotate, Axis axis){
        Matrix4f rotateMatrix = new Matrix4f();
        int rowColumn = axis.getNumber();
        ArrayDeque<Float> values = new ArrayDeque<>();
        values.add((float) Math.cos(rotate));
        values.add(-(float) Math.sin(rotate));
        values.add((float) Math.sin(rotate));
        values.add((float) Math.cos(rotate));
        for (int x = 0; x < 3; x++){
            for (int y = 0; y < 3; y++){
                if ((y == x) && (x == rowColumn)){
                    rotateMatrix.setCell(y, x, 1);
                }
                if ((y != rowColumn) && (x != rowColumn)){
                    if (Math.abs(values.peek()) >= Global.eps) {
                        rotateMatrix.setCell(y, x, values.remove());
                    } else {
                        values.remove();
                    }
                }
            }
        }
        return rotateMatrix;
    }
    @Override
    public AbstractTransformation getTransformation() {
        if (isOneAxis){
           return new MatrixTransformation(makeRotateMatrix(rotateX, rotateAxis));
        } else {
            Matrix4f rotateMatrixX = makeRotateMatrix(rotateX, Axis.X);
            Matrix4f rotateMatrixY = makeRotateMatrix(rotateY, Axis.Y);
            Matrix4f rotateMatrixZ = makeRotateMatrix(rotateZ, Axis.Z);

            Matrix4f matrixXY = rotateMatrixX.mulMatrix(rotateMatrixY);
            Matrix4f matrixXYZ = matrixXY.mulMatrix(rotateMatrixZ);
            return new MatrixTransformation(matrixXYZ);
        }
    }
}
