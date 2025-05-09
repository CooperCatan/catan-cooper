DROP TABLE IF EXISTS player;
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
    json_hexes text,
    json_vertices text,
    json_edges text,
    winner_id bigint,
    is_game_over boolean DEFAULT FALSE,
    bank_brick bigint DEFAULT 19,
    bank_ore bigint DEFAULT 19,
    bank_sheep bigint DEFAULT 19,
    bank_wheat bigint DEFAULT 19,
    bank_wood bigint DEFAULT 19,
    bank_year_of_plenty bigint DEFAULT 2,
    bank_monopoly bigint DEFAULT 2,
    bank_road_building bigint DEFAULT 2,
    bank_victory_point bigint DEFAULT 5,
    bank_knight bigint DEFAULT 14,
    PRIMARY KEY (game_id),
    FOREIGN KEY (winner_id) REFERENCES account (account_id)
);

CREATE TABLE player (
    account_id bigint,
    game_id bigint,
    ore bigint DEFAULT 0,
    sheep bigint DEFAULT 0,
    wheat bigint DEFAULT 0,
    wood bigint DEFAULT 0,
    brick bigint DEFAULT 0,
    victory_point bigint DEFAULT 0,
    knight bigint DEFAULT 0,
    knight_used bigint DEFAULT 0,
    monopoly bigint DEFAULT 0,
    year_of_plenty bigint DEFAULT 0,
    road_building bigint DEFAULT 0,
    num_settlements bigint DEFAULT 0,
    num_roads bigint DEFAULT 0,
    num_cities bigint DEFAULT 0,
    num_longest_continuous_road bigint DEFAULT 0,
    largest_army boolean DEFAULT FALSE,
    longest_road boolean DEFAULT FALSE,
    PRIMARY KEY (account_id),
    FOREIGN KEY (account_id) REFERENCES account (account_id),
    FOREIGN KEY (game_id) REFERENCES game (game_id)
);