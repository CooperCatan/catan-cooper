import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { getAuth } from 'firebase/auth';
import { Separator } from '@radix-ui/react-separator';
import { XCircle } from 'lucide-react';
import GameBoard from './GameBoard';
import { PLAYER_COLORS } from './GameBoard';
import { cn } from '../../utils/cn';

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
  color?: string;
}

const GameRoom: React.FC = () => {
  const { gameId } = useParams();
  const navigate = useNavigate();
  const auth = getAuth();
  const [countdown, setCountdown] = useState(60);
  const [players, setPlayers] = useState<Player[]>([]);
  const [isSetupPhase, setIsSetupPhase] = useState(true);
  const [currentTurn, setCurrentTurn] = useState<number | null>(null);

  useEffect(() => {
    const currentUser = auth.currentUser;
    if (!currentUser) {
      navigate('/signin');
      return;
    }

    // For local testing, make it your turn (Player 3) after countdown
    const timer = setInterval(() => {
      setCountdown(prev => {
        if (prev <= 1) {
          clearInterval(timer);
          setCurrentTurn(3); // Start with Player 3 (current user)
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    const simulatedPlayers: Player[] = [
      {
        id: 1,
        username: "Player1",
        elo: 1200,
        totalGames: 15,
        totalWins: 8,
        devCards: 0,
        knightsPlayed: 0,
        roadsPlaced: 0,
        color: PLAYER_COLORS[1]
      },
      {
        id: 2,
        username: "Player2",
        elo: 1150,
        totalGames: 12,
        totalWins: 5,
        devCards: 0,
        knightsPlayed: 0,
        roadsPlaced: 0,
        color: PLAYER_COLORS[2]
      },
      {
        id: 3,
        username: currentUser.displayName || currentUser.email?.split('@')[0] || "Anonymous",
        elo: 1000,
        totalGames: 0,
        totalWins: 0,
        isCurrentUser: true,
        devCards: 0,
        knightsPlayed: 0,
        roadsPlaced: 0,
        color: PLAYER_COLORS[3],
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

    return () => clearInterval(timer);
  }, [auth, navigate]);

  const handleTurnComplete = () => {
    // Rotate to next player's turn
    setCurrentTurn(prev => {
      if (prev === 3) return 1; // After your turn (3), go to Player 1
      if (prev === 1) return 2;
      if (prev === 2) return 3;
      return 3; // Default to your turn if something goes wrong
    });
  };

  const PlayerCard: React.FC<{ player: Player }> = ({ player }) => (
    <div 
      className={cn(
        "relative rounded-xl p-4 transition-all duration-200",
        "bg-white/20 backdrop-blur-sm",
        "border-[1.5px] shadow-lg hover:shadow-xl",
        currentTurn === player.id && "ring-2 ring-offset-2 ring-offset-blue-100/50"
      )}
      style={{
        borderColor: player.color
      }}
    >
      {currentTurn === player.id && (
        <div className="absolute -top-2 -right-2 px-2 py-0.5 bg-green-500/90 backdrop-blur-sm text-white text-xs font-medium rounded-full shadow-sm">
          Current Turn
        </div>
      )}
      <div className="flex items-center justify-between mb-2">
        <div className="flex items-center gap-2">
          <div 
            className={cn(
              "w-4 h-4 rounded-full border-2",
              "transition-all duration-200"
            )}
            style={{ 
              backgroundColor: player.isCurrentUser ? 'white' : player.color,
              borderColor: player.color
            }}
          />
          <h3 className="text-base font-semibold text-gray-800">
            {player.username}
          </h3>
        </div>
        <div className="flex items-center gap-1">
          <span className="text-sm font-medium text-gray-500">ELO</span>
          <span className="text-sm font-bold text-gray-700">{player.elo}</span>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-2 text-xs text-gray-600 mb-2">
        <div className="flex flex-col items-center p-1 bg-gray-50 rounded">
          <span className="font-medium">Games</span>
          <span>{player.totalGames}</span>
        </div>
        <div className="flex flex-col items-center p-1 bg-gray-50 rounded">
          <span className="font-medium">Wins</span>
          <span>{player.totalWins}</span>
        </div>
        <div className="flex flex-col items-center p-1 bg-gray-50 rounded">
          <span className="font-medium">Rate</span>
          <span>{((player.totalWins / (player.totalGames || 1)) * 100).toFixed(1)}%</span>
        </div>
      </div>

      <div className="grid grid-cols-3 gap-1 text-xs text-gray-600 mb-2">
        <div className="flex items-center gap-1">
          <span>🎲</span>
          <span>{player.devCards}</span>
        </div>
        <div className="flex items-center gap-1">
          <span>⚔️</span>
          <span>{player.knightsPlayed}</span>
        </div>
        <div className="flex items-center gap-1">
          <span>🛣️</span>
          <span>{player.roadsPlaced}</span>
        </div>
      </div>

      {player.isCurrentUser && player.resources && (
        <>
          <Separator className="my-2 bg-gray-200" />
          <div className="space-y-1">
            <div className="font-medium text-xs text-gray-700">Resources</div>
            <div className="grid grid-cols-2 gap-2 text-xs">
              <div className="flex items-center gap-1 text-gray-700">
                <span>🧱</span>
                <span>{player.resources.brick}</span>
              </div>
              <div className="flex items-center gap-1 text-gray-700">
                <span>🌲</span>
                <span>{player.resources.wood}</span>
              </div>
              <div className="flex items-center gap-1 text-gray-700">
                <span>⛰️</span>
                <span>{player.resources.ore}</span>
              </div>
              <div className="flex items-center gap-1 text-gray-700">
                <span>🌾</span>
                <span>{player.resources.wheat}</span>
              </div>
              <div className="flex items-center gap-1 text-gray-700">
                <span>🐑</span>
                <span>{player.resources.wool}</span>
              </div>
            </div>
          </div>
        </>
      )}
    </div>
  );

  return (
    <div className="h-screen w-screen bg-gradient-to-br from-blue-50 to-blue-100 flex overflow-hidden">
      <button
        onClick={() => navigate('/lobby')}
        className="fixed top-4 left-4 z-10 flex items-center gap-2 px-4 py-2 bg-white/20 backdrop-blur-sm text-red-600 text-sm font-medium rounded-xl hover:bg-red-50/50 transition-all shadow-lg hover:shadow-xl border border-red-200/50"
      >
        <XCircle size={16} />
        <span>Quit Game</span>
      </button>

      <div className="flex-1 relative">
        <div className="absolute inset-0">
          <div className="text-center py-4 bg-white/10 backdrop-blur-sm border-b border-white/20">
            <h2 className="text-2xl font-bold text-gray-800/90">Game #{gameId}</h2>
            <div className="text-lg font-medium text-gray-700/90 mt-1">
              {countdown > 0 
                ? `Starting in ${countdown}s`
                : isSetupPhase 
                  ? "Setup Phase"
                  : "Game in Progress"}
            </div>
          </div>
          <div className="w-full h-[calc(100%-80px)]">
            <GameBoard 
              gameId={Number(gameId)}
              accountId={Number(auth.currentUser?.uid)}
              isSetupPhase={isSetupPhase}
              isCurrentTurn={currentTurn === 3}
              onPlacementComplete={handleTurnComplete}
            />
          </div>
        </div>
      </div>

      <div className="w-96 bg-white/10 backdrop-blur-sm border-l border-white/20 p-6 overflow-y-auto">
        <div className="flex items-center justify-between mb-6">
          <h2 className="text-xl font-bold text-gray-800/90">Players</h2>
          <div className="px-3 py-1 bg-blue-100/50 backdrop-blur-sm text-blue-700/90 text-sm font-medium rounded-full border border-blue-200/50">
            {countdown > 0 ? `Starting in ${countdown}s` : 'Setup Phase'}
          </div>
        </div>
        <div className="space-y-4">
          {players.map(player => (
            <PlayerCard key={player.id} player={player} />
          ))}
        </div>
      </div>
    </div>
  );
};

export default GameRoom; 