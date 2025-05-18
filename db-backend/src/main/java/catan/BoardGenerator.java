package catan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.geom.Point2D;
import java.util.*;

// class for generating init board state
public class BoardGenerator {

    private static final Logger logger = LoggerFactory.getLogger(BoardGenerator.class);

    // board constants
    public static final int NUM_HEXES = 19;
    public static final int NUM_VERTICES = 54; 
    public static final int NUM_EDGES = 72;    

    // resource constants and pip constants
    private static final int WOOD_COUNT = 4;
    private static final int WHEAT_COUNT = 4;
    private static final int SHEEP_COUNT = 4;
    private static final int BRICK_COUNT = 3;
    private static final int ORE_COUNT = 3;
    private static final String DESERT_TYPE = "desert";
    private static final Integer[] PIP_VALUES = {2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12}; // Standard pips (18 total)
    private static final int DESERT_HEX_ID = 10; // fixed desert id

    // coord calc constants 
    // these constants define the geometry for SVG rendering and dynamic coordinate calculation
    private static final double TILE_SIZE = 60.0; //  radius from hex center to a corner
    private static final double H_DIST = Math.sqrt(3.0) * TILE_SIZE; // horizontal distance between hex centers (sqrt(3)*TILE_SIZE)
    private static final double V_DIST = 1.5 * TILE_SIZE; // vertical distance between hex rows (center to center)

    // these offsets are used when calculating pixel centers based on a grid layout
    private static final double RENDER_OFFSET_X = TILE_SIZE * 2; // Start rendering slightly offset from SVG edge
    private static final double RENDER_OFFSET_Y = TILE_SIZE * 1.5;

    // board maps
    // edge ID (1-72) -> {vertex ID 1 (1-54), vertex ID 2 (1-54)}
    private static final Map<Integer, int[]> EDGE_TO_VERTICES_MAP = new HashMap<>();
    // hex ID (1-19) -> {6 Vertex IDs (1-54) in clockwise order, starting from top
    private static final Map<Integer, int[]> HEX_TO_VERTICES_MAP = new HashMap<>();

