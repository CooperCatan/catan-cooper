package catan;

class Edge {
    private final Vertex vertex1;
    private final Vertex vertex2;
    private boolean hasRoad;
    private int playerId;

    public Edge(Vertex vertex1, Vertex vertex2, boolean hasRoad, int playerId) {
        this.vertex1 = vertex1;
        this.vertex2 = vertex2;
        this.hasRoad = hasRoad;
        this.playerId = playerId;
    }

    public Vertex getVertex1() {
        return vertex1;
    }

    public Vertex getVertex2() {
        return vertex2;
    }

    public boolean hasRoad() {
        return hasRoad;
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setRoad(int playerId) {
        if(this.hasRoad) {
            System.out.println("Road already exists");
        } else {
            this.hasRoad = true;
            this.playerId = playerId;
        }
    }
}