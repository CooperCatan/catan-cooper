import React, { useState, useEffect } from 'react';
import { Auth } from 'firebase/auth';

interface HexTile {
  id: number;
  type: 'desert' | 'wood' | 'brick' | 'ore' | 'wheat' | 'sheep';
  number?: number;
  hasRobber: boolean;
  x: number;
  y: number;
}

interface Port {
  type: 'any' | 'wood' | 'brick' | 'ore' | 'wheat' | 'sheep';
  position: 'N' | 'NE' | 'SE' | 'S' | 'SW' | 'NW';
  x: number;
  y: number;
  rotation: number;
}

interface Vertex {
  id: number;
  x: number;
  y: number;
  settlement?: {
    playerId: number;
    type: 'settlement' | 'city';
  };
  adjacentVertices?: number[];
}

interface Edge {
  id: number;
  connectedVertices: number[];
  road?: {
    playerId: number;
  };
  occupied?: boolean;
  ownerId?: number | null;
}

type ActionType = 'SETTLEMENT' | 'CITY' | 'ROAD' | 'TRADE' | 'DEV_CARD' | null;

interface GameBoardProps {
  gameId: number;
  accountId: number;
  auth: Auth;
  isSetupPhase: boolean;
  isCurrentTurn: boolean;
  onPlacementComplete: () => void;
  selectedAction: ActionType;
  onActionSelect: (locationId: string | number) => void;
  boardState: {
    hexes: {
      id: number;
      type: 'desert' | 'wood' | 'brick' | 'ore' | 'wheat' | 'sheep';
      number?: number;
      hasRobber: boolean;
      x: number;
      y: number;
    }[];
    vertices: {
      id: number;
      x: number;
      y: number;
      settlement?: {
        playerId: number;
        type: 'settlement' | 'city';
      };
      adjacentVertices?: number[];
    }[];
    edges: {
      id: number;
      connectedVertices: number[];
      road?: {
        playerId: number;
      };
      occupied?: boolean;
      ownerId?: number | null;
    }[];
  };
  gameConfig?: any;
  onSetupActionSuccess?: (data: any) => void;
}

const TILE_SIZE = 60;
const ROBBER_SIZE = TILE_SIZE;

// hex bg scaling factors
const RESOURCE_SCALE = {
  desert: 1.2,
  wood: 1.4,
  brick: 1.4,
  ore: 1.4,
  wheat: 1.4,
  sheep: 1.4,
  default: 1.4
};

const RESOURCE_IMAGES = {
  desert: '/resource-desert.png',
  wood: '/resource-wood.png',
  brick: '/resource-brick.png',
  ore: '/resource-ore.png',
  wheat: '/resource-wheat.png',
  sheep: '/resource-sheep.png',
};

const ROBBER_IMAGE = '/robber.png';

const PORTS: Port[] = [
  { type: 'any', position: 'N', x: 1.5, y: -0.5, rotation: 0 },
  { type: 'wood', position: 'NE', x: 3.5, y: 0, rotation: 60 },
  { type: 'brick', position: 'SE', x: 4.5, y: 2, rotation: 120 },
  { type: 'any', position: 'S', x: 2.5, y: 4.5, rotation: 180 },
  { type: 'wheat', position: 'SW', x: 0, y: 3, rotation: 240 },
  { type: 'sheep', position: 'NW', x: 0, y: 1, rotation: 300 },
];

const PORT_COLORS = {
  wood: '#27ae60',
  brick: '#c0392b',
  ore: '#7f8c8d',
  wheat: '#f1c40f',
  sheep: '#2ecc71',
  any: '#95a5a6',
};

const SETTLEMENT_SIZE = 20;

