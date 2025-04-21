package catan;

import java.util.Arrays;
import java.util.List;

public class Hex {
    private Integer[] vertexIds;
    private transient Vertex[] vertices;
    private int id;
    private String resourceType;
    private int rollValue;

    public Hex(int id, String resourceType, int rollValue) {
        this.id = id;
        //Hex always has 6 Vertices
        this.vertexIds = new Integer[6];
        this.vertices = new Vertex[6];
        this.resourceType = resourceType;
        this.rollValue = rollValue;
    }

    public void setVertex(int index, Vertex vertex) {
        //Credit to ChatGPT for helping me track down this annoying but from the deserializer interaction with gson
        if (this.vertices == null) {
            this.vertices = new Vertex[6];
        }

        this.vertexIds[index] = vertex.getId();
        this.vertices[index] = vertex;
    }

    public List<Integer> getVertexIds() { return Arrays.asList(vertexIds); }

    public List<Vertex> getVertices() {
        return Arrays.asList(vertices);
    }

    public int getId() {
        return id;
    }

    public String getResourceType() {
        return resourceType;
    }

    public int getRollValue() {
        return rollValue;
    }
}