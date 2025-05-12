package catan;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.awt.geom.Point2D;
import java.util.*;

// Class responsible for generating the initial Catan board state
public class BoardGenerator {

    private static final Logger logger = LoggerFactory.getLogger(BoardGenerator.class);

    // --- Board Structure Constants ---
    public static final int NUM_HEXES = 19;
    public static final int NUM_VERTICES = 54; // Standard Catan Board
    public static final int NUM_EDGES = 72;    // Standard Catan Board

    // --- Resource and Pip Constants ---
    private static final int WOOD_COUNT = 4;
    private static final int WHEAT_COUNT = 4;
    private static final int SHEEP_COUNT = 4;
    private static final int BRICK_COUNT = 3;
    private static final int ORE_COUNT = 3;
    private static final String DESERT_TYPE = "desert";
    private static final Integer[] PIP_VALUES = {2, 3, 3, 4, 4, 5, 5, 6, 6, 8, 8, 9, 9, 10, 10, 11, 11, 12}; // Standard pips (18 total)
    private static final int DESERT_HEX_ID = 10; // Fixed ID for the desert hex (adjust if topology changes)

    // --- Coordinate Calculation Constants ---
    // These constants define the geometry for SVG rendering and dynamic coordinate calculation
    private static final double TILE_SIZE = 60.0; // Effectively the radius from hex center to a corner
    private static final double H_DIST = Math.sqrt(3.0) * TILE_SIZE; // Horizontal distance between hex centers (sqrt(3)*TILE_SIZE)
    private static final double V_DIST = 1.5 * TILE_SIZE; // Vertical distance between hex rows (center to center)

    // These offsets are used when calculating pixel centers based on a grid layout
    private static final double RENDER_OFFSET_X = TILE_SIZE * 2; // Start rendering slightly offset from SVG edge
    private static final double RENDER_OFFSET_Y = TILE_SIZE * 1.5;

    // --- Predefined Board Topology Maps ---
    // !! IMPORTANT: THESE ARE STILL PLACEHOLDERS AND MUST BE FILLED MANUALLY !!
    // Edge ID (1-72) -> {Vertex ID 1 (1-54), Vertex ID 2 (1-54)}
    private static final Map<Integer, int[]> EDGE_TO_VERTICES_MAP = new HashMap<>();
    // Hex ID (1-19) -> {6 Vertex IDs (1-54) in clockwise order, e.g., starting Top}
    private static final Map<Integer, int[]> HEX_TO_VERTICES_MAP = new HashMap<>();

    // --- Static Initializer for Topology Maps ---
    static {
        logger.info("[BoardGenerator_STATIC_INIT] Initializing topology maps (PLACEHOLDER DATA)...");
        long startTime = System.currentTimeMillis();
        try {
            HEX_TO_VERTICES_MAP.clear();
            EDGE_TO_VERTICES_MAP.clear();

            // --- Populate Standard Topology Maps (Using standard 1-based IDs) ---
            // !! THIS IS PLACEHOLDER DATA - NEEDS TO BE FILLED MANUALLY !!
            // Define Hex -> Vertices mapping (Example: Clockwise starting Top)
            HEX_TO_VERTICES_MAP.put(1, new int[]{3, 4, 5, 13, 12, 11}); // Example (Verify!)
            HEX_TO_VERTICES_MAP.put(2, new int[]{5, 6, 7, 15, 14, 13});
            // ... add all 19 hexes ...
            HEX_TO_VERTICES_MAP.put(10, new int[]{25, 26, 27, 37, 36, 35}); // Example for Desert Hex ID 10
            HEX_TO_VERTICES_MAP.put(19, new int[]{47, 48, 49, 50, -1, 54}); // Example border, fix -1

            // Define Edge -> Vertices mapping (Ensure all 72 edges are defined)
            // !! THIS IS PLACEHOLDER DATA - NEEDS TO BE FILLED MANUALLY !!
            EDGE_TO_VERTICES_MAP.put(1, new int[]{3, 4});
            EDGE_TO_VERTICES_MAP.put(2, new int[]{4, 5});
            // ... add all 72 edges ...
            EDGE_TO_VERTICES_MAP.put(72, new int[]{53, 54}); // Example last edge

            logger.info("[BoardGenerator_STATIC_INIT] Populated HEX_TO_VERTICES_MAP with {} entries.", HEX_TO_VERTICES_MAP.size());
            logger.info("[BoardGenerator_STATIC_INIT] Populated EDGE_TO_VERTICES_MAP with {} entries.", EDGE_TO_VERTICES_MAP.size());

            verifyTopologyMaps();

            logger.info("[BoardGenerator_STATIC_INIT] Static initialization finished in {} ms.", (System.currentTimeMillis() - startTime));

        } catch (Exception e) {
            logger.error("[BoardGenerator_STATIC_INIT] CRITICAL EXCEPTION during static map initialization: {}", e.getMessage(), e);
            throw new ExceptionInInitializerError(e); // Halt application startup
        }
    }

