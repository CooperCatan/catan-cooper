package catan;

import java.util.ArrayList;
import java.util.List;

class Vertex {
    private int id;
    private List<Edge> adjacentEdges;
    private int buildingType; // 0 for nothing, 1 for settlement, 2 for city
    private int playerId; // ID of the player who owns the building

    public Vertex(int id) {
        this.id = id;
        this.adjacentEdges = new ArrayList<>();
        this.buildingType = 0; // Default to no building
        this.playerId = -1; // Default to no owner
    }

    public void addEdge(Vertex vertex) {
        Edge newEdge = new Edge(this, vertex, false, -1);
        this.adjacentEdges.add(newEdge);
        vertex.adjacentEdges.add(newEdge);
    }

    public int getId() {
        return id;
    }

    public List<Edge> getAdjacentEdges() {
        return adjacentEdges;
    }

    public int getBuildingType() {
        return buildingType;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setBuilding(int buildingType, int playerId) {
        if (buildingType >= 0 && buildingType <= 2) {
            this.buildingType = buildingType;
            this.playerId = playerId;
        }
    }
}