    static {
        long startTime = System.currentTimeMillis();
        try {
            HEX_TO_VERTICES_MAP.clear();
            EDGE_TO_VERTICES_MAP.clear();

            // game board map
            // hex numbering in reading order left->right and then top->down like reading a book
            HEX_TO_VERTICES_MAP.put(1, new int[]{1, 5, 9, 13, 8, 4});      // top left hex
            HEX_TO_VERTICES_MAP.put(2, new int[]{2, 6, 10, 14, 9, 5});     // top middle hex
            HEX_TO_VERTICES_MAP.put(3, new int[]{3, 7, 11, 15, 10, 6});     // top right hex

            HEX_TO_VERTICES_MAP.put(4, new int[]{8, 13, 18, 23, 17, 12});     
            HEX_TO_VERTICES_MAP.put(5, new int[]{9, 14, 19, 24, 18, 13});   
            HEX_TO_VERTICES_MAP.put(6, new int[]{10, 15, 20, 25, 19, 14});  
            HEX_TO_VERTICES_MAP.put(7, new int[]{11, 16, 21, 26, 20, 15});
            
            HEX_TO_VERTICES_MAP.put(8, new int[]{17, 23, 29, 34, 28, 22});  // middle row, far-left hex
            HEX_TO_VERTICES_MAP.put(9, new int[]{18, 24, 30, 35, 29, 23});  // middle row, left hex
            HEX_TO_VERTICES_MAP.put(10, new int[]{19, 25, 31, 36, 30, 24}); // middle row mdidle hex (hard-coded desert)
            HEX_TO_VERTICES_MAP.put(11, new int[]{20, 26, 32, 37, 31, 25}); // middle row, right hex
            HEX_TO_VERTICES_MAP.put(12, new int[]{21, 27, 33, 38, 32, 26}); // middle row, far-right hex
            
            HEX_TO_VERTICES_MAP.put(13, new int[]{29, 35, 40, 44, 39, 34}); 
            HEX_TO_VERTICES_MAP.put(14, new int[]{30, 36, 41, 45, 40, 35}); 
            HEX_TO_VERTICES_MAP.put(15, new int[]{31, 37, 42, 46, 41, 36}); 
            HEX_TO_VERTICES_MAP.put(16, new int[]{32, 38, 43, 47, 42, 37}); 
            
            HEX_TO_VERTICES_MAP.put(17, new int[]{40, 45, 49, 52, 48, 44}); // bottom left hex
            HEX_TO_VERTICES_MAP.put(18, new int[]{41, 46, 50, 53, 49, 45}); // bottom middle hex
            HEX_TO_VERTICES_MAP.put(19, new int[]{42, 47, 51, 54, 50, 46}); // bottom right hex

            // edge to vertices mapping, same deal, left to right reading order
            EDGE_TO_VERTICES_MAP.put(1, new int[]{4, 1});
            EDGE_TO_VERTICES_MAP.put(2, new int[]{1, 5});
            EDGE_TO_VERTICES_MAP.put(3, new int[]{5, 2});
            EDGE_TO_VERTICES_MAP.put(4, new int[]{2, 6});
            EDGE_TO_VERTICES_MAP.put(5, new int[]{6, 3});
            EDGE_TO_VERTICES_MAP.put(6, new int[]{3, 7});
            EDGE_TO_VERTICES_MAP.put(7, new int[]{4, 8});
            EDGE_TO_VERTICES_MAP.put(8, new int[]{5, 9});
            EDGE_TO_VERTICES_MAP.put(9, new int[]{6, 10});
            EDGE_TO_VERTICES_MAP.put(10, new int[]{7, 11});
            EDGE_TO_VERTICES_MAP.put(11, new int[]{12, 8});
            EDGE_TO_VERTICES_MAP.put(12, new int[]{8, 13});
            EDGE_TO_VERTICES_MAP.put(13, new int[]{13, 9});
            EDGE_TO_VERTICES_MAP.put(14, new int[]{9, 14});
            EDGE_TO_VERTICES_MAP.put(15, new int[]{14, 10});
            EDGE_TO_VERTICES_MAP.put(16, new int[]{10, 15});
            EDGE_TO_VERTICES_MAP.put(17, new int[]{15, 11});
            EDGE_TO_VERTICES_MAP.put(18, new int[]{11, 16}); // done so far
            EDGE_TO_VERTICES_MAP.put(19, new int[]{12, 17});
            EDGE_TO_VERTICES_MAP.put(20, new int[]{13, 18});
            EDGE_TO_VERTICES_MAP.put(21, new int[]{14, 19});
            EDGE_TO_VERTICES_MAP.put(22, new int[]{15, 20});
            EDGE_TO_VERTICES_MAP.put(23, new int[]{16, 21});
            EDGE_TO_VERTICES_MAP.put(24, new int[]{22, 17});
            EDGE_TO_VERTICES_MAP.put(25, new int[]{17, 23});
            EDGE_TO_VERTICES_MAP.put(26, new int[]{23, 18});
            EDGE_TO_VERTICES_MAP.put(27, new int[]{18, 24});
            EDGE_TO_VERTICES_MAP.put(28, new int[]{24, 19});
            EDGE_TO_VERTICES_MAP.put(29, new int[]{19, 25});
            EDGE_TO_VERTICES_MAP.put(30, new int[]{25, 20});
            EDGE_TO_VERTICES_MAP.put(31, new int[]{20, 26});
            EDGE_TO_VERTICES_MAP.put(32, new int[]{26, 21});
            EDGE_TO_VERTICES_MAP.put(33, new int[]{21, 27}); // done so far
            EDGE_TO_VERTICES_MAP.put(34, new int[]{22, 28});
            EDGE_TO_VERTICES_MAP.put(35, new int[]{23, 29});
            EDGE_TO_VERTICES_MAP.put(36, new int[]{24, 30});
            EDGE_TO_VERTICES_MAP.put(37, new int[]{25, 31});
            EDGE_TO_VERTICES_MAP.put(38, new int[]{26, 32});
            EDGE_TO_VERTICES_MAP.put(39, new int[]{27, 33});
            EDGE_TO_VERTICES_MAP.put(40, new int[]{28, 34});
            EDGE_TO_VERTICES_MAP.put(41, new int[]{34, 29}); // done so far
            EDGE_TO_VERTICES_MAP.put(42, new int[]{29, 35});
            EDGE_TO_VERTICES_MAP.put(43, new int[]{35, 30});
            EDGE_TO_VERTICES_MAP.put(44, new int[]{30, 36});
            EDGE_TO_VERTICES_MAP.put(45, new int[]{36, 31});
            EDGE_TO_VERTICES_MAP.put(46, new int[]{31, 37});
            EDGE_TO_VERTICES_MAP.put(47, new int[]{37, 32});
            EDGE_TO_VERTICES_MAP.put(48, new int[]{32, 38});
            EDGE_TO_VERTICES_MAP.put(49, new int[]{38, 33});
            EDGE_TO_VERTICES_MAP.put(50, new int[]{34, 39});
            EDGE_TO_VERTICES_MAP.put(51, new int[]{35, 40});
            EDGE_TO_VERTICES_MAP.put(52, new int[]{36, 41});
            EDGE_TO_VERTICES_MAP.put(53, new int[]{37, 42});
            EDGE_TO_VERTICES_MAP.put(54, new int[]{38, 43});
            EDGE_TO_VERTICES_MAP.put(55, new int[]{39, 44});
            EDGE_TO_VERTICES_MAP.put(56, new int[]{44, 40});
            EDGE_TO_VERTICES_MAP.put(57, new int[]{40, 45});
            EDGE_TO_VERTICES_MAP.put(58, new int[]{45, 41});
            EDGE_TO_VERTICES_MAP.put(59, new int[]{41, 46});
            EDGE_TO_VERTICES_MAP.put(60, new int[]{46, 42});
            EDGE_TO_VERTICES_MAP.put(61, new int[]{42, 47});
            EDGE_TO_VERTICES_MAP.put(62, new int[]{47, 43});
            EDGE_TO_VERTICES_MAP.put(63, new int[]{44, 48});
            EDGE_TO_VERTICES_MAP.put(64, new int[]{45, 49});
            EDGE_TO_VERTICES_MAP.put(65, new int[]{46, 50});
            EDGE_TO_VERTICES_MAP.put(66, new int[]{47, 51});
            EDGE_TO_VERTICES_MAP.put(67, new int[]{48, 52});
            EDGE_TO_VERTICES_MAP.put(68, new int[]{52, 49});
            EDGE_TO_VERTICES_MAP.put(69, new int[]{49, 53});
            EDGE_TO_VERTICES_MAP.put(70, new int[]{53, 50});
            EDGE_TO_VERTICES_MAP.put(71, new int[]{50, 54});
            EDGE_TO_VERTICES_MAP.put(72, new int[]{54, 51}); // last edge

        } catch (Exception e) {
            logger.error("[BoardGenerator_STATIC_INIT] CRITICAL EXCEPTION during static map initialization: {}", e.getMessage(), e);
            throw new ExceptionInInitializerError(e); // halt application startup
        }
    }

