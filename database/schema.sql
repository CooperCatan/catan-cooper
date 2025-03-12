-- Drop existing tables and sequences in reverse dependency order
DROP TABLE IF EXISTS game_action;
DROP TABLE IF EXISTS trade;
DROP TABLE IF EXISTS player_state;
DROP TABLE IF EXISTS bank_cards_remaining;
DROP TABLE IF EXISTS game_state;
DROP TABLE IF EXISTS account;
DROP SEQUENCE IF EXISTS account_id_seq;
DROP SEQUENCE IF EXISTS game_id_seq;
DROP SEQUENCE IF EXISTS turn_number_seq;

-- Create sequences for IDs and turn numbers
CREATE SEQUENCE account_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE game_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE turn_number_seq START WITH 1 INCREMENT BY 1;

-- Account: User profiles with stats (Task: User Authentication - 2 weeks)
CREATE TABLE account (
    account_id BIGINT DEFAULT nextval('account_id_seq') PRIMARY KEY,
    username VARCHAR(255) UNIQUE NOT NULL, -- Unique identifier for login
    password VARCHAR(255) NOT NULL,        -- Hashed password
    total_wins BIGINT DEFAULT 0,           -- Tracks wins
    total_losses BIGINT DEFAULT 0,         -- Tracks losses
    total_games BIGINT DEFAULT 0 CHECK (total_games >= 0 AND total_losses >= 0 AND total_games = total_wins + total_losses),
    elo BIGINT DEFAULT 0                   -- Elo rating for competitive play
);

-- Game_State: Tracks game instances with turn-based versioning (Task: Game State Management - 3 weeks)
CREATE TABLE game_state (
    game_id BIGINT DEFAULT nextval('game_id_seq'),
    turn_number BIGINT DEFAULT nextval('turn_number_seq'), -- Snapshots per turn
    winner_id BIGINT REFERENCES account(account_id),       -- Links to winner (Task: Victory Condition Logic)
    robber_location JSONB,                                 -- e.g., {"x": 1, "y": 2} for hex position
    is_game_over BOOLEAN DEFAULT FALSE,                    -- Tracks game end
    board_state JSONB NOT NULL,                            -- Hex tiles and ports
    PRIMARY KEY (game_id, turn_number)
);

-- Bank_Cards_Remaining: Tracks available resources and cards (Task: Game State Management - 3 weeks)
CREATE TABLE bank_cards_remaining (
    game_id BIGINT,
    turn_number BIGINT,
    brick BIGINT DEFAULT 19,           -- Explicit resource counts
    ore BIGINT DEFAULT 19,
    sheep BIGINT DEFAULT 19,
    wheat BIGINT DEFAULT 19,
    wood BIGINT DEFAULT 19,
    year_of_plenty BIGINT DEFAULT 2,
    monopoly BIGINT DEFAULT 2,
    road_building BIGINT DEFAULT 2,
    victory_point BIGINT DEFAULT 5,
    knight BIGINT DEFAULT 14,
    PRIMARY KEY (game_id, turn_number),
    FOREIGN KEY (game_id, turn_number) REFERENCES game_state(game_id, turn_number)
);

-- Player_State: Tracks player stats per turn (Task: Game State Management - 3 weeks)
CREATE TABLE player_state (
    account_id BIGINT REFERENCES account(account_id),
    game_id BIGINT,
    turn_number BIGINT,
    brick BIGINT DEFAULT 0,            -- Explicit resources
    ore BIGINT DEFAULT 0,
    sheep BIGINT DEFAULT 0,
    wheat BIGINT DEFAULT 0,
    wood BIGINT DEFAULT 0,
    victory_point BIGINT DEFAULT 0,    -- Development card counts
    knight BIGINT DEFAULT 0,
    monopoly BIGINT DEFAULT 0,
    year_of_plenty BIGINT DEFAULT 0,
    road_building BIGINT DEFAULT 0,
    num_settlements BIGINT DEFAULT 0,  -- Tracks structures
    num_roads BIGINT DEFAULT 0,
    num_cities BIGINT DEFAULT 0,
    num_longest_continuous_road BIGINT DEFAULT 0, -- For longest road bonus
    largest_army BOOLEAN DEFAULT FALSE,           -- Bonus points (2 VP)
    longest_road BOOLEAN DEFAULT FALSE,           -- Bonus points (2 VP)
    PRIMARY KEY (account_id, game_id, turn_number),
    FOREIGN KEY (game_id, turn_number) REFERENCES game_state(game_id, turn_number)
);

-- Trade: Records trades between players or with bank (Task: Trading System - 3 weeks)
CREATE TABLE trade (
    trade_id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    game_id BIGINT,
    turn_number BIGINT,
    from_player_id BIGINT REFERENCES account(account_id),
    to_player_id BIGINT REFERENCES account(account_id), -- NULL for bank trades
    given_resource VARCHAR(255),                        -- Single resource trade
    given_amount BIGINT,
    received_resource VARCHAR(255),
    received_amount BIGINT,
    is_accepted BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (game_id, turn_number) REFERENCES game_state(game_id, turn_number)
);

-- Game_Action: Logs actions per turn (Task: Turn-Based System - 3 weeks)
CREATE TABLE game_action (
    game_id BIGINT,
    turn_number BIGINT,
    action_type VARCHAR(255), -- e.g., "roll_dice", "build_settlement"
    PRIMARY KEY (game_id, turn_number),
    FOREIGN KEY (game_id, turn_number) REFERENCES game_state(game_id, turn_number)
);

-- Indexes for performance (Addresses Backend Risk: Concurrency - Page 2)
CREATE INDEX idx_player_state_game_id ON player_state(game_id, turn_number);
CREATE INDEX idx_trade_game_id ON trade(game_id, turn_number);
