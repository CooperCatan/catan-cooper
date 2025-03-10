package catan;

import java.util.Arrays;
import java.util.List;

public class Hex {

    private Vertex[] vertices;
    private int id;
    private String resourceType;
    private int rollValue;

    public Hex(int id, String resourceType, int rollValue) {
        this.id = id;
        //Hex always has 6 Vertices
        this.vertices = new Vertex[6];
        this.resourceType = resourceType;
        this.rollValue = rollValue;
    }

    public void setVertex(int index, Vertex vertex) {
        vertices[index] = vertex;
    }

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