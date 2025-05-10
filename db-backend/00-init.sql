DROP TABLE IF EXISTS game;
DROP TABLE IF EXISTS account;

DROP SEQUENCE IF EXISTS account_id_seq;
DROP SEQUENCE IF EXISTS game_id_seq;
CREATE SEQUENCE account_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE game_id_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE account (
    account_id bigint DEFAULT nextval('account_id_seq'),
    username varchar(255) UNIQUE NOT NULL,
    email varchar(255) UNIQUE NOT NULL,
    total_wins bigint DEFAULT 0,
    total_losses bigint DEFAULT 0,
    total_games bigint DEFAULT 0 CHECK (
        total_games >= 0
        AND total_losses >= 0
        AND total_games = total_losses + total_wins
    ),
    elo bigint DEFAULT 1000,
    PRIMARY KEY (account_id)
);

CREATE TABLE game (
    game_id bigint DEFAULT nextval('game_id_seq'),
    player_list bigint[] NOT NULL,  -- array of account_ids of players in the game
    winner_id bigint,
    is_game_over boolean DEFAULT FALSE,
    in_progress boolean DEFAULT FALSE,
    created_at timestamp DEFAULT CURRENT_TIMESTAMP,
    game_name varchar(255) NOT NULL,
    json_hexes text,
    json_vertices text,
    json_edges text,
    json_players text,
    current_dice_roll integer,
    robber_location integer,
    bank_brick integer DEFAULT 19,
    bank_ore integer DEFAULT 19,
    bank_sheep integer DEFAULT 19,
    bank_wheat integer DEFAULT 19,
    bank_wood integer DEFAULT 19,
    bank_year_of_plenty integer DEFAULT 2,
    bank_monopoly integer DEFAULT 2,
    bank_road_building integer DEFAULT 2,
    bank_victory_point integer DEFAULT 5,
    bank_knight integer DEFAULT 14,
    PRIMARY KEY (game_id),
    FOREIGN KEY (winner_id) REFERENCES account (account_id)
);