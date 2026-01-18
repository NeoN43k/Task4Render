package com.cgvsu.affine_transformations;

import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.vector.Vector3f;

import java.util.ArrayList;
import java.util.NoSuchElementException;

public class CompositeAffine implements Affine {
    ArrayList<Affine> transformations = new ArrayList<>();
    public void add (Affine transformation){
        transformations.add(transformation);
    }

    @Override
    public AbstractTransformation getTransformation() {
        if (transformations.isEmpty()){
            throw new NoSuchElementException();
        }
        Matrix4f output = transformations.get(0).getTransformation().getMatrix();
        if (transformations.size()>1){
            for (int i = 1; i < transformations.size(); i++){
                output = output.mulMatrix(transformations.get(i).getTransformation().getMatrix());
            }
        }
        return new MatrixTransformation(output);
    }
    public ArrayList<Vector3f> execute(ArrayList<Vector3f> vertices){
        ArrayList<Vector3f> output = new ArrayList<>();
        for (int i = 0; i < vertices.size(); i++){
            output.add(vertices.get(i));
            for (Affine transformation: transformations){
                output.set(i, transformation.getTransformation().execute(output.get(i)));
            }
        }
        return output;
    }
}