    // inner class to hold the generated board data
    public static class BoardData {
        public final List<Hex> hexes;
        public final List<Vertex> vertices;
        public final List<Edge> edges;
        public final int initialRobberLocation;

        public BoardData(List<Hex> hexes, List<Vertex> vertices, List<Edge> edges, int initialRobberLocation) {
            this.hexes = hexes;
            this.vertices = vertices;
            this.edges = edges;
            this.initialRobberLocation = initialRobberLocation;
        }
    }

    // public method to gen board
    public static BoardData generateNewBoard() {
        logger.debug("[BoardGenerator] Starting initial board generation.");
        List<Hex> localHexes = new ArrayList<>();
        List<Vertex> localVertices = new ArrayList<>();
        List<Edge> localEdges = new ArrayList<>();
        Map<Integer, Point2D.Double> hexCenterCoords = new HashMap<>(); // to store calculated hex centers for vertex calculation
        int initialRobberHexId = DESERT_HEX_ID; // robber starts on the desert

        // shuffle resources and pips
        List<String> resourcesToAssign = new ArrayList<>();
        resourcesToAssign.addAll(Collections.nCopies(WOOD_COUNT, "wood"));
        resourcesToAssign.addAll(Collections.nCopies(WHEAT_COUNT, "wheat"));
        resourcesToAssign.addAll(Collections.nCopies(SHEEP_COUNT, "sheep"));
        resourcesToAssign.addAll(Collections.nCopies(BRICK_COUNT, "brick"));
        resourcesToAssign.addAll(Collections.nCopies(ORE_COUNT, "ore"));
        Collections.shuffle(resourcesToAssign);

        List<Integer> pipsToAssign = new ArrayList<>(Arrays.asList(PIP_VALUES));
        Collections.shuffle(pipsToAssign);
        int resourceAssignIdx = 0;
        int pipAssignIdx = 0;

        try {
            // generate hexes (ID, resource type, pips, coordinates) ---
            logger.debug("[BoardGenerator] Generating {} hexes...", NUM_HEXES);
            for (int i = 0; i < NUM_HEXES; i++) {
                int hexId = i + 1;
                Hex hex = new Hex();
                hex.setId(hexId);

                // assign type, pip, robber starts at desert during game init
                if (hexId == DESERT_HEX_ID) {
                    hex.setType(DESERT_TYPE);
                    hex.setHasRobber(true);
                    hex.setPipValue(0);
                } else {
                    if (resourceAssignIdx < resourcesToAssign.size()) {
                        hex.setType(resourcesToAssign.get(resourceAssignIdx++));
                    } else {
                        logger.error("[BoardGenerator] Ran out of resources to assign!"); hex.setType("error");
                    }
                    if (pipAssignIdx < pipsToAssign.size()) {
                        hex.setPipValue(pipsToAssign.get(pipAssignIdx++));
                    } else {
                        logger.error("[BoardGenerator] Ran out of pips to assign!"); hex.setPipValue(0);
                    }
                    hex.setHasRobber(false);
                }

                // calculate and store hex center pixel coordinates (for rendering and vertex calculation)
                double gridRow = calculateHexGridRow(hexId - 1); // 0-indexed
                double gridCol = calculateHexGridCol(hexId - 1); // 0-indexed

                double pixelCenterY = RENDER_OFFSET_Y + gridRow * V_DIST;

                double xOffsetForRow = 0.0;
                if (gridRow == 0 || gridRow == 4) { // topmost and bottommost rows (3 hexes)
                    xOffsetForRow = H_DIST; // shifted by one full hex width relative to middle row's start
                } else if (gridRow == 1 || gridRow == 3) { // Second and fourth rows (4 hexes)
                    xOffsetForRow = H_DIST / 2.0; // shifted by half hex width
                }

                double pixelCenterX = RENDER_OFFSET_X + xOffsetForRow + (gridCol * H_DIST);

                hex.setX(pixelCenterX); // store actual pixel X for frontend rendering
                hex.setY(pixelCenterY); // store actual pixel Y for frontend rendering
                hexCenterCoords.put(hexId, new Point2D.Double(pixelCenterX, pixelCenterY)); // Store for vertex calculation
                localHexes.add(hex);
            }
            logger.debug("[BoardGenerator] Generated {} hexes. Desert at Hex ID: {}. Hex centers calculated.",
                        localHexes.size(), DESERT_HEX_ID);

            // generate vertices (ID,  adjacency placeholders for checking and longest road)
            logger.debug("[BoardGenerator] Generating {} vertices and calculating coordinates...", NUM_VERTICES);
            for (int i = 0; i < NUM_VERTICES; i++) {
                int vertexId = i + 1;
                Vertex vertex = new Vertex();
                vertex.setId(vertexId);
                vertex.setOccupied(false);
                vertex.setBuildingType(null);
                vertex.setOwnerId(null);

                // find adjacent hexes using the topology map
                List<Integer> adjHexIds = findAdjacentHexIdsForVertex(vertexId);
                // calculate coordinates by averaging adjacent hex corners corresponding to this vertex
                Point2D.Double coords = calculateVertexCoordinates(vertexId, adjHexIds, hexCenterCoords);

                if (coords != null) {
                    vertex.setX(coords.getX());
                    vertex.setY(coords.getY());
                } else {
                    logger.warn("[BoardGenerator] Could not calculate coordinates for Vertex ID: {}. Setting to (0,0). Check topology maps.", vertexId);
                    vertex.setX(0.0); vertex.setY(0.0); // fallback
                }
                vertex.setAdjacentHexes(adjHexIds); // store hexes it borders
                vertex.setConnectedEdges(new ArrayList<>()); // init empty, populated next
                vertex.setAdjacentVertices(new ArrayList<>());// init empty, populated next
                localVertices.add(vertex);
            }
            logger.debug("[BoardGenerator] {} vertices generated with dynamic coordinates.", localVertices.size());

            // generate edge (connected vertices) ids
            logger.debug("[BoardGenerator] Generating {} edges and assigning vertex connections...", NUM_EDGES);
            for (int i = 0; i < NUM_EDGES; i++) {
                int edgeId = i + 1;
                Edge edge = new Edge();
                edge.setId(edgeId);
                edge.setOccupied(false);
                edge.setOwnerId(null);

                int[] connectedVIds = EDGE_TO_VERTICES_MAP.get(edgeId);
                if (connectedVIds != null && connectedVIds.length == 2 && connectedVIds[0] > 0 && connectedVIds[1] > 0) {
                    // convert int array to List<Integer>
                     edge.setConnectedVertices(Arrays.asList(connectedVIds[0], connectedVIds[1]));
                } else {
                    logger.error("[BoardGenerator] CRITICAL: Edge {} has missing, invalid, or placeholder vertex connections in EDGE_TO_VERTICES_MAP. Connected: {}. Topology map likely incomplete or incorrect!",
                                 edgeId, Arrays.toString(connectedVIds));
                    edge.setConnectedVertices(new ArrayList<>()); 
                }
                localEdges.add(edge);
            }
            logger.debug("[BoardGenerator] {} edges generated.", localEdges.size());

            // populate remaining adjacency info (vertex -> edges, vertex -> vertices) 
            // iterate through vertices and use the generated edges to find connections
            logger.debug("[BoardGenerator] Populating remaining adjacency info for vertices (connectedEdges, adjacentVertices)...");
            for (Vertex vertex : localVertices) {
                int currentVertexId = vertex.getId();
                List<Integer> edgesTouchingVertex = new ArrayList<>();
                List<Integer> verticesAdjacentToVertex = new ArrayList<>();

                for (Edge edge : localEdges) {
                    List<Integer> edgeVerts = edge.getConnectedVertices();
                    if (edgeVerts != null && edgeVerts.contains(currentVertexId)) {
                        edgesTouchingVertex.add(edge.getId());
                        // find the *other* vertex on this edge
                        for (int connectedVId : edgeVerts) {
                            if (connectedVId != currentVertexId && !verticesAdjacentToVertex.contains(connectedVId)) {
                                verticesAdjacentToVertex.add(connectedVId);
                            }
                        }
                    }
                }

                vertex.setConnectedEdges(edgesTouchingVertex);
                vertex.setAdjacentVertices(verticesAdjacentToVertex);
            }
            logger.debug("[BoardGenerator] Adjacency info (connectedEdges, adjacentVertices) populated for all vertices.");

            logger.info("[BoardGenerator] Board generation complete. Hexes: {}, Vertices: {}, Edges: {}. Robber initially at Hex: {}",
                    localHexes.size(), localVertices.size(), localEdges.size(), initialRobberHexId);
            return new BoardData(localHexes, localVertices, localEdges, initialRobberHexId);

        } catch (Exception e) {
            logger.error("[BoardGenerator] CRITICAL EXCEPTION during board generation: {}", e.getMessage(), e);
            return new BoardData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), -1); // Indicate error
        }
    }

    // priv helper methods

    // calculates the logical grid ROW (0-4) for a given 0-indexed hex ID
    private static int calculateHexGridRow(int index) {
        if (index < 3) return 0;       // Row 0 (Hexes 0-2)
        else if (index < 7) return 1;   // Row 1 (Hexes 3-6)
        else if (index < 12) return 2;  // Row 2 (Hexes 7-11)
        else if (index < 16) return 3;  // Row 3 (Hexes 12-15)
        else return 4;                  // Row 4 (Hexes 16-18)
    }

    // calculates the logical grid COLUMN index within its row for a 0-indexed hex ID
    private static int calculateHexGridCol(int index) {
        if (index < 3) return index;            // Row 0: 0, 1, 2
        else if (index < 7) return index - 3;   // Row 1: 0, 1, 2, 3
        else if (index < 12) return index - 7;  // Row 2: 0, 1, 2, 3, 4
        else if (index < 16) return index - 12; // Row 3: 0, 1, 2, 3
        else return index - 16;                 // Row 4: 0, 1, 2
    }

    // finds adjacent hex IDs for a vertex using the HEX_TO_VERTICES_MAP
    private static List<Integer> findAdjacentHexIdsForVertex(int vertexId) {
        List<Integer> adjacentHexes = new ArrayList<>();
        if (vertexId <= 0 || vertexId > NUM_VERTICES) return adjacentHexes; // Invalid vertex ID

        for (Map.Entry<Integer, int[]> entry : HEX_TO_VERTICES_MAP.entrySet()) {
            int hexId = entry.getKey();
            int[] verticesOnHex = entry.getValue();
            if (verticesOnHex != null) {
                for (int vIdOnHex : verticesOnHex) {
                    if (vIdOnHex == vertexId) {
                        adjacentHexes.add(hexId);
                        break; // found in this hex, move to next hex
                    }
                }
            }
        }
        return adjacentHexes;
    }

    // calculates vertex coordinates by averaging the relevant corners of adjacent hexes
    private static Point2D.Double calculateVertexCoordinates(int vertexId, List<Integer> adjacentHexIds, Map<Integer, Point2D.Double> hexCenterCoords) {
        if (adjacentHexIds == null || adjacentHexIds.isEmpty()) {
            logger.warn("[BoardGenerator_V_COORD] Vertex {} has no adjacent hex IDs listed. Cannot calculate coordinates.", vertexId);
            return new Point2D.Double(0, 0);
        }

        double sumX = 0;
        double sumY = 0;
        int contributingHexes = 0;

        for (int hexId : adjacentHexIds) {
            Point2D.Double hexCenter = hexCenterCoords.get(hexId);
            if (hexCenter == null) {
                logger.warn("[BoardGenerator_V_COORD] Missing center coordinate for adjacent hex {} of vertex {}. Skipping.", hexId, vertexId);
                continue;
            }

            int[] verticesOnThisHex = HEX_TO_VERTICES_MAP.get(hexId);
            if (verticesOnThisHex == null) {
                logger.warn("[BoardGenerator_V_COORD] Vertex list for adjacent hex {} not found. Skipping.", hexId);
                continue;
            }

            int cornerIndex = -1;
            for (int i = 0; i < verticesOnThisHex.length; i++) {
                if (verticesOnThisHex[i] == vertexId) {
                    cornerIndex = i;
                    break;
                }
            }

            if (cornerIndex != -1) {
                double angle_deg;
                switch (cornerIndex) {
                    case 0: angle_deg = -90; break;  // top
                    case 1: angle_deg = -30; break;  // top-right
                    case 2: angle_deg = 30; break;   // bottom-right
                    case 3: angle_deg = 90; break;   // bottom
                    case 4: angle_deg = 150; break;  // bottom-left
                    case 5: angle_deg = 210; break;  // top-left (or -150)
                    default: angle_deg = 0; break;  // should not happen
                }
                
                double angle_rad = Math.toRadians(angle_deg);
                sumX += hexCenter.getX() + TILE_SIZE * Math.cos(angle_rad);
                sumY += hexCenter.getY() + TILE_SIZE * Math.sin(angle_rad);
                contributingHexes++;
            }
        }

        if (contributingHexes > 0) {
            return new Point2D.Double(sumX / contributingHexes, sumY / contributingHexes);
        } else {
            logger.error("[BoardGenerator_V_COORD] CRITICAL: No contributing hexes found for Vertex {}. Returning origin.", vertexId);
            return new Point2D.Double(0, 0);
        }
    }
}