import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getAuth } from 'firebase/auth';
import GameBoard from './GameBoard';

interface Player {
  id: number;
  username: string;
  elo: number;
  totalGames: number;
  totalWins: number;
  isCurrentUser?: boolean;
  // game-level information
  devCards: number;
  knightsPlayed: number;
  roadsPlaced: number;
  // info below is private and only available to individual players
  resources?: {
    brick: number;
    wood: number;
    ore: number;
    wheat: number;
    wool: number;
  };
}

const GameRoom: React.FC = () => {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const auth = getAuth();
  const [countdown, setCountdown] = useState(60);
  const [players, setPlayers] = useState<Player[]>([]);

  useEffect(() => {
    const currentUser = auth.currentUser;
    if (!currentUser) {
      navigate('/signin');
      return;
    }

    const simulatedPlayers: Player[] = [
      {
        id: 1,
        username: "Player1",
        elo: 1200,
        totalGames: 15,
        totalWins: 8,
        devCards: 0,
        knightsPlayed: 0,
        roadsPlaced: 0
      },
      {
        id: 2,
        username: "Player2",
        elo: 1150,
        totalGames: 12,
        totalWins: 5,
        devCards: 0,
        knightsPlayed: 0,
        roadsPlaced: 0
      },
      {
        id: 3,
        username: currentUser.displayName || "You",
        elo: 1000,
        totalGames: 0,
        totalWins: 0,
        isCurrentUser: true,
        devCards: 0,
        knightsPlayed: 0,
        roadsPlaced: 0,
        resources: {
          brick: 0,
          wood: 0,
          ore: 0,
          wheat: 0,
          wool: 0
        }
      }
    ];

    setPlayers(simulatedPlayers);

    // countdown
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          // here you would typically start the game when countdown over, can extend
          // depending on num of players to place initial buildings, functionality not implemented yet 
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [auth, navigate]);

  const PlayerCard: React.FC<{ player: Player }> = ({ player }) => (
    <div 
      className={`
        ${player.isCurrentUser 
          ? 'bg-catan-brick/10 border-catan-brick' 
          : 'bg-white border-gray-200'
        }
        rounded-lg shadow-sm p-3 mb-2 border transition-all duration-200
      `}
    >
      <div className="flex justify-between items-center mb-1">
        <h3 className="text-base font-semibold text-gray-800">{player.username}</h3>
        <span className="text-sm font-medium text-gray-600">ELO: {player.elo}</span>
      </div>
      <div className="text-sm text-gray-600">
        <div className="flex justify-between text-xs mb-1">
          <span>Games: {player.totalGames}</span>
          <span>Wins: {player.totalWins}</span>
          <span>{((player.totalWins / player.totalGames) * 100).toFixed(1)}%</span>
        </div>
        <div className="border-t pt-1 mt-1 text-xs grid grid-cols-3 gap-1">
          <div>Dev Cards: {player.devCards}</div>
          <div>Knights: {player.knightsPlayed}</div>
          <div>Roads: {player.roadsPlaced}</div>
        </div>
        {player.isCurrentUser && player.resources && (
          <div className="border-t pt-2 mt-2">
            <div className="font-medium mb-1">Your Resources:</div>
            <div className="grid grid-cols-2 gap-2">
              <div>🧱 Brick: {player.resources.brick}</div>
              <div>🌲 Wood: {player.resources.wood}</div>
              <div>⛰️ Ore: {player.resources.ore}</div>
              <div>🌾 Wheat: {player.resources.wheat}</div>
              <div>🐑 Sheep: {player.resources.wool}</div>
            </div>
          </div>
        )}
      </div>
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-100 flex">
      {/* quit */}
      <button
        onClick={() => navigate('/lobby')}
        className="absolute top-4 left-4 px-4 py-2 bg-red-500 text-white text-sm font-medium rounded-lg hover:bg-red-600 transition-colors shadow-md flex items-center space-x-1"
      >
        <span>✕</span>
        <span>Quit</span>
      </button>

      {/* game board section LHS */}
      <div className="flex-1 p-4">
        <div className="bg-white rounded-xl shadow-lg p-4 h-full">
          <div className="text-center mb-2">
            <h2 className="text-xl font-bold text-gray-800">Game #{gameId}</h2>
            <div className="text-lg font-medium text-catan-brick">
              Game Starting in: {countdown}s
            </div>
          </div>
          <div className="w-full h-[calc(100%-80px)] bg-blue-50 rounded-lg flex items-center justify-center">
            <GameBoard />
          </div>
        </div>
      </div>

      {/* all players section RHS */}
      <div className="w-72 bg-white shadow-lg p-3 overflow-y-auto">
        <h2 className="text-lg font-bold text-gray-800 mb-3">Players</h2>
        <div className="space-y-2">
          {/* regular players */}
          {players.filter(p => !p.isCurrentUser).map(player => (
            <PlayerCard key={player.id} player={player} />
          ))}
          
          {/* self */}
          <div className="mt-4 pt-2 border-t border-gray-200">
            <h3 className="text-xs font-medium text-gray-500 mb-1">You</h3>
            {players.filter(p => p.isCurrentUser).map(player => (
              <PlayerCard key={player.id} player={player} />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default GameRoom; 