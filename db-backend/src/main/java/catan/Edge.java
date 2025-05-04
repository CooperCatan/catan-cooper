package catan;

class Edge {
    private final int vertex1Id;
    private final int vertex2Id;
    private boolean hasRoad;
    private long playerId;

    public Edge(int vertex1Id, int vertex2Id, boolean hasRoad, long playerId) {
        this.vertex1Id = vertex1Id;
        this.vertex2Id = vertex2Id;
        this.hasRoad = hasRoad;
        this.playerId = playerId;
    }

    public int getVertex1() {
        return vertex1Id;
    }

    public int getVertex2() {
        return vertex2Id;
    }

    public boolean hasRoad() {
        return hasRoad;
    }
    public void setRoad(boolean hasRoad) {
        this.hasRoad = hasRoad;
    }

    public long getPlayerId() {
        return playerId;
    }

    public void setRoad(long playerId) {
        if(this.hasRoad) {
            System.out.println("Road already exists between " + this.vertex1Id + " and " + this.vertex2Id);
        } else {
            this.hasRoad = true;
            this.playerId = playerId;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        //If it's not an edge obviously false
        if (!(o instanceof Edge edge)) {
            return false;
        }
        //If the source an destination is the same as the destination and source, the edge is the same
        return (vertex1Id == edge.vertex1Id && vertex2Id == edge.vertex2Id) || (vertex1Id == edge.vertex2Id && vertex2Id == edge.vertex1Id);
    }

    @Override
    public int hashCode() {
        //Fixing hashCode for the edge items
        return Math.min(vertex1Id, vertex2Id) * 31 + Math.max(vertex1Id, vertex2Id);
    }

    public void setPlayerId(Long accountId) {
        this.playerId = accountId;
    }
}