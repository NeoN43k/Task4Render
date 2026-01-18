package com.cgvsu.affine_transformations;

import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector3f;

public interface AbstractTransformation {
    Matrix4f getMatrix();
    Vector3f execute(Vector3f v);
}