    // Inner class to hold the generated board data
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

    // --- Public Method to Generate Board ---
    public static BoardData generateNewBoard() {
        logger.info("[BoardGenerator] Starting initial board generation.");
        List<Hex> localHexes = new ArrayList<>();
        List<Vertex> localVertices = new ArrayList<>();
        List<Edge> localEdges = new ArrayList<>();
        Map<Integer, Point2D.Double> hexCenterCoords = new HashMap<>(); // To store calculated hex centers for vertex calculation
        int initialRobberHexId = DESERT_HEX_ID; // Robber starts on the desert

        // --- Prepare shuffled resources and pips ---
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
            // --- 1. Generate Hexes (ID, Type, Pips, Coordinates) ---
            logger.debug("[BoardGenerator] Generating {} hexes...", NUM_HEXES);
            for (int i = 0; i < NUM_HEXES; i++) {
                int hexId = i + 1;
                Hex hex = new Hex();
                hex.setId(hexId);

                // Assign Type, Pip, Robber (Fixed Desert)
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

                // Calculate and store hex CENTER pixel coordinates (for rendering and vertex calculation)
                // Using pointy-top hex grid layout logic
                double gridRow = calculateHexGridRow(hexId - 1); // 0-indexed
                double gridCol = calculateHexGridCol(hexId - 1); // 0-indexed

                // Calculate center X, Y based on grid position, staggering odd rows
                double pixelCenterX = RENDER_OFFSET_X + gridCol * H_DIST + (gridRow % 2 != 0 ? H_DIST / 2.0 : 0); // Stagger rows based on standard grid layout
                double pixelCenterY = RENDER_OFFSET_Y + gridRow * V_DIST;

                hex.setX(pixelCenterX); // Store actual pixel X for frontend rendering
                hex.setY(pixelCenterY); // Store actual pixel Y for frontend rendering
                hexCenterCoords.put(hexId, new Point2D.Double(pixelCenterX, pixelCenterY)); // Store for vertex calculation
                localHexes.add(hex);
            }
            logger.info("[BoardGenerator] Generated {} hexes. Desert at Hex ID: {}. Hex centers calculated.",
                        localHexes.size(), DESERT_HEX_ID);

            // --- 2. Generate Vertices (ID, Dynamic Coordinates, Adjacency placeholders) ---
            logger.debug("[BoardGenerator] Generating {} vertices and calculating coordinates...", NUM_VERTICES);
            for (int i = 0; i < NUM_VERTICES; i++) {
                int vertexId = i + 1;
                Vertex vertex = new Vertex();
                vertex.setId(vertexId);
                vertex.setOccupied(false);
                vertex.setBuildingType(null);
                vertex.setOwnerId(null);

                // Find adjacent hexes using the topology map
                List<Integer> adjHexIds = findAdjacentHexIdsForVertex(vertexId);
                // Calculate coordinates by averaging adjacent hex corners corresponding to this vertex
                Point2D.Double coords = calculateVertexCoordinates(vertexId, adjHexIds, hexCenterCoords);

                if (coords != null) {
                    vertex.setX(coords.getX());
                    vertex.setY(coords.getY());
                } else {
                    logger.warn("[BoardGenerator] Could not calculate coordinates for Vertex ID: {}. Setting to (0,0). Check topology maps.", vertexId);
                    vertex.setX(0.0); vertex.setY(0.0); // Fallback
                }
                vertex.setAdjacentHexes(adjHexIds); // Store hexes it borders
                vertex.setConnectedEdges(new ArrayList<>()); // Init empty, populated next
                vertex.setAdjacentVertices(new ArrayList<>());// Init empty, populated next
                localVertices.add(vertex);
            }
            logger.debug("[BoardGenerator] {} vertices generated with dynamic coordinates.", localVertices.size());

            // --- 3. Generate Edges (ID, Connected Vertices from map) ---
            logger.debug("[BoardGenerator] Generating {} edges and assigning vertex connections...", NUM_EDGES);
            for (int i = 0; i < NUM_EDGES; i++) {
                int edgeId = i + 1;
                Edge edge = new Edge();
                edge.setId(edgeId);
                edge.setOccupied(false);
                edge.setOwnerId(null);

                int[] connectedVIds = EDGE_TO_VERTICES_MAP.get(edgeId);
                if (connectedVIds != null && connectedVIds.length == 2 && connectedVIds[0] > 0 && connectedVIds[1] > 0) {
                    // Convert int array to List<Integer>
                     edge.setConnectedVertices(Arrays.asList(connectedVIds[0], connectedVIds[1]));
                } else {
                    logger.error("[BoardGenerator] CRITICAL: Edge {} has missing, invalid, or placeholder vertex connections in EDGE_TO_VERTICES_MAP. Connected: {}. Topology map likely incomplete or incorrect!",
                                 edgeId, Arrays.toString(connectedVIds));
                    edge.setConnectedVertices(new ArrayList<>()); // Assign empty list
                }
                localEdges.add(edge);
            }
            logger.debug("[BoardGenerator] {} edges generated.", localEdges.size());

            // --- 4. Populate Remaining Adjacency Info (Vertex -> Edges, Vertex -> Vertices) ---
            // Iterate through vertices and use the generated edges to find connections
            logger.debug("[BoardGenerator] Populating remaining adjacency info for vertices (connectedEdges, adjacentVertices)...");
            for (Vertex vertex : localVertices) {
                int currentVertexId = vertex.getId();
                List<Integer> edgesTouchingVertex = new ArrayList<>();
                List<Integer> verticesAdjacentToVertex = new ArrayList<>();

                for (Edge edge : localEdges) {
                    List<Integer> edgeVerts = edge.getConnectedVertices();
                    if (edgeVerts != null && edgeVerts.contains(currentVertexId)) {
                        edgesTouchingVertex.add(edge.getId());
                        // Find the *other* vertex on this edge
                        for (int connectedVId : edgeVerts) {
                            if (connectedVId != currentVertexId && !verticesAdjacentToVertex.contains(connectedVId)) {
                                verticesAdjacentToVertex.add(connectedVId);
                            }
                        }
                    }
                }
                 // Sort adjacency lists for consistency, if desired
                 // Collections.sort(edgesTouchingVertex);
                 // Collections.sort(verticesAdjacentToVertex);
                vertex.setConnectedEdges(edgesTouchingVertex);
                vertex.setAdjacentVertices(verticesAdjacentToVertex);
            }
            logger.debug("[BoardGenerator] Adjacency info (connectedEdges, adjacentVertices) populated for all vertices.");

            logger.info("[BoardGenerator] Board generation complete. Hexes: {}, Vertices: {}, Edges: {}. Robber initially at Hex: {}",
                    localHexes.size(), localVertices.size(), localEdges.size(), initialRobberHexId);
            return new BoardData(localHexes, localVertices, localEdges, initialRobberHexId);

        } catch (Exception e) {
            logger.error("[BoardGenerator] CRITICAL EXCEPTION during board generation: {}", e.getMessage(), e);
            // Return empty board data or throw to indicate failure
            return new BoardData(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), -1); // Indicate error
        }
    }

    // --- Private Helper Methods for Generation ---

    // Calculates the logical grid ROW (0-4) for a given 0-indexed hex ID
    private static int calculateHexGridRow(int index) {
        if (index < 3) return 0;       // Row 0 (Hexes 0-2)
        else if (index < 7) return 1;   // Row 1 (Hexes 3-6)
        else if (index < 12) return 2;  // Row 2 (Hexes 7-11)
        else if (index < 16) return 3;  // Row 3 (Hexes 12-15)
        else return 4;                  // Row 4 (Hexes 16-18)
    }

    // Calculates the logical grid COLUMN index within its row for a 0-indexed hex ID
    private static int calculateHexGridCol(int index) {
        if (index < 3) return index;            // Row 0: 0, 1, 2
        else if (index < 7) return index - 3;   // Row 1: 0, 1, 2, 3
        else if (index < 12) return index - 7;  // Row 2: 0, 1, 2, 3, 4
        else if (index < 16) return index - 12; // Row 3: 0, 1, 2, 3
        else return index - 16;                 // Row 4: 0, 1, 2
    }

    // Finds adjacent hex IDs for a vertex using the HEX_TO_VERTICES_MAP
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
                        break; // Found in this hex, move to next hex
                    }
                }
            }
        }
        return adjacentHexes;
    }

    // Calculates vertex coordinates by averaging the relevant corners of adjacent hexes
    private static Point2D.Double calculateVertexCoordinates(int vertexId, List<Integer> adjacentHexIds, Map<Integer, Point2D.Double> hexCenterCoords) {
        if (adjacentHexIds == null || adjacentHexIds.isEmpty()) {
            logger.warn("[BoardGenerator_V_COORD] Vertex {} has no adjacent hex IDs listed. Cannot calculate coordinates.", vertexId);
            return new Point2D.Double(0, 0); // Fallback
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

            // Find which corner this vertex represents for the current hex
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
                // Calculate the coordinate of that specific corner of the hex
                // Angle calculation needs to be consistent with the vertex ordering in HEX_TO_VERTICES_MAP
                // Assuming clockwise order starting top: 0=Top, 1=TopRight, 2=BottomRight, 3=Bottom, 4=BottomLeft, 5=TopLeft
                // Angle = 60 * cornerIndex + 90 (if 0 is Top vertex, adjust as needed)
                 // OR simpler: use the frontend's angle system: angle_deg = 60 * i - 30 (if map matches FE corners)
                double angle_rad = Math.toRadians(60 * cornerIndex - 30); // Match FE rendering corner angles

                sumX += hexCenter.getX() + TILE_SIZE * Math.cos(angle_rad);
                sumY += hexCenter.getY() + TILE_SIZE * Math.sin(angle_rad);
                contributingHexes++;
            } else {
                 logger.warn("[BoardGenerator_V_COORD] Vertex {} not found in vertex list for adjacent hex {}.", vertexId, hexId);
            }
        }

        if (contributingHexes > 0) {
            return new Point2D.Double(sumX / contributingHexes, sumY / contributingHexes);
        } else {
            logger.error("[BoardGenerator_V_COORD] CRITICAL: No contributing hexes found for Vertex {}. Returning origin.", vertexId);
            return new Point2D.Double(0, 0); // Fallback
        }
    }

    // --- Private Helper: Topology Verification ---
    private static void verifyTopologyMaps() {
        logger.info("[BoardGenerator_VERIFY] Verifying topology map consistency (PLACEHOLDER DATA)...");
        boolean errorsFound = false;

        // Basic size checks (more detailed checks need complete maps)
        if (HEX_TO_VERTICES_MAP.isEmpty() || EDGE_TO_VERTICES_MAP.isEmpty()) {
             logger.warn("[BoardGenerator_VERIFY] WARN: Topology maps appear empty or incomplete (placeholders). Verification will be limited.");
             // Don't set errorsFound = true for placeholder stage
        } else {
            if (HEX_TO_VERTICES_MAP.size() != NUM_HEXES) {
                 logger.error("[BoardGenerator_VERIFY] FAIL: HEX_TO_VERTICES_MAP size ({}) != NUM_HEXES ({})", HEX_TO_VERTICES_MAP.size(), NUM_HEXES);
                errorsFound = true;
            }
             if (EDGE_TO_VERTICES_MAP.size() != NUM_EDGES) {
                 logger.error("[BoardGenerator_VERIFY] FAIL: EDGE_TO_VERTICES_MAP size ({}) != NUM_EDGES ({})", EDGE_TO_VERTICES_MAP.size(), NUM_EDGES);
                 errorsFound = true; // Expect exactly 72 edges
             }
        }

        // Placeholder validation (can add more checks once maps are filled)
        // ... (Add more detailed checks like vertex degree, shared edges consistency later) ...

        if (errorsFound) {
            logger.error("[BoardGenerator_VERIFY] Topology verification FAILED based on current data.");
            // Throw error only if maps are expected to be complete
            // throw new ExceptionInInitializerError("Failed topology map verification.");
        } else {
            logger.info("[BoardGenerator_VERIFY] Topology verification PASSED (basic checks on potentially incomplete data).");
        }
    }
}