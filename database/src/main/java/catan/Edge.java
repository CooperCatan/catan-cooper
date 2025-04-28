package catan;

class Edge {
    private final int vertex1Id;
    private final int vertex2Id;
    private boolean hasRoad;
    private int playerId;

    public Edge(int vertex1Id, int vertex2Id, boolean hasRoad, int playerId) {
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

    public int getPlayerId() {
        return playerId;
    }

    public void setRoad(int playerId) {
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
}