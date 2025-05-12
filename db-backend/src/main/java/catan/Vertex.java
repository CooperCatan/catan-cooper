package catan;

import java.util.ArrayList;
import java.util.List;

public class Vertex {
    private int id;
    private boolean isOccupied;
    private Long ownerId;
    private String buildingType;  // "settlement" or "city"
    
    // need coordinates for rendering
    private double x;
    private double y;
    
    // list of connected elements on board
    private List<Integer> adjacentVertices;
    private List<Integer> connectedEdges;
    private List<Integer> adjacentHexes;

    public Vertex() {
        this.isOccupied = false;
        this.adjacentVertices = new ArrayList<>();
        this.connectedEdges = new ArrayList<>();
        this.adjacentHexes = new ArrayList<>();
        // default coords, should be overwritten
        this.x = 0.0;
        this.y = 0.0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public String getBuildingType() { return buildingType; }
    public void setBuildingType(String buildingType) { this.buildingType = buildingType; }

    public double getX() { return x; }
    public void setX(double x) { this.x = x; }

    public double getY() { return y; }
    public void setY(double y) { this.y = y; }

    public List<Integer> getAdjacentVertices() { return adjacentVertices; }
    public void setAdjacentVertices(List<Integer> adjacentVertices) { this.adjacentVertices = adjacentVertices; }

    public List<Integer> getConnectedEdges() { return connectedEdges; }
    public void setConnectedEdges(List<Integer> connectedEdges) { this.connectedEdges = connectedEdges; }

    public List<Integer> getAdjacentHexes() { return adjacentHexes; }
    public void setAdjacentHexes(List<Integer> adjacentHexes) { this.adjacentHexes = adjacentHexes; }
} 