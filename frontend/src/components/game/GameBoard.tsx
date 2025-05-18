import React, { useState, useEffect } from 'react';
import { Auth } from 'firebase/auth';
import { getPlayerColor, DEFAULT_PLAYER_COLOR } from '../../utils/playerColors';

import desertImg from '../../assets/images/board-icons/resource-desert.png';
import woodImg from '../../assets/images/board-icons/resource-wood.png';
import brickImg from '../../assets/images/board-icons/resource-brick.png';
import oreImg from '../../assets/images/board-icons/resource-ore.png';
import wheatImg from '../../assets/images/board-icons/resource-wheat.png';
import sheepImg from '../../assets/images/board-icons/resource-sheep.png';
import robberImg from '../../assets/images/board-icons/robber.png';

interface HexTile {
  id: number;
  type: 'desert' | 'wood' | 'brick' | 'ore' | 'wheat' | 'sheep';
  number?: number;
  hasRobber: boolean;
  x: number;
  y: number;
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
  players: any[];
}

const TILE_SIZE = 60;
const HEX_RADIUS = TILE_SIZE;
const HEX_WIDTH = Math.sqrt(3) * HEX_RADIUS;
const HEX_HEIGHT = 2 * HEX_RADIUS;

const ROBBER_ICON_SIZE = HEX_RADIUS * 0.8;
const PIP_CIRCLE_RADIUS = HEX_RADIUS / 2.8; // radius for the white circle behind the pip number

const RESOURCE_TEXTURE_PATHS = {
  desert: desertImg,
  wood: woodImg,
  brick: brickImg,
  ore: oreImg,
  wheat: wheatImg,
  sheep: sheepImg,
};

const ROBBER_IMAGE_PATH = robberImg; // use imported image
// TODO: fix the robber image

const SETTLEMENT_SIZE = 20;

// helper function to check distance rule for initial settlement placement
const isValidInitialSettlementPlacement = (vertexId: number, currentVertices: Vertex[]): boolean => {
  const vertex = currentVertices.find(v => v.id === vertexId);
  if (!vertex || vertex.settlement) { // already occupied or doesn't exist
    return false;
  }

  // check adjacent vertices for existing settlements
  // this requires knowing the graph structure (which vertices are adjacent to which)
  // assuming Vertex object has an adjacentVertices: number[] property populated by backend or useEffect
  if (vertex.adjacentVertices && vertex.adjacentVertices.length > 0) {
    for (const adjVertexId of vertex.adjacentVertices) {
      const adjVertex = currentVertices.find(v => v.id === adjVertexId);
      if (adjVertex && adjVertex.settlement) {
        return false; // adjacent vertex is occupied
      }
    }
  } else {

  }
  return true;
};

