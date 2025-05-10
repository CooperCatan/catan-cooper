package catan;

import java.util.ArrayList;
import java.util.List;

public class Edge {
    private int id;
    private boolean isOccupied;
    private Long ownerId;
    private List<Integer> connectedVertices;
    private List<Integer> connectedEdges;
    private List<Integer> adjacentHexes;

    public Edge() {
        this.isOccupied = false;
        this.connectedVertices = new ArrayList<>();
        this.connectedEdges = new ArrayList<>();
        this.adjacentHexes = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public boolean isOccupied() { return isOccupied; }
    public void setOccupied(boolean occupied) { isOccupied = occupied; }

    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

    public List<Integer> getConnectedVertices() { return connectedVertices; }
    public void setConnectedVertices(List<Integer> connectedVertices) { this.connectedVertices = connectedVertices; }

    public List<Integer> getConnectedEdges() { return connectedEdges; }
    public void setConnectedEdges(List<Integer> connectedEdges) { this.connectedEdges = connectedEdges; }

    public List<Integer> getAdjacentHexes() { return adjacentHexes; }
    public void setAdjacentHexes(List<Integer> adjacentHexes) { this.adjacentHexes = adjacentHexes; }
} 