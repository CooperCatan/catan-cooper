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
        if(this.adjacentEdges.contains(newEdge) || vertex.adjacentEdges.contains(newEdge)) {
            //Do nothing
        } else {
            //Add it to the list of both
            this.adjacentEdges.add(newEdge);
            vertex.adjacentEdges.add(newEdge);
        }
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

    public boolean setBuilding(int newBuilding, int playerId) {
        //Check if the building type is valid, for this object. If so, return true
        if (this.buildingType == 0 && newBuilding == 1) {
            this.buildingType = newBuilding;
            return true;
        } else if(this.buildingType == 1 && newBuilding == 2) {
            this.buildingType = newBuilding;
            return true;
        } else {
            return false;
        }
    }
}