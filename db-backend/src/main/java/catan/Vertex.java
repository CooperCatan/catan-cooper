package catan;

import java.util.ArrayList;
import java.util.List;

class Vertex {
    private int id;
    private List<Integer> adjacentEdgeIds;
    private transient List<Edge> adjacentEdges;
    private int buildingType; // 0 for nothing, 1 for settlement, 2 for city
    private long playerId; // ID of the player who owns the building

    public Vertex(int id) {
        this.id = id;
        this.adjacentEdgeIds = new ArrayList<>();
        this.adjacentEdges = new ArrayList<>();
        this.buildingType = 0; // Default to no building
        this.playerId = 0; // Default to no owner
    }

    public void addEdge(Vertex vertex) {
        //DEFAULT EXPRESSION FOR GAME START
        addEdge(vertex, false, -1);
    }

    //Workaround for GSON problems with constructor avoidant data population
    public void init() {
        this.adjacentEdges = new ArrayList<>();
    }

    public void addEdge(Vertex vertex, boolean hasRoad, long playerId) {
        Edge newEdge = new Edge(this.getId(), vertex.getId(), hasRoad, playerId);
        if(this.adjacentEdges == null) {
            this.init();
        }
        if(vertex.getAdjacentEdges() == null) {
            vertex.init();
        }
        //If there are no edges, or it is in the edge list of either side, it is a duplicate and could lose road data
        if(!(this.adjacentEdges.contains(newEdge) || vertex.adjacentEdges.contains(newEdge))) {
            //Add it to the list of both
            if(!this.adjacentEdgeIds.contains(vertex.getId())) {
                this.adjacentEdgeIds.add(vertex.getId());
            }
            this.adjacentEdges.add(newEdge);
            if(!vertex.adjacentEdgeIds.contains(this.getId())) {
                vertex.adjacentEdgeIds.add(this.getId());
            }
            vertex.adjacentEdges.add(newEdge);
        } else {
            //do nothing
        }
    }

    public int getId() {
        return id;
    }

    public List<Integer> getAdjacentEdgeIds() { return adjacentEdgeIds; }
    public List<Edge> getAdjacentEdges() {
        if(this.adjacentEdges == null) {
            this.init();
        }
        return adjacentEdges;
    }

    public int getBuildingType() {
        return buildingType;
    }

    public long getPlayerId() {
        return playerId;
    }
    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

    public boolean setBuilding(int newBuilding, long playerId) {
        //Check if the building type is valid, for this object. If so, return true
        if (this.buildingType == 0 && newBuilding == 1) {
            this.buildingType = newBuilding;
            return true;
        } else if(this.buildingType == 1 && newBuilding == 2 && playerId == this.playerId) {
            //Cities have the extra condition of needing the previous building to belong to the same person
            this.buildingType = newBuilding;
            return true;
        } else {
            return false;
        }
    }
}