package com.cgvsu.affine_transformations;

import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector3f;
import com.cgvsu.math.vector.Vector4f;

public class MatrixTransformation implements AbstractTransformation{
    private Matrix4f matrix;

    public MatrixTransformation(Matrix4f matrix) {
        this.matrix = matrix;
    }

    @Override
    public Matrix4f getMatrix() {
        return matrix;
    }

    @Override
    public Vector3f execute(Vector3f vector) {
        Vector4f temp = matrix.mulVector(new Vector4f(vector.getX(), vector.getY(), vector.getZ(), 1));
        if (temp.getW() != 1 && temp.getW() != 0){
            return new Vector3f(temp.getX()/temp.getW(), temp.getY()/temp.getW(), temp.getZ()/temp.getW());
        } else return new Vector3f(temp.getX(), temp.getY(), temp.getZ());
    }
}