// hlper function to get asterisk representation for pip values
const getPipAsterisks = (pipValue?: number): string => {
  if (!pipValue) return '';
  switch (pipValue) {
    case 2: case 12: return '*';
    case 3: case 11: return '**';
    case 4: case 10: return '***';
    case 5: case 9: return '****';
    case 6: case 8: return '*****';
    default: return '';
  }
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
  players,
}) => {
  const [selectedVertex, setSelectedVertex] = useState<number | null>(null);
  const [placementMode, setPlacementMode] = useState<'settlement' | 'road' | null>(null);
  const [showValidPlacements, setShowValidPlacements] = useState(true);

  const allXCoords = boardState.hexes.map(h => h.x).concat(boardState.vertices.map(v => v.x));
  const allYCoords = boardState.hexes.map(h => h.y).concat(boardState.vertices.map(v => v.y));
  
  const minX = Math.min(...allXCoords) - HEX_WIDTH; // add some padding
  const maxX = Math.max(...allXCoords) + HEX_WIDTH;
  const minY = Math.min(...allYCoords) - HEX_HEIGHT;
  const maxY = Math.max(...allYCoords) + HEX_HEIGHT;

  const boardPixelWidth = maxX - minX;
  const boardPixelHeight = maxY - minY;

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
    console.log(`[GameBoard] handleEdgeClick triggered for Edge ID: ${edge.id}, Current isTurn: ${isCurrentTurn}, placementMode: ${placementMode}, selectedAction: ${selectedAction}`);

    if (!isCurrentTurn || edge.road || edge.occupied) {
      console.log(`[GameBoard] handleEdgeClick: Not current turn OR edge already occupied. Edge ID: ${edge.id}`);
      return;
    }

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
            edgeId: edge.id.toString()
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

  const getHexagonPoints = (cx: number, cy: number, radius: number): string => {
    let points = "";
    for (let i = 0; i < 6; i++) {
      const angle_deg = 60 * i - 30;
      const angle_rad = Math.PI / 180 * angle_deg;
      points += (cx + radius * Math.cos(angle_rad)) + "," + (cy + radius * Math.sin(angle_rad)) + " ";
    }
    return points.trim();
  };

  const renderRobber = (tile: HexTile) => {
    if (!tile.hasRobber) return null;
    
    const centerX = tile.x;
    const centerY = tile.y;
    
    return (
      <image
        href={ROBBER_IMAGE_PATH}
        x={centerX - ROBBER_ICON_SIZE / 2}
        y={centerY - ROBBER_ICON_SIZE / 2}
        width={ROBBER_ICON_SIZE}
        height={ROBBER_ICON_SIZE}
        className="pointer-events-none"
      />
    );
  };

  const renderSettlement = (vertex: Vertex, finalIsValidSettlementPlacement: boolean) => {
    const settlementData = vertex.settlement;

    const ownerPlayer = settlementData ? players.find(p => p.id === settlementData.playerId) : null;
    const color = ownerPlayer?.color || DEFAULT_PLAYER_COLOR; 

    const isOwnedByCurrentUser = settlementData?.playerId === accountId;
    const S = SETTLEMENT_SIZE; 

    return (
      <g 
        key={`vertex-${vertex.id}`}
        transform={`translate(${vertex.x}, ${vertex.y})`}
        onClick={() => handleVertexClick(vertex)}
        className={finalIsValidSettlementPlacement ? "cursor-pointer hover:opacity-80" : "cursor-default"}
      >
        <title>Vertex ID: {vertex.id}{settlementData ? ` (Owner: ${settlementData.playerId}, Type: ${settlementData.type})` : ''}</title>

        {settlementData ? (
          settlementData.type === 'city' ? (
            <>
              {/* Existing City rendering (two rectangles) */}
              <rect 
                x={-S / 1.5}
                y={-S / 1.5} 
                width={S * 1.33}
                height={S * 1.33}
                fill={color}
                stroke={isOwnedByCurrentUser ? "gold" : "black"} 
                strokeWidth={isOwnedByCurrentUser ? 3 : 1.5}
                rx="2"
              />
              <rect 
                  x={-S / 2.5}
                  y={-S / 0.9}
                  width={S * 0.8}
                  height={S * 0.8}
                  fill={color}
                  stroke={isOwnedByCurrentUser ? "gold" : "black"}
                  strokeWidth={isOwnedByCurrentUser ? 3 : 1.5}
                  rx="2"
              />
            </>
          ) : settlementData.type === 'settlement' ? (
            // TODO: fix this, this doesnt even display
            // New Settlement rendering (House shape)
            <>
              <rect // House base
                x={-S / 2.2}
                y={-S / 5} // Shift base down slightly so roof point is higher
                width={S * 0.9} // Slightly narrower base
                height={S / 2}
                fill={color}
                stroke={isOwnedByCurrentUser ? "gold" : "#333"}
                strokeWidth="1.5"
              />
              <polygon // house roof
                points={`0,-${S/1.8} -${S/2.2},-${S/5} ${S/2.2},-${S/5}`}
                fill={color}
                stroke={isOwnedByCurrentUser ? "gold" : "#333"}
                strokeWidth="1.5"
              />
            </>
          )
          : null // should not happen if settlementData exists
          // TODO; fix settlement display on vertices
        ) : finalIsValidSettlementPlacement ? (
          // Placeholder for valid empty spot (existing diamond)
          <polygon 
            points={`0,-${S/2} ${S/2},0 0,${S/2} -${S/2},0`}
            fill={'#FFFFE0'} // Brighter Yellow for valid
            stroke={"#BDB76B"} // Darker yellow border for valid
            strokeWidth="1.5"
            className={'opacity-100 cursor-pointer hover:opacity-90'} // Already has cursor-pointer here
          />
        ) : (
          // Optional: Render a non-interactive placeholder for empty spots not valid for current action
          <polygon 
            points={`0,-${S/2} ${S/2},0 0,${S/2} -${S/2},0`}
            fill={'rgba(255, 255, 255, 0.2)'} 
            stroke={"#CCC"} 
            strokeWidth="0.5"
            className={'opacity-30 cursor-default'}
          />
        )
        }
      </g>
    );
  };

  const renderRoad = (edge: Edge, isValid: boolean) => {
    const roadData = edge.road;
    if (!isValid && !roadData && !edge.ownerId) {
      return null;
    }

    const ownerId = roadData ? roadData.playerId : edge.ownerId;
    const ownerPlayer = players.find(p => p.id === ownerId);
    const isOwnedByCurrentUser = ownerId === accountId;

    const v1 = boardState.vertices.find(v => v.id === edge.connectedVertices[0]);
    const v2 = boardState.vertices.find(v => v.id === edge.connectedVertices[1]);

    if (!v1 || !v2) return null;

    let strokeColor = ownerPlayer?.color || DEFAULT_PLAYER_COLOR;
    let currentStrokeWidth = 10;
    let classForRoad = "cursor-default";

    if (isValid && !ownerId && !roadData) {
      // This is a valid, unbuilt road. Make it visually distinct and clickable.
      strokeColor = "rgba(128, 128, 128, 0.6)"; // Semi-transparent grey for placement spots
      currentStrokeWidth = 12; // Slightly thicker to ensure clickability
      classForRoad = "cursor-pointer hover:opacity-100"; // Ensure full opacity on hover over the placeholder
    } else if (ownerId) {
      // This is an existing, owned road.
      // strokeColor is already set to ownerPlayer.color or default
      // currentStrokeWidth is 10
      // classForRoad is cursor-default
    } else {
      // This case should ideally not be hit if the initial check `!isValid && !roadData && !edge.ownerId` is working.
      // Or it's an unowned, invalid spot, which shouldn't be interactive.
      return null; // Or render a very faint, non-interactive line if desired for debugging
    }

    return (
      <line
        key={`edge-${v1.id}-${v2.id}`}
        x1={v1.x}
        y1={v1.y}
        x2={v2.x}
        y2={v2.y}
        stroke={strokeColor}
        strokeWidth={currentStrokeWidth}
        strokeLinecap="round"
        onClick={() => handleEdgeClick(edge)}
        className={classForRoad}
        style={{ filter: isOwnedByCurrentUser ? 'url(#glow)' : 'none' }} 
      >
        <title>Edge ID: {edge.id}{ownerId ? ` (Owner: ${ownerId})` : ''}</title>
      </line>
    );
  };

  return (
    <div className="w-full h-full flex items-center justify-center bg-blue-200/30 p-4 overflow-auto">
      <svg
        viewBox={`${minX} ${minY} ${boardPixelWidth} ${boardPixelHeight}`} 
        className="max-w-full max-h-full"
        preserveAspectRatio="xMidYMid meet"
      >
        <defs>
          {boardState.hexes.map(hex => (
            <clipPath key={`clip-${hex.id}`} id={`hexClipPath-${hex.id}`}>
              <polygon points={getHexagonPoints(hex.x, hex.y, HEX_RADIUS)} />
            </clipPath>
          ))}
        </defs>

        {/* Render Hexes */}
        {boardState.hexes.map(hex => {
          const hexPoints = getHexagonPoints(hex.x, hex.y, HEX_RADIUS);
          const imageX = hex.x - HEX_WIDTH / 2;
          const imageY = hex.y - HEX_HEIGHT / 2;
          const resourceImagePath = RESOURCE_TEXTURE_PATHS[hex.type as keyof typeof RESOURCE_TEXTURE_PATHS]; // Added type assertion

          return (
            <g key={`hex-group-${hex.id}`}>
              <polygon 
                points={hexPoints} 
                fill={hex.type === 'desert' ? '#D2B48C' : 'transparent'} // Tan for desert, transparent for image hexes
                stroke="#666" // Darker stroke for better visibility
                strokeWidth="1.5"
              />
              {/* Render image only if it's not a desert tile AND a path exists */}
              {hex.type !== 'desert' && resourceImagePath && (
                <image 
                  href={resourceImagePath} 
                  x={imageX} 
                  y={imageY} 
                  width={HEX_WIDTH}
                  height={HEX_HEIGHT}
                  clipPath={`url(#hexClipPath-${hex.id})`}
                  preserveAspectRatio="xMidYMid slice" // Changed back to slice to fill hex
                />
              )}
              {/* Pip Number Display with White Circle Background */}
              {hex.number && hex.type !== 'desert' && (
                <g opacity={0.85}>
                  <circle 
                    cx={hex.x}
                    cy={hex.y}
                    r={PIP_CIRCLE_RADIUS}
                    fill="white"
                    stroke="#CCC" // Light grey stroke for the circle
                    strokeWidth="1"
                  />
                  <text 
                    x={hex.x} 
                    y={hex.y - (PIP_CIRCLE_RADIUS / 4)} // Position main number slightly up
                      textAnchor="middle"
                      dominantBaseline="middle"
                    fontSize={PIP_CIRCLE_RADIUS * 0.9} // Adjust font size relative to circle
                      fontWeight="bold"
                    fill={hex.number === 6 || hex.number === 8 ? '#D32F2F' : '#212121'} // Red for 6/8, dark grey otherwise
                    className="select-none pointer-events-none"
                  >
                    {hex.number}
                  </text>
                  <text
                    x={hex.x}
                    y={hex.y + (PIP_CIRCLE_RADIUS / 2.5)} // Position pips slightly down
                    textAnchor="middle"
                    dominantBaseline="middle"
                    fontSize={PIP_CIRCLE_RADIUS * 0.45} // Smaller font size for pips
                    fill={hex.number === 6 || hex.number === 8 ? '#D32F2F' : '#555555'} // Red for 6/8 pips, slightly lighter grey otherwise
                    className="select-none pointer-events-none"
                  >
                    {getPipAsterisks(hex.number)}
                  </text>
                </g>
              )}
              {hex.hasRobber && (
                <image 
                  href={ROBBER_IMAGE_PATH} // This will now be the imported image variable
                  x={hex.x - ROBBER_ICON_SIZE / 2} 
                  y={hex.y - ROBBER_ICON_SIZE / 2} 
                  width={ROBBER_ICON_SIZE}
                  height={ROBBER_ICON_SIZE}
                  className="pointer-events-none drop-shadow-lg"
                />
              )}
            </g>
          );
        })}

        {/* Render Settlements */}
        {boardState.vertices.map(vertex => {
          const isValidForSetup = 
            isSetupPhase && 
            isCurrentTurn && 
            (placementMode === 'settlement' || placementMode === null) && // Explicitly check for settlement mode or initial (null)
            !vertex.settlement &&
            isValidInitialSettlementPlacement(vertex.id, boardState.vertices);

          const isValidForGame = 
            !isSetupPhase &&
            isCurrentTurn &&
            selectedAction === 'SETTLEMENT' &&
            !vertex.settlement &&
            isValidInitialSettlementPlacement(vertex.id, boardState.vertices); // Assuming distance rule still applies
          
          const finalIsValidSettlementPlacement = isValidForSetup || isValidForGame;
          
          return renderSettlement(vertex, finalIsValidSettlementPlacement);
        })}

        {/* Render Edges (Roads) AFTER Settlements to ensure they are on top for click events */}
        {boardState.edges.map(edge => {
          const v1Id = edge.connectedVertices?.[0];
          const v2Id = edge.connectedVertices?.[1];
          
          const isValidForSetup = 
            isSetupPhase && 
            isCurrentTurn && 
            placementMode === 'road' &&
            !edge.road &&
            showValidPlacements &&
            selectedVertex !== null &&
            (v1Id === selectedVertex || v2Id === selectedVertex);

          if (isSetupPhase && placementMode === 'road') {
            console.log(
              `[GameBoard RoadEval Edge ID: ${edge.id}] ` +
              `isSetup: ${isSetupPhase}, isTurn: ${isCurrentTurn}, pMode: ${placementMode}, ` +
              `selVtx: ${selectedVertex}, edgeOccupied: ${!!edge.road}, showValid: ${showValidPlacements}, ` +
              `v1: ${v1Id}, v2: ${v2Id}, connects: ${(v1Id === selectedVertex || v2Id === selectedVertex)}, ` +
              `FINAL_isValidForSetup: ${isValidForSetup}`
            );
          }

          const isValidForGame = 
            !isSetupPhase &&
            isCurrentTurn &&
            selectedAction === 'ROAD' &&
            !edge.road;

          const finalIsValidRoadPlacement = isValidForSetup || isValidForGame;

          return renderRoad(edge, finalIsValidRoadPlacement);
        })}

        {isCurrentTurn && showValidPlacements && (() => {
          let currentPlayerTextColor = DEFAULT_PLAYER_COLOR;
          if (gameConfig && gameConfig.playerList && accountId) {
            const playerIndex = gameConfig.playerList.indexOf(accountId);
            if (playerIndex !== -1) {
              currentPlayerTextColor = getPlayerColor(playerIndex + 1);
            }
          }

          return (
            <g>
              <rect
                x="250"
                y="-85"
                width="300"
                height="45"
                rx="22.5"
                fill="rgba(239, 246, 255, 0.85)"
                stroke="rgba(147, 197, 253, 0.9)"
                strokeWidth="2"
              />
              <text
                x="400"
                y="-62.5"
                textAnchor="middle"
                dominantBaseline="middle"
                fill={currentPlayerTextColor}
                fontSize="20"
                fontWeight="bold"
                className="drop-shadow-md"
              >
                {placementMode === 'road' ? 'Place a Road' : 'Place a Settlement'}
              </text>
            </g>
          );
        })()}
      </svg>
    </div>
  );
};

export default GameBoard; 