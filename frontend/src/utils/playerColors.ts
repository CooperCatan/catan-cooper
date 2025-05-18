export const PLAYER_COLORS: Record<number, string> = {
  1: '#FF6B6B', // red (Player 1)
  2: '#4ECDC4', // blue/teal (Player 2)
  3: '#FFA07A', // orange (Player 3)
  4: '#98D8AA', // green (Player 4)
};

export const DEFAULT_PLAYER_COLOR = '#E0E0E0'; // fallback color

export const getPlayerColor = (playerId?: number | null): string => {
  if (playerId && PLAYER_COLORS[playerId]) {
    return PLAYER_COLORS[playerId];
  }
  return DEFAULT_PLAYER_COLOR;
}; 