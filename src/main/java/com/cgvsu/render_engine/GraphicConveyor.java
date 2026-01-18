package com.cgvsu.render_engine;

import com.cgvsu.math.matrix.Matrix4f;
import com.cgvsu.math.point.Point2f;
import com.cgvsu.math.vector.Vector3f;

public class GraphicConveyor {

    public static Matrix4f rotateScaleTranslate() {
        float[] matrix = new float[]{
                1, 0, 0, 0,
                0, 1, 0, 0,
                0, 0, 1, 0,
                0, 0, 0, 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target) {
        return lookAt(eye, target, new Vector3f(0F, 1.0F, 0F));
    }

    public static Matrix4f lookAt(Vector3f eye, Vector3f target, Vector3f up) {
        Vector3f resultX = new Vector3f();
        Vector3f resultY = new Vector3f();
        Vector3f resultZ = new Vector3f();

        resultZ.sub(target, eye);
        resultX.cross(up, resultZ);
        resultY.cross(resultZ, resultX);

        resultX.normalize();
        resultY.normalize();
        resultZ.normalize();

        float[] matrix = new float[]{
                resultX.getX(), resultY.getX(), resultZ.getX(), 0,
                resultX.getY(), resultY.getY(), resultZ.getY(), 0,
                resultX.getZ(), resultY.getZ(), resultZ.getZ(), 0,
                -resultX.dot(eye), -resultY.dot(eye), -resultZ.dot(eye), 1};
        return new Matrix4f(matrix);
    }

    public static Matrix4f perspective(
            final float fov,
            final float aspectRatio,
            final float nearPlane,
            final float farPlane) {
        Matrix4f result = new Matrix4f();
        float tangentMinusOnDegree = (float) (1.0F / (Math.tan(fov * 0.5F)));
        result.setCell(0,0, tangentMinusOnDegree / aspectRatio);
        result.setCell(1,1, tangentMinusOnDegree);
        result.setCell(2,2, (farPlane + nearPlane) / (farPlane - nearPlane));
        result.setCell(2,3, 1.0f);
        result.setCell(3,2, 2 * (nearPlane * farPlane) / (nearPlane - farPlane));
        return result;
    }

    public static Vector3f rotatePointAroundAxis(Vector3f point, Vector3f center, Vector3f axis, float angle) {
        Vector3f translatedPoint = new Vector3f(
                point.getX() - center.getX(),
                point.getY() - center.getY(),
                point.getZ() - center.getZ()
        );

        Matrix4f rotationMatrix = createRotationMatrix(axis, angle);
        Vector3f rotatedPoint = GraphicConveyor.multiplyMatrix4ByVector3(rotationMatrix, translatedPoint);

        return new Vector3f(
                rotatedPoint.getX() + center.getX(),
                rotatedPoint.getY() + center.getY(),
                rotatedPoint.getZ() + center.getZ()
        );
    }

    private static Matrix4f createRotationMatrix(Vector3f axis, float angle) {
        float cos = (float) Math.cos(angle);
        float sin = (float) Math.sin(angle);
        float oneMinusCos = 1 - cos;

        float x = axis.getX();
        float y = axis.getY();
        float z = axis.getZ();

        float[] matrix = new float[]{
                cos + x * x * oneMinusCos, x * y * oneMinusCos - z * sin, x * z * oneMinusCos + y * sin, 0,
                y * x * oneMinusCos + z * sin, cos + y * y * oneMinusCos, y * z * oneMinusCos - x * sin, 0,
                z * x * oneMinusCos - y * sin, z * y * oneMinusCos + x * sin, cos + z * z * oneMinusCos, 0,
                0, 0, 0, 1
        };

        return new Matrix4f(matrix);
    }

    // сначала надо транспонировать матрицу для правильного умножения
    public static Vector3f multiplyMatrix4ByVector3(final Matrix4f matrix, final Vector3f vertex) {
        Matrix4f matrixTrans = matrix.transposition();
        return matrixTrans.mulVectorDivW(vertex);
    }

    public static Point2f vertexToPoint(final Vector3f vertex, final int width, final int height) {
        return new Point2f(vertex.getX() * width + width / 2.0F, -vertex.getY() * height + height / 2.0F);
    }
}
