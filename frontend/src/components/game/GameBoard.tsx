import React, { useState, useEffect } from 'react';

interface HexTile {
  id: number;
  type: 'desert' | 'wood' | 'brick' | 'ore' | 'wheat' | 'wool';
  number?: number;
  hasRobber: boolean;
  x: number;
  y: number;
}

interface Port {
  type: 'any' | 'wood' | 'brick' | 'ore' | 'wheat' | 'wool';
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
  };
}

interface Edge {
  id: string;
  v1: number;
  v2: number;
  road?: {
    playerId: number;
  };
}

interface GameBoardProps {
  gameId: number;
  accountId: number;
  isSetupPhase: boolean;
  isCurrentTurn: boolean;
  onPlacementComplete: () => void;
}

const TILE_SIZE = 60;
const HEX_HEIGHT = TILE_SIZE * 2;
const HEX_WIDTH = Math.sqrt(3) * TILE_SIZE;
const ROBBER_SIZE = TILE_SIZE;

// hex bg scaling factors
const RESOURCE_SCALE = {
  default: 1.4,

};

const RESOURCE_IMAGES = {
  desert: '/resource-desert.png',
  wood: '/resource-wood.png',
  brick: '/resource-brick.png',
  ore: '/resource-ore.png',
  wheat: '/resource-wheat.png',
  wool: '/resource-sheep.png',
};

const ROBBER_IMAGE = '/robber.png';

const TILES: HexTile[] = [
  { id: 1, type: 'ore', number: 10, hasRobber: false, x: 1, y: 0 },
  { id: 2, type: 'wool', number: 2, hasRobber: false, x: 2, y: 0 },
  { id: 3, type: 'wood', number: 9, hasRobber: false, x: 3, y: 0 },
  
  { id: 4, type: 'wheat', number: 12, hasRobber: false, x: 0.5, y: 1 },
  { id: 5, type: 'brick', number: 6, hasRobber: false, x: 1.5, y: 1 },
  { id: 6, type: 'wool', number: 4, hasRobber: false, x: 2.5, y: 1 },
  { id: 7, type: 'wheat', number: 10, hasRobber: false, x: 3.5, y: 1 },
  
  { id: 8, type: 'wood', number: 9, hasRobber: false, x: 0, y: 2 },
  { id: 9, type: 'desert', hasRobber: true, x: 1, y: 2 },
  { id: 10, type: 'ore', number: 3, hasRobber: false, x: 2, y: 2 },
  { id: 11, type: 'wood', number: 8, hasRobber: false, x: 3, y: 2 },
  { id: 12, type: 'brick', number: 5, hasRobber: false, x: 4, y: 2 },
  
  { id: 13, type: 'ore', number: 8, hasRobber: false, x: 0.5, y: 3 },
  { id: 14, type: 'wheat', number: 5, hasRobber: false, x: 1.5, y: 3 },
  { id: 15, type: 'brick', number: 11, hasRobber: false, x: 2.5, y: 3 },
  { id: 16, type: 'wool', number: 3, hasRobber: false, x: 3.5, y: 3 },
  
  { id: 17, type: 'wood', number: 4, hasRobber: false, x: 1, y: 4 },
  { id: 18, type: 'wool', number: 6, hasRobber: false, x: 2, y: 4 },
  { id: 19, type: 'wheat', number: 11, hasRobber: false, x: 3, y: 4 },
];

const PORTS: Port[] = [
  { type: 'any', position: 'N', x: 1.5, y: -0.5, rotation: 0 },
  { type: 'wood', position: 'NE', x: 3.5, y: 0, rotation: 60 },
  { type: 'brick', position: 'SE', x: 4.5, y: 2, rotation: 120 },
  { type: 'any', position: 'S', x: 2.5, y: 4.5, rotation: 180 },
  { type: 'wheat', position: 'SW', x: 0, y: 3, rotation: 240 },
  { type: 'any', position: 'NW', x: 0, y: 1, rotation: 300 },
];

const PORT_COLORS = {
  wood: '#27ae60',
  brick: '#c0392b',
  ore: '#7f8c8d',
  wheat: '#f1c40f',
  wool: '#2ecc71',
  any: '#95a5a6',
};

export const PLAYER_COLORS: Record<number, string> = {
  1: '#e74c3c', // red
  2: '#2ecc71', // green
  3: '#3498db', // blue
  4: '#f1c40f'  // yellow
};

const SETTLEMENT_SIZE = 20;

