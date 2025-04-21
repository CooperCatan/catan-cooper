import React from 'react';

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

const TILE_SIZE = 60;
const HEX_HEIGHT = TILE_SIZE * 2;
const HEX_WIDTH = Math.sqrt(3) * TILE_SIZE;
const ROBBER_SIZE = TILE_SIZE * 0.8;

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

const GameBoard: React.FC = () => {
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

  return (
    <div className="w-full h-full flex items-center justify-center">
      <svg
        viewBox="0 0 800 700"
        className="w-full h-full max-w-3xl"
        style={{ backgroundColor: '#b2ebf2' }}
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
      </svg>
    </div>
  );
};

export default GameBoard; 