package catan;

import java.util.ArrayList;
import java.util.List;

class testBoard {
    private Hex hex;
    private Vertex[] vertices;

    //TEST MAIN
    public static void main(String[] args) {
        //Set up board
        testBoard board = new testBoard();
        //Check hex type
        System.out.println(board.getHex().getResourceType());

        //Start board manipulation
        board.getVertex(4).setBuilding(1, 23);
        List<Edge> edges = board.getVertex(4).getAdjacentEdges();
        System.out.println("Before adding a road:");
        for (Edge edge : edges) {
            System.out.println("Edge from vertex " + edge.getVertex1().getId() + " to " + edge.getVertex2().getId() + " has road: " + edge.hasRoad());
        }

        //Add a road and test again
        System.out.println("After adding a road:");
        edges.get(1).setRoad(23);
        for (Edge edge : edges) {
            System.out.println("Edge from vertex " + edge.getVertex1().getId() + " to " + edge.getVertex2().getId() + " has road: " + edge.hasRoad());
        }
        System.out.println("After trying to add the road again:");
        edges.get(1).setRoad(23);
        System.out.println("Building type on vertex 4: " + board.getVertex(4).getBuildingType());
        System.out.println("Building type on vertex 5: " + board.getVertex(5).getBuildingType());

        //Check edges from vertex 5
        edges = board.getVertex(5).getAdjacentEdges();
        for (Edge edge : edges) {
            System.out.println("Edge from vertex " + edge.getVertex1().getId() + " to " + edge.getVertex2().getId() + " has road: " + edge.hasRoad());
        }
    }

    public testBoard() {
        vertices = new Vertex[6];
        for (int i = 0; i < 6; i++) {
            vertices[i] = new Vertex(i);
        }
        hex = new Hex(1, "Ore", 6);
        for (int i = 0; i < 6; i++) {
            hex.setVertex(i, vertices[i]);
        }
        for (int i = 0; i < 6; i++) {
            vertices[i].addEdge(vertices[(i + 1) % 6]);
        }
    }

    public Hex getHex() {
        return hex;
    }

    public Vertex getVertex(int index) {
        return vertices[index];
    }
}