const GameBoard: React.FC<GameBoardProps> = ({ 
  gameId, 
  accountId, 
  isSetupPhase, 
  isCurrentTurn,
  onPlacementComplete 
}) => {
  const [vertices, setVertices] = useState<Vertex[]>([]);
  const [edges, setEdges] = useState<Edge[]>([]);
  const [selectedVertex, setSelectedVertex] = useState<number | null>(null);
  const [placementMode, setPlacementMode] = useState<'settlement' | 'road' | null>(null);
  const [showValidPlacements, setShowValidPlacements] = useState(true);

  // calculate vertices and edges on mount
  useEffect(() => {
    const calculatedVertices: Vertex[] = [];
    TILES.forEach(tile => {
      const centerX = tile.x * HEX_WIDTH + HEX_WIDTH * 2;
      const centerY = tile.y * HEX_HEIGHT * 0.75 + HEX_HEIGHT;
      
      for (let i = 0; i < 6; i++) {
        const angle = (60 * i - 30) * Math.PI / 180;
        const x = centerX + TILE_SIZE * Math.cos(angle);
        const y = centerY + TILE_SIZE * Math.sin(angle);
        
        if (!calculatedVertices.some(v => 
          Math.abs(v.x - x) < 5 && Math.abs(v.y - y) < 5
        )) {
          calculatedVertices.push({
            id: calculatedVertices.length + 1,
            x,
            y
          });
        }
      }
    });
    setVertices(calculatedVertices);

    const calculatedEdges: Edge[] = [];
    calculatedVertices.forEach(v1 => {
      calculatedVertices.forEach(v2 => {
        const distance = Math.sqrt(
          Math.pow(v1.x - v2.x, 2) + Math.pow(v1.y - v2.y, 2)
        );
        if (distance > 0 && distance <= TILE_SIZE * 1.2) {
          const edgeId = `${Math.min(v1.id, v2.id)}-${Math.max(v1.id, v2.id)}`;
          if (!calculatedEdges.some(e => e.id === edgeId)) {
            calculatedEdges.push({
              id: edgeId,
              v1: v1.id,
              v2: v2.id
            });
          }
        }
      });
    });
    setEdges(calculatedEdges);
  }, []);

  const handleVertexClick = async (vertex: Vertex) => {
    if (!isSetupPhase || !isCurrentTurn || vertex.settlement || placementMode === 'road' || !showValidPlacements) return;

    try {
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/setup-action`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          accountId,
          action: 'SETTLEMENT',
          v1: vertex.id,
          v2: 0
        })
      });

      if (response.ok) {
        setVertices(prev => prev.map(v => 
          v.id === vertex.id 
            ? { ...v, settlement: { playerId: accountId } }
            : v
        ));
        setSelectedVertex(vertex.id);
        setPlacementMode('road');
        setShowValidPlacements(true); // reset for road placement
      }
    } catch (error) {
      console.error('Error placing settlement:', error);
    }
  };

  const handleEdgeClick = async (edge: Edge) => {
    if (!isSetupPhase || !isCurrentTurn || edge.road || placementMode !== 'road' || !showValidPlacements) return;
    
    if (!selectedVertex || (edge.v1 !== selectedVertex && edge.v2 !== selectedVertex)) return;

    try {
      const response = await fetch(`http://localhost:8080/api/games/${gameId}/setup-action`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          accountId,
          action: 'ROAD',
          v1: edge.v1,
          v2: edge.v2
        })
      });

      if (response.ok) {
        setEdges(prev => prev.map(e => 
          e.id === edge.id 
            ? { ...e, road: { playerId: accountId } }
            : e
        ));
        setSelectedVertex(null);
        setPlacementMode(null);
        setShowValidPlacements(false);
        onPlacementComplete(); // notify parent that turn is complete
      }
    } catch (error) {
      console.error('Error placing road:', error);
    }
  };

  const getHexPoints = (x: number, y: number): string => {
    const centerX = x * HEX_WIDTH + HEX_WIDTH * 2;
    const centerY = y * HEX_HEIGHT * 0.75 + HEX_HEIGHT;
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
    const centerX = port.x * HEX_WIDTH + HEX_WIDTH * 2;
    const centerY = port.y * HEX_HEIGHT * 0.75 + HEX_HEIGHT;
    
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
    
    const centerX = tile.x * HEX_WIDTH + HEX_WIDTH * 2;
    const centerY = tile.y * HEX_HEIGHT * 0.75 + HEX_HEIGHT;
    
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
    const color = vertex.settlement 
      ? PLAYER_COLORS[vertex.settlement.playerId] 
      : isValid 
        ? 'white'
        : '#2c3e50';

    // house shape path - centered at vertex position
    const housePoints = [
      // roof
      `M ${vertex.x - SETTLEMENT_SIZE/2} ${vertex.y}`,
      `L ${vertex.x} ${vertex.y - SETTLEMENT_SIZE/2}`,
      `L ${vertex.x + SETTLEMENT_SIZE/2} ${vertex.y}`,
      // walls
      `L ${vertex.x + SETTLEMENT_SIZE/2} ${vertex.y + SETTLEMENT_SIZE/2}`,
      `L ${vertex.x - SETTLEMENT_SIZE/2} ${vertex.y + SETTLEMENT_SIZE/2}`,
      'Z' // close the path
    ].join(' ');

    return (
      <g key={vertex.id} 
         className={isValid ? 'cursor-pointer hover:opacity-80' : ''}
         onClick={() => handleVertexClick(vertex)}>
        <path
          d={housePoints}
          fill={color}
          stroke="#2c3e50"
          strokeWidth="2"
        />
      </g>
    );
  };

  const renderRoad = (edge: Edge, v1: Vertex, v2: Vertex, isValid: boolean) => {
    const color = edge.road 
      ? PLAYER_COLORS[edge.road.playerId]
      : isValid 
        ? '#ffd700' // highlight color for valid placement
        : '#2c3e50';

    // calculate the angle of the road
    const angle = Math.atan2(v2.y - v1.y, v2.x - v1.x);
    const length = Math.sqrt(Math.pow(v2.x - v1.x, 2) + Math.pow(v2.y - v1.y, 2));
    const width = 8;

    // calculate the corners of the rectangle
    const dx = width * Math.sin(angle) / 2;
    const dy = -width * Math.cos(angle) / 2;

    const points = [
      `${v1.x + dx},${v1.y + dy}`,
      `${v2.x + dx},${v2.y + dy}`,
      `${v2.x - dx},${v2.y - dy}`,
      `${v1.x - dx},${v1.y - dy}`
    ].join(' ');

    return (
      <g key={edge.id} 
         className={isValid ? 'cursor-pointer hover:opacity-80' : ''}
         onClick={() => handleEdgeClick(edge)}>
        <polygon
          points={points}
          fill={color}
          stroke="#2c3e50"
          strokeWidth="1"
        />
      </g>
    );
  };

  return (
    <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-blue-100/50 to-blue-200/50">
      <svg
        viewBox="0 0 800 700"
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
                  width={HEX_WIDTH * scale}
                  height={HEX_HEIGHT * scale}
                  x={-HEX_WIDTH * (scale - 1) / 2}
                  y={-HEX_HEIGHT * (scale - 1) / 2}
                  preserveAspectRatio="xMidYMid slice"
                />
              </pattern>
            );
          })}
        </defs>

        {PORTS.map(port => renderPort(port))}
        
        {TILES.map(tile => (
          <g key={tile.id}>
            <polygon
              points={getHexPoints(tile.x, tile.y)}
              fill={`url(#resource-${tile.type})`}
              stroke="#2c3e50"
              strokeWidth="2"
            />
            
            {tile.number && (
              <g>
                <circle
                  cx={tile.x * HEX_WIDTH + HEX_WIDTH * 2}
                  cy={tile.y * HEX_HEIGHT * 0.75 + HEX_HEIGHT}
                  r={TILE_SIZE / 3}
                  fill="#fff"
                  stroke="#2c3e50"
                  strokeWidth="2"
                />
                <text
                  x={tile.x * HEX_WIDTH + HEX_WIDTH * 2}
                  y={tile.y * HEX_HEIGHT * 0.75 + HEX_HEIGHT}
                  textAnchor="middle"
                  dominantBaseline="middle"
                  fill="#2c3e50"
                  fontSize={tile.number === 6 || tile.number === 8 ? "20" : "16"}
                  fontWeight="bold"
                >
                  {tile.number}
                </text>
              </g>
            )}
            
            {renderRobber(tile)}
          </g>
        ))}

        {/* draw turn indicator */}
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
              fill={PLAYER_COLORS[accountId]}
              fontSize="20"
              fontWeight="bold"
              className="drop-shadow-sm"
            >
              {placementMode === 'road' ? 'Place a Road' : 'Place a Settlement'}
            </text>
          </g>
        )}

        {/* draw roads */}
        {edges.map(edge => {
          const v1 = vertices.find(v => v.id === edge.v1);
          const v2 = vertices.find(v => v.id === edge.v2);
          if (!v1 || !v2) return null;

          const isValidRoadPlacement = isSetupPhase && 
            isCurrentTurn && 
            placementMode === 'road' &&
            !edge.road &&
            showValidPlacements &&
            (edge.v1 === selectedVertex || edge.v2 === selectedVertex);

          return renderRoad(edge, v1, v2, isValidRoadPlacement);
        })}

        {/* draw settlements */}
        {vertices.map(vertex => {
          const isValidSettlementPlacement = isSetupPhase && 
            isCurrentTurn && 
            placementMode !== 'road' &&
            !vertex.settlement &&
            showValidPlacements;

          return renderSettlement(vertex, isValidSettlementPlacement);
        })}
      </svg>
    </div>
  );
};

export default GameBoard; 