// Helper function to check distance rule for initial settlement placement
const isValidInitialSettlementPlacement = (vertexId: number, currentVertices: Vertex[]): boolean => {
  const vertex = currentVertices.find(v => v.id === vertexId);
  if (!vertex || vertex.settlement) { // Already occupied or doesn't exist
    return false;
  }

  // Check adjacent vertices for existing settlements
  // This requires knowing the graph structure (which vertices are adjacent to which)
  // Assuming Vertex object has an adjacentVertices: number[] property populated by backend or useEffect
  if (vertex.adjacentVertices && vertex.adjacentVertices.length > 0) {
    for (const adjVertexId of vertex.adjacentVertices) {
      const adjVertex = currentVertices.find(v => v.id === adjVertexId);
      if (adjVertex && adjVertex.settlement) {
        return false; // Adjacent vertex is occupied
      }
    }
  } else {
    // If adjacency info isn't directly on vertex, we might need a more complex graph traversal
    // or rely on the backend for this validation. For now, simple check.
    // console.warn(`[GameBoard] Vertex ${vertexId} has no adjacency information for distance rule check.`);
    // Fallback: For simplicity in this step, if no direct adjacency info, we assume true, 
    // but this needs to be robust based on actual vertex data structure from backend.
    // The backend *must* enforce this rule regardless of frontend highlighting.
  }
  return true;
};

export const PLAYER_COLORS: Record<number, string> = {
  1: '#FF6B6B', // Red (Player ID 1)
  2: '#4ECDC4', // Blue/Teal (Player ID 2)
  3: '#FFA07A', // Orange (LightSalmon) (Player ID 3)
  4: '#98D8AA'  // Green (Player ID 4)
};

