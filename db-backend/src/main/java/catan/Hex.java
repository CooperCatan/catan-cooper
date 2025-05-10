package catan;

import java.util.List;
import java.util.ArrayList;

public class Hex {
    private int id;
    private String type;  // "wood", "brick", "ore", "wheat", "sheep", "desert"
    private int pipValue;
    private boolean hasRobber;
    private double x;
    private double y;
    private List<Integer> adjacentVertices;
    private List<Integer> adjacentEdges;

    public Hex() {
        this.adjacentVertices = new ArrayList<>();
        this.adjacentEdges = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public int getPipValue() { return pipValue; }
    public void setPipValue(int pipValue) { this.pipValue = pipValue; }

    public boolean hasRobber() { return hasRobber; }
    public void setHasRobber(boolean hasRobber) { this.hasRobber = hasRobber; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public List<Integer> getAdjacentVertices() { return adjacentVertices; }
    public void setAdjacentVertices(List<Integer> adjacentVertices) { this.adjacentVertices = adjacentVertices; }

    public List<Integer> getAdjacentEdges() { return adjacentEdges; }
    public void setAdjacentEdges(List<Integer> adjacentEdges) { this.adjacentEdges = adjacentEdges; }
} 