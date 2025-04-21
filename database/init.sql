-- for repeated use, we can just run this file in the beginning

DROP TABLE IF EXISTS game_action;
DROP TABLE IF EXISTS trade;
DROP TABLE IF EXISTS player_state;
DROP TABLE IF EXISTS bank_cards_remaining;
DROP TABLE IF EXISTS game_state;
DROP TABLE IF EXISTS account;

DROP SEQUENCE IF EXISTS account_id_seq;
DROP SEQUENCE IF EXISTS game_id_seq;
DROP SEQUENCE IF EXISTS turn_number_seq;
CREATE SEQUENCE account_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE game_id_seq START WITH 1 INCREMENT BY 1;
CREATE SEQUENCE turn_number_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE account (
    account_id bigint DEFAULT nextval('account_id_seq'),
    username varchar(255),
    password varchar(255),
    total_wins bigint DEFAULT 0,
    total_losses bigint DEFAULT 0,
    total_games bigint DEFAULT 0 CHECK (
        total_games >= 0
        AND total_losses >= 0
        AND total_games = total_losses + total_wins
    ),
    elo bigint DEFAULT 0,
    PRIMARY KEY (account_id)
);

CREATE TABLE game_state (
    game_id bigint DEFAULT nextval('game_id_seq'),
    turn_number bigint DEFAULT nextval('turn_number_seq'),
    board_state JSONB,
    winner_id bigint,
    is_game_over boolean DEFAULT FALSE,
    bank_brick bigint DEFAULT 19, --hardcoded values here and below from catan rulebook
    bank_ore bigint DEFAULT 19,
    bank_sheep bigint DEFAULT 19,
    bank_wheat bigint DEFAULT 19,
    bank_wood bigint DEFAULT 19,
    bank_year_of_plenty bigint DEFAULT 2,
    bank_monopoly bigint DEFAULT 2,
    bank_road_building bigint DEFAULT 2,
    bank_victory_point bigint DEFAULT 5,
    bank_knight bigint DEFAULT 14,
    PRIMARY KEY (game_id, turn_number),
    FOREIGN KEY (winner_id) REFERENCES account(account_id)
);


CREATE TABLE player_state (
    account_id bigint,
    game_id bigint,
    turn_number bigint,
    hand_ore bigint DEFAULT 0,
    hand_sheep bigint DEFAULT 0,
    hand_wheat bigint DEFAULT 0,
    hand_wood bigint DEFAULT 0,
    hand_brick bigint DEFAULT 0,
    hand_victory_point bigint DEFAULT 0,
    hand_knight bigint DEFAULT 0,
    hand_monopoly bigint DEFAULT 0,
    hand_year_of_plenty bigint DEFAULT 0,
    hand_road_building bigint DEFAULT 0,
    num_settlements bigint DEFAULT 0,
    num_roads bigint DEFAULT 0,
    num_cities bigint DEFAULT 0,
    num_longest_continuous_road bigint DEFAULT 0,
    largest_army boolean DEFAULT FALSE,
    longest_road boolean DEFAULT FALSE,
    PRIMARY KEY (account_id, game_id, turn_number),
    FOREIGN KEY (account_id) REFERENCES account(account_id),
    FOREIGN KEY (game_id, turn_number) REFERENCES game_state(game_id, turn_number)
);

CREATE TABLE trade (
    trade_id bigint,
    game_id bigint,
    turn_number bigint,
    from_player_id bigint,
    to_player_id bigint,
    given_resource varchar(255),
    given_amount bigint,
    received_resource varchar(255),
    received_amount bigint,
    is_accepted boolean DEFAULT FALSE,
    PRIMARY KEY (trade_id),
    FOREIGN KEY (game_id, turn_number) REFERENCES game_state(game_id, turn_number),
    FOREIGN KEY (from_player_id) REFERENCES account(account_id),
    FOREIGN KEY (to_player_id) REFERENCES account(account_id)
);

CREATE TABLE game_action (
    game_id bigint,
    turn_number bigint,
    action_type varchar(255),
    PRIMARY KEY (game_id, turn_number),
    FOREIGN KEY (game_id, turn_number) REFERENCES game_state(game_id, turn_number)
);
