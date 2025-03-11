package catan;

import java.util.*;

class testBoard {
    List<Hex> hexes;
    private final Vertex[] vertices;

    //TEST MAIN
    public static void main(String[] args) {
        //Set up board
        testBoard board = new testBoard();
        //Check hex type
        System.out.println(board.getHex(1).getResourceType());

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
        this.vertices = new Vertex[54];
        //Make all the vertices on the board
        for (int i = 0; i < 54; i++) {
            this.vertices[i] = new Vertex(i+1);
        }
        //Make a list of hexes
        this.hexes = new ArrayList<>();

        //Make the lists of resources and roll values, and then shuffle them
        List<String> resources = new ArrayList<>(Arrays.asList(
                "brick", "brick", "brick",
                "wood", "wood", "wood", "wood",
                "wheat", "wheat", "wheat", "wheat",
                "ore", "ore", "ore",
                "sheep", "sheep", "sheep", "sheep",
                "desert"
        ));
        List<Integer> rvs = new ArrayList<>(Arrays.asList(
                2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12
        ));
        Collections.shuffle(rvs);
        Collections.shuffle(resources);

        //Hex to be inserted
        //Have to unlink i and itrRVS because of the desert tile
        Hex insert;
        int itrRVS = 0;
        for (int i = 0; i < 19; i++) {
            //Set a Hex to its resource type. If desert, it doesn't get a number.
            if(resources.get(i).equals("desert")) {
                insert = new Hex(i+1, "desert", 0);
            } else {
                insert = new Hex(i+1, resources.get(i), rvs.get(itrRVS));
                itrRVS++;
            }
            hexes.add(insert);
            //Now assign all of these hexes their adjacent vertices
            //The linker also handles
            linkVertices(insert);
        }
    }

    //I'm building this separately for readability and because if I don't a lot of the conditionals are repetitive
    public void linkVertices(Hex index) {
        if(index.getId() <= 3) {
            //These hexes "view" their surrounding vertices in clockwise order from top left to bottom left 0->5
            //This means that vertex "3" to hex 1 is vertex "5" to hex 2 (similarly vertex 2 on hex 1 is vertex 0 on hex 2)
            //The hexes 1 through 3 link to the vertices in the following map:
            index.setVertex(0, vertices[2*index.getId()-2]);
            index.setVertex(1, vertices[2*index.getId()-1]);
            index.setVertex(2, vertices[2*index.getId()]);
            index.setVertex(3, vertices[2*index.getId()+8]);
            index.setVertex(4, vertices[2*index.getId()+7]);
            index.setVertex(5, vertices[2*index.getId()+6]);
        } else if (index.getId() <= 7) {
            //The hexes 4 through 7 map via:
            index.setVertex(0, vertices[2*index.getId()-1]);
            index.setVertex(1, vertices[2*index.getId()]);
            index.setVertex(2, vertices[2*index.getId()+1]);
            index.setVertex(3, vertices[2*index.getId()+11]);
            index.setVertex(4, vertices[2*index.getId()+10]);
            index.setVertex(5, vertices[2*index.getId()+9]);
        } else if (index.getId() <= 12) {
            //The hexes 8 through 12 map via:
            index.setVertex(0, vertices[2*index.getId()]);
            index.setVertex(1, vertices[2*index.getId()+1]);
            index.setVertex(2, vertices[2*index.getId()+2]);
            index.setVertex(3, vertices[2*index.getId()+13]);
            index.setVertex(4, vertices[2*index.getId()+12]);
            index.setVertex(5, vertices[2*index.getId()+11]);
        } else if (index.getId() <= 16) {
            //The hexes 13 through 16 map via:
            index.setVertex(0, vertices[2*index.getId()+2]);
            index.setVertex(1, vertices[2*index.getId()+3]);
            index.setVertex(2, vertices[2*index.getId()+4]);
            index.setVertex(3, vertices[2*index.getId()+14]);
            index.setVertex(4, vertices[2*index.getId()+13]);
            index.setVertex(5, vertices[2*index.getId()+12]);
        } else if (index.getId() <= 19) {
            //The hexes 17 through 19 map via:
            index.setVertex(0, vertices[2*index.getId()+5]);
            index.setVertex(1, vertices[2*index.getId()+6]);
            index.setVertex(2, vertices[2*index.getId()+7]);
            index.setVertex(3, vertices[2*index.getId()+15]);
            index.setVertex(4, vertices[2*index.getId()+14]);
            index.setVertex(5, vertices[2*index.getId()+13]);
        }
        //Add edges to this hex list
        for (int i = 0; i < 6; i++) {
            index.getVertices().get(i).addEdge(index.getVertices().get((i + 1) % 6));
        }
    }

    public void build(String type, int v1, int v2, int pid) {
        switch (type) {
            case "SETTLEMENT" -> {
                //TODO -- CHECK RESOURCES
                boolean possible = false; //determines if settlement placement is possible
                //Have to check the current vertex
                if(vertices[v1-1].getBuildingType() == 0) {
                    //These two are the conditional checks
                    boolean hasRoad = false;
                    boolean hasNeighbor = false;
                    //Now that the vertex is known to be empty, loop through its edge list.
                    for (Edge link : vertices[v1-1].getAdjacentEdges()) {
                        //If one of these edges has a road of the correct pid, it may be possible
                        if (link.hasRoad() && link.getPlayerId() == pid) {
                            hasRoad = true;
                        }
                        //If one of these edges has a building on the other end, the placement is definitely impossible
                        //This checks both vertices since Vertex1 vs Vertex2 is orientation dependant, but it still works
                        //since you haven't set the building on either vertex yet.
                        if (link.getVertex1().getBuildingType() != 0 || link.getVertex2().getBuildingType() != 0) {
                            hasNeighbor = true;
                            break; //You can stop checking
                        }
                    }
                    //This is like the equation for if placement is valid
                    possible = hasRoad && !hasNeighbor;
                }
                if(possible) {
                    //TODO -- SUBTRACT RESOURCES
                    vertices[v1-1].setBuilding(1, pid);
                }
            }
            case "CITY" -> {
                //TODO -- CHECK RESOURCES
                if(vertices[v1-1].getBuildingType() == 1 && vertices[v1-1].getPlayerId() == pid) {
                    //TODO -- SUBTRACT RESOURCES
                    vertices[v1-1].setBuilding(2, pid);
                }
            }
            case "ROAD" -> {
                //TODO -- CHECK RESOURCES
                for (Edge link : vertices[v1-1].getAdjacentEdges()) {
                    if (link.getVertex1().getId() == v2 || link.getVertex2().getId() == v2) {
                        link.setRoad(pid);
                        //TODO -- SUBTRACT RESOURCES AND FIX RETURN VALUE OF SET ROAD
                    }
                }
            }
            default ->
                //Should never happen
                    System.out.println("ERROR: Invalid build type");
        }
    }

    public Hex getHex(int index) {
        return this.hexes.get(index);
    }

    public Vertex getVertex(int index) {
        return this.vertices[index];
    }
}