package com.cgvsu.objwriter;

import com.cgvsu.math.Vector2f;
import com.cgvsu.math.Vector3f;
import com.cgvsu.model.Model;
import com.cgvsu.model.Polygon;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

public class ObjWriter {

    public static void write(Model model, String fileName) throws IOException {
        try (FileWriter writer = new FileWriter(fileName)) {
            writeModel(model, writer);
        }
    }

    public static void writeModel(Model model, FileWriter writer) throws IOException {
        // Записываем вершины
        for (Vector3f vertex : model.vertices) {
            writer.write(String.format("v %.6f %.6f %.6f\n",
                    vertex.x, vertex.y, vertex.z));
        }

        writer.write("\n");

        // Записываем текстуры
        for (Vector2f texture : model.textureVertices) {
            writer.write(String.format("vt %.6f %.6f\n",
                    texture.x, texture.y));
        }

        writer.write("\n");

        // Записываем нормали
        for (Vector3f normal : model.normals) {
            writer.write(String.format("vn %.6f %.6f %.6f\n",
                    normal.x, normal.y, normal.z));
        }

        writer.write("\n");

        // Записываем полигоны
        for (Polygon polygon : model.polygons) {
            writer.write("f ");
            List<Integer> vertexIndices = polygon.getVertexIndices();
            List<Integer> textureIndices = polygon.getTextureVertexIndices();
            List<Integer> normalIndices = polygon.getNormalIndices();

            for (int i = 0; i < vertexIndices.size(); i++) {
                int vIndex = vertexIndices.get(i) + 1; // OBJ индексы с 1

                if (!textureIndices.isEmpty() && !normalIndices.isEmpty()) {
                    int vtIndex = textureIndices.get(i) + 1;
                    int vnIndex = normalIndices.get(i) + 1;
                    writer.write(String.format("%d/%d/%d ", vIndex, vtIndex, vnIndex));
                } else if (!textureIndices.isEmpty()) {
                    int vtIndex = textureIndices.get(i) + 1;
                    writer.write(String.format("%d/%d ", vIndex, vtIndex));
                } else if (!normalIndices.isEmpty()) {
                    int vnIndex = normalIndices.get(i) + 1;
                    writer.write(String.format("%d//%d ", vIndex, vnIndex));
                } else {
                    writer.write(String.format("%d ", vIndex));
                }
            }
            writer.write("\n");
        }
    }
}