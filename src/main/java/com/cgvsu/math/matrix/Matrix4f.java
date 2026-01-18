package com.cgvsu.math.matrix;
import com.cgvsu.math.vector.Vector4f;
import com.cgvsu.math.vector.Vector3f;

public class Matrix4f {
    private float[][] matrix;

    public Matrix4f() {
        this.matrix = new float[4][4];
    }

    public Matrix4f(boolean isUnitMatrix) {
        if (isUnitMatrix) {
            this.matrix = new float[][]{
                    {1f, 0, 0, 0},
                    {0, 1f, 0, 0},
                    {0, 0, 1f, 0},
                    {0, 0, 0, 1f}
            };
        } else {
            this.matrix = new float[4][4];
        }
    }

    public Matrix4f(float[][] matrix) {
        if (matrix.length != 4 || matrix[0].length != 4) {
            throw new IllegalArgumentException("Предоставленная матрица должна быть матрицей 4 на 4");
        }
        this.matrix = matrix;
    }

    public Matrix4f(float[] array) {
        // Проверяем, что массив содержит ровно 16 элементов
        if (array.length != 16) {
            throw new IllegalArgumentException("Массив должен содержать ровно 16 элементов.");
        }

        this.matrix = new float[4][4];

        // Заполняем матрицу значениями из массива
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.matrix[i][j] = array[i * 4 + j];
            }
        }
    }

    public Matrix4f(Matrix4f other) {
        this.matrix = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.matrix[i][j] = other.getCell(i, j);
            }
        }
    }
    public float[][] getMatrix() {
        return matrix;
    }

    public float getCell(int row, int col) {
        return matrix[row][col];
    }

    public void setCell (int row, int col, float value){
        matrix[row][col] = value;
    }

    public boolean equals (Matrix4f matrix4f){
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                if (matrix[i][j] != matrix4f.getCell(i,j)){
                    return false;
                }
            }
        }
        return true;
    }

    public void printMatrix() {
        System.out.println("Matrix: ");
        for (float[] floats : matrix) {
            for (int j = 0; j < matrix[0].length; j++) {
                System.out.print(floats[j] + " ");
            }
            System.out.println();
        }
    }

    public Matrix4f sum(Matrix4f matrix4f) {
        if (matrix4f.getMatrix().length != 4 || matrix4f.getMatrix()[0].length != 4) {
            throw new IllegalArgumentException();
        }
        float[][] values = new float[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                values[i][j] = matrix[i][j] + matrix4f.getCell(i, j);
            }
        }
        return new Matrix4f(values);
    }

    public Matrix4f sub(Matrix4f matrix4f) {
        if (matrix4f.getMatrix().length != 4 || matrix4f.getMatrix()[0].length != 4) {
            throw new IllegalArgumentException("Предоставленная матрица должна быть матрицей 4 на 4.");
        }
        float[][] values = new float[matrix.length][matrix[0].length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                values[i][j] = matrix[i][j] - matrix4f.getCell(i, j);
            }
        }
        return new Matrix4f(values);
    }

    public Matrix4f transposition() {
        float[][] res = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                res[j][i] = matrix[i][j];
            }
        }
        return new Matrix4f(res);
    }

    // умножает матрицу на вектор-столбец
    public Vector4f mulVector(Vector4f vectorCol) {
        if (vectorCol == null) {
            throw new NullPointerException("Предоставленный вектор не может быть нулевым");
        }

        float[] values = new float[4];
        for (int i = 0; i < 4; i++) {
            values[i] = 0;
            for (int j = 0; j < 4; j++) {
                values[i] += (matrix[i][j] * vectorCol.get(j));
            }
        }
        return new Vector4f(values[0], values[1], values[2], values[3]);
    }

    // умножает матрицу на вектор-столбец c нормировкой по W координате
    public Vector3f mulVectorDivW(Vector3f vectorCol3f) {
        if (vectorCol3f == null) {
            throw new NullPointerException("Предоставленный вектор не может быть нулевым");
        }
        Vector4f vector4fCol = vectorCol3f.translationToVector4f();
        Vector4f vec = this.mulVector(vector4fCol);
        return new Vector3f(vec.getX() / vec.getW(), vec.getY() / vec.getW(), vec.getZ() / vec.getW());
    }

    public Matrix4f mulMatrix(Matrix4f matrix4f) {
        if (matrix4f.getMatrix().length != 4 || matrix4f.getMatrix()[0].length != 4) {
            throw new IllegalArgumentException("Предоставленная матрица должна быть матрицей 4 на 4.");
        }
        float[][] values = new float[4][4];
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                values[i][j] = 0;
                for (int k = 0; k < 4; k++) {
                    values[i][j] += matrix[i][k] * matrix4f.getCell(k, j);
                }
            }
        }
        return new Matrix4f(values);
    }
}