const GameBoard: React.FC<GameBoardProps> = ({ 
  gameId, 
  accountId, 
  auth,
  isSetupPhase, 
  isCurrentTurn,
  onPlacementComplete,
  selectedAction,
  onActionSelect,
  boardState,
  gameConfig,
  onSetupActionSuccess,
}) => {
  const [selectedVertex, setSelectedVertex] = useState<number | null>(null);
  const [placementMode, setPlacementMode] = useState<'settlement' | 'road' | null>(null);
  const [showValidPlacements, setShowValidPlacements] = useState(true);

  const handleVertexClick = async (vertex: Vertex) => {
    if (isSetupPhase) {
      console.debug(`[GameBoard] Vertex ${vertex.id} clicked during setup phase by account ${accountId}.`);
      let numExistingSettlements = 0;
      boardState.vertices.forEach(v => {
        if (v.settlement && v.settlement.playerId === accountId) {
          numExistingSettlements++;
        }
      });
      const isSecondRound = numExistingSettlements === 1;
      console.debug(`[GameBoard] Existing settlements for account ${accountId}: ${numExistingSettlements}. Is second round: ${isSecondRound}`);

      try {
        const currentUser = auth.currentUser;
        if (!currentUser) {
          console.error("[GameBoard] No current user found on auth object for setup action.");
          return;
        }
        const token = await currentUser.getIdToken();
        if (!token) {
          console.error("[GameBoard] No auth token found for setup action.");
          return;
        }

        const response = await fetch(`http://localhost:8080/api/games/${gameId}/setup-action`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify({
            accountId: accountId, 
            actionType: 'SETTLEMENT',
            vertexId: vertex.id,
            isSecondRoundPlacement: isSecondRound
          })
        });

        if (response.ok) {
          const responseData = await response.json();
          console.info("[GameBoard] Initial settlement placement successful:", responseData);
          setSelectedVertex(vertex.id); 
          setPlacementMode('road');
          setShowValidPlacements(true); 
          if (onSetupActionSuccess) {
            onSetupActionSuccess(responseData); 
          }
        } else {
          const errorData = await response.json().catch(() => ({ message: 'Failed to place settlement' }));
          console.error("[GameBoard] Error placing initial settlement:", response.status, errorData);
          alert(`Error placing settlement: ${errorData.message || response.statusText}`);
        }
      } catch (error) {
        console.error('[GameBoard] Exception during initial settlement placement:', error);
        alert('An exception occurred while placing the settlement.');
      }
    } else if (selectedAction === 'SETTLEMENT' || selectedAction === 'CITY') {
      onActionSelect(vertex.id);
    }
  };

  const handleEdgeClick = async (edge: Edge) => {
    if (!isCurrentTurn || edge.road || edge.occupied) return;

    const v1Id = edge.connectedVertices?.[0];
    const v2Id = edge.connectedVertices?.[1];

    if (v1Id === undefined || v2Id === undefined) {
        console.error("[GameBoard] Edge object missing connectedVertices:", edge);
        return;
    }

    if (isSetupPhase && placementMode === 'road') {
      if (!selectedVertex || (v1Id !== selectedVertex && v2Id !== selectedVertex)) {
          console.debug(`[GameBoard] Edge ${edge.id} (${v1Id}-${v2Id}) not connected to selected vertex ${selectedVertex}`);
          return;
      }

      try {
        const currentUser = auth.currentUser;
        if (!currentUser) {
          console.error("[GameBoard] No current user found for setup road action.");
          return;
        }
        const token = await currentUser.getIdToken();
        if (!token) {
          console.error("[GameBoard] No auth token found for setup road action.");
          return;
        }

        const response = await fetch(`http://localhost:8080/api/games/${gameId}/setup-action`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
            'Authorization': `Bearer ${token}`,
          },
          body: JSON.stringify({
            accountId: accountId,
            actionType: 'ROAD',
            v1: v1Id,
            v2: v2Id
          })
        });

        if (response.ok) {
          const responseData = await response.json();
          console.info("[GameBoard] Initial road placement successful:", responseData);
          setSelectedVertex(null);
          setPlacementMode(null);
          setShowValidPlacements(false);
          if (onSetupActionSuccess) {
            onSetupActionSuccess(responseData);
          }
        } else {
          const errorData = await response.json().catch(() => ({ message: 'Failed to place road' }));
          console.error("[GameBoard] Error placing initial road:", response.status, errorData);
          alert(`Error placing road: ${errorData.message || response.statusText}`);
        }
      } catch (error) {
        console.error('[GameBoard] Exception during initial road placement:', error);
        alert('An exception occurred while placing the road.');
      }
    } else if (selectedAction === 'ROAD') {
      onActionSelect(edge.id);
    }
  };

  const getHexPathData = (hexTile: HexTile): string => {
    const pixelCenterX = hexTile.x;
    const pixelCenterY = hexTile.y;

    if (isNaN(pixelCenterX) || isNaN(pixelCenterY)) {
        console.error("[GameBoard] NaN detected for hex pixel center (from backend):", hexTile);
        return "M0,0";
    }

    let path = "M";
    for (let i = 0; i < 6; i++) {
      const angle_deg = 60 * i - 30;
      const angle_rad = Math.PI / 180 * angle_deg;
      const pointX = pixelCenterX + TILE_SIZE * Math.cos(angle_rad);
      const pointY = pixelCenterY + TILE_SIZE * Math.sin(angle_rad);
      if (isNaN(pointX) || isNaN(pointY)) {
        console.error("[GameBoard] NaN detected for hex corner point:", hexTile, "Corner:", i, "px:", pointX, "py:", pointY);
      }
      path += ` ${pointX} ${pointY}${i < 5 ? " L" : " Z"}`;
    }
    return path;
  };

  const getHexPoints = (x: number, y: number): string => {
    const centerX = x;
    const centerY = y;
    const points = [];
    for (let i = 0; i < 6; i++) {
      const angle = (60 * i - 30) * Math.PI / 180;
      points.push(
        centerX + TILE_SIZE * Math.cos(angle),
        centerY + TILE_SIZE * Math.sin(angle)
      );
    }
    return points.join(' ');
  };

  const renderPort = (port: Port) => {
    const centerX = port.x * TILE_SIZE + TILE_SIZE * 2;
    const centerY = port.y * TILE_SIZE * 0.75 + TILE_SIZE;
    
    return (
      <g key={`port-${port.position}`} transform={`rotate(${port.rotation} ${centerX} ${centerY})`}>
        <circle
          cx={centerX}
          cy={centerY}
          r={TILE_SIZE / 3}
          fill={PORT_COLORS[port.type]}
          stroke="#2c3e50"
          strokeWidth="2"
        />
        <text
          x={centerX}
          y={centerY}
          textAnchor="middle"
          dominantBaseline="middle"
          fill="#fff"
          fontSize="12"
          fontWeight="bold"
        >
          {port.type === 'any' ? '3:1' : '2:1'}
        </text>
      </g>
    );
  };

  const renderRobber = (tile: HexTile) => {
    if (!tile.hasRobber) return null;
    
    const centerX = tile.x;
    const centerY = tile.y;
    
    return (
      <image
        href={ROBBER_IMAGE}
        x={centerX - ROBBER_SIZE / 2}
        y={centerY - ROBBER_SIZE / 2}
        width={ROBBER_SIZE}
        height={ROBBER_SIZE}
        style={{ filter: 'drop-shadow(2px 2px 2px rgba(0,0,0,0.3))' }}
      />
    );
  };

  const renderSettlement = (vertex: Vertex, isValid: boolean) => {
    const settlementColor = vertex.settlement?.playerId && PLAYER_COLORS[vertex.settlement.playerId]
      ? PLAYER_COLORS[vertex.settlement.playerId]
      : '#E0E0E0';
    const isOwnedByCurrentUser = vertex.settlement?.playerId === accountId;

    return (
      <g 
        key={`vertex-${vertex.id}`}
        transform={`translate(${vertex.x}, ${vertex.y})`}
        onClick={() => handleVertexClick(vertex)}
        className={isValid && (selectedAction === 'SETTLEMENT' || selectedAction === 'CITY') ? "cursor-pointer hover:opacity-80" : "cursor-default"}
      >
        <title>Vertex ID: {vertex.id}{vertex.settlement ? ` (Owner: ${vertex.settlement.playerId})` : ''}</title>

        {vertex.settlement?.type === 'city' ? (
          <>
            <rect 
              x={-SETTLEMENT_SIZE / 1.5}
              y={-SETTLEMENT_SIZE / 1.5} 
              width={SETTLEMENT_SIZE * 1.33}
              height={SETTLEMENT_SIZE * 1.33}
              fill={settlementColor}
              stroke={isOwnedByCurrentUser ? "gold" : "black"} 
              strokeWidth={isOwnedByCurrentUser ? 3 : 1.5}
              rx="2"
            />
            <rect 
                x={-SETTLEMENT_SIZE / 2.5}
                y={-SETTLEMENT_SIZE / 0.9}
                width={SETTLEMENT_SIZE * 0.8}
                height={SETTLEMENT_SIZE * 0.8}
                fill={settlementColor}
                stroke={isOwnedByCurrentUser ? "gold" : "black"}
                strokeWidth={isOwnedByCurrentUser ? 3 : 1.5}
                rx="2"
            />
          </>
        ) : (
          <polygon 
            points={`0,-${SETTLEMENT_SIZE/2} ${SETTLEMENT_SIZE/2},0 0,${SETTLEMENT_SIZE/2} -${SETTLEMENT_SIZE/2},0`}
            fill={vertex.settlement ? settlementColor : (isValid ? '#FFFFAA' : 'white')}
            stroke={isOwnedByCurrentUser ? "gold" : "#666"} 
            strokeWidth="1.5"
            className={isValid ? 'opacity-70 hover:opacity-100' : 'opacity-50'}
          />
        )}
      </g>
    );
  };

  const renderRoad = (edge: Edge, isValid: boolean) => {
    const v1 = boardState.vertices.find(v => v.id === edge.connectedVertices[0]);
    const v2 = boardState.vertices.find(v => v.id === edge.connectedVertices[1]);

    if (!v1 || !v2) return null;

    const ownerId = edge.road?.playerId || edge.ownerId;
    const roadColor = ownerId && PLAYER_COLORS[ownerId]
      ? PLAYER_COLORS[ownerId]
      : '#A0A0A0';
    
    const isOwnedByCurrentUser = ownerId === accountId;

    return (
      <line
        key={`edge-${v1.id}-${v2.id}`}
        x1={v1.x}
        y1={v1.y}
        x2={v2.x}
        y2={v2.y}
        stroke={ownerId ? roadColor : (isValid && selectedAction === 'ROAD' ? '#FFFFAA' : '#CCC')}
        strokeWidth={ownerId || (isValid && selectedAction === 'ROAD') ? "10" : "8"}
        strokeLinecap="round"
        onClick={() => handleEdgeClick(edge)}
        className={isValid && selectedAction === 'ROAD' ? "cursor-pointer hover:opacity-80" : "cursor-default"}
        style={{ filter: isOwnedByCurrentUser ? 'url(#glow)' : 'none' }} 
      >
        <title>Edge ID: {edge.id}{ownerId ? ` (Owner: ${ownerId})` : ''}</title>
      </line>
    );
  };

  return (
    <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-blue-100/50 to-blue-200/50">
      <svg
        viewBox="-100 -100 1000 900"
        className="w-full h-full max-w-4xl"
      >
        <defs>
          {Object.entries(RESOURCE_IMAGES).map(([type, imagePath]) => {
            const scale = RESOURCE_SCALE[type as keyof typeof RESOURCE_SCALE] || RESOURCE_SCALE.default;
            return (
              <pattern
                key={type}
                id={`resource-${type}`}
                patternUnits="objectBoundingBox"
                width="1"
                height="1"
                preserveAspectRatio="xMidYMid slice"
              >
                <image
                  href={imagePath}
                  width={TILE_SIZE * scale}
                  height={TILE_SIZE * scale}
                  x={-TILE_SIZE * (scale - 1) / 2}
                  y={-TILE_SIZE * (scale - 1) / 2}
                  preserveAspectRatio="xMidYMid slice"
                />
              </pattern>
            );
          })}
          <filter id="glow" x="-50%" y="-50%" width="200%" height="200%">
            <feGaussianBlur stdDeviation="3.5" result="coloredBlur"/>
            <feMerge>
              <feMergeNode in="coloredBlur"/>
              <feMergeNode in="SourceGraphic"/>
            </feMerge>
          </filter>
        </defs>

        {PORTS.map(port => renderPort(port))}
        
        {boardState.hexes.map(tile => {
          const path = getHexPathData(tile);
          const centerX = tile.x;
          const centerY = tile.y;

          if (path.includes("NaN") || path.includes("undefined")) {
            console.error("[GameBoard] Invalid path generated for tile:", tile, "Path:", path);
            return null;
          }

          return (
            <g key={`hex-${tile.id}`} transform={`translate(0,0)`}> 
              <title>Hex ID: {tile.id} ({tile.type}{tile.number ? ` - ${tile.number}` : ''})</title>
              <path
                d={path}
                fill={`url(#resource-${tile.type})`}
                stroke="#2c3e50"
                strokeWidth="2"
                onClick={() => console.log('Hex clicked:', tile.id, 'Coords:', tile.x, tile.y)}
              />
              {tile.number && tile.type !== 'desert' && (
                <g>
                  <circle 
                      cx={centerX} 
                      cy={centerY} 
                      r={TILE_SIZE / 3}
                      fill="#fff"
                      stroke="#2c3e50" 
                      strokeWidth="2"
                  />
                  <text 
                      x={centerX} 
                      y={centerY} 
                      textAnchor="middle"
                      dominantBaseline="middle"
                      fill="#2c3e50"
                      fontSize={tile.number === 6 || tile.number === 8 ? "20" : "16"}
                      fontWeight="bold"
                      style={{ pointerEvents: 'none' }} 
                  >
                      {tile.number}
                  </text>
                </g>
              )}
              {tile.hasRobber && renderRobber(tile)}
            </g>
          );
        })}

        {isCurrentTurn && showValidPlacements && (
          <g>
            <rect
              x="250"
              y="10"
              width="300"
              height="40"
              rx="20"
              className="fill-white/20 backdrop-blur-sm"
            />
            <text
              x="400"
              y="35"
              textAnchor="middle"
              fill={PLAYER_COLORS[accountId] || '#95a5a6'}
              fontSize="20"
              fontWeight="bold"
              className="drop-shadow-sm"
            >
              {placementMode === 'road' ? 'Place a Road' : 'Place a Settlement'}
            </text>
          </g>
        )}

        {boardState.edges.map(edge => {
          const v1Id = edge.connectedVertices?.[0];
          const v2Id = edge.connectedVertices?.[1];
          
          const isValidRoadPlacement = 
            isSetupPhase && 
            isCurrentTurn && 
            placementMode === 'road' &&
            !edge.occupied &&
            showValidPlacements &&
            selectedVertex !== null &&
            (v1Id === selectedVertex || v2Id === selectedVertex);

          return renderRoad(edge, isValidRoadPlacement);
        })}

        {boardState.vertices.map(vertex => {
          const isActuallyValidPlacement = 
            isSetupPhase && 
            isCurrentTurn && 
            placementMode !== 'road' &&
            !vertex.settlement &&
            isValidInitialSettlementPlacement(vertex.id, boardState.vertices);
          
          return renderSettlement(vertex, isActuallyValidPlacement);
        })}
      </svg>
    </div>
  );
};

export default GameBoard; 