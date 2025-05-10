-- changed s.t. we are not storing passwords as plaintext anymore, storing emails for firebase auth
-- massively simplified the sql backend, we do not need to handle the actual game state here, we can just
-- handle it in the backend application logic since it is transient data anyway. only interested in displaying
-- the players in the game, the game itself, the winner, and if it is completed. 

INSERT INTO account (username, email, total_wins, total_losses, total_games, elo)
VALUES 
('surinsingh', 'surinderpal@cooper.edu', 5, 3, 8, 1200),
('mohamed', 'mohamed@cooper.edu', 3, 4, 7, 1150),
('isaacm', 'isaacm@cooper.edu', 2, 5, 7, 1100);

-- game 1: in progress, not waiting for more players
INSERT INTO game (
    game_id, player_list, winner_id, is_game_over, in_progress, game_name,
    json_hexes, json_vertices, json_edges, json_players,
    current_dice_roll, robber_location,
    bank_brick, bank_ore, bank_sheep, bank_wheat, bank_wood,
    bank_year_of_plenty, bank_monopoly, bank_road_building, bank_victory_point, bank_knight
)
VALUES (
    1, ARRAY[1, 2], NULL, FALSE, TRUE, 'Casual Game',
    NULL, NULL, NULL, NULL,
    NULL, NULL,
    19, 19, 19, 19, 19,
    2, 2, 2, 5, 14
);

-- game 2: completed game with a winner 
INSERT INTO game (
    game_id, player_list, winner_id, is_game_over, in_progress, game_name,
    json_hexes, json_vertices, json_edges, json_players,
    current_dice_roll, robber_location,
    bank_brick, bank_ore, bank_sheep, bank_wheat, bank_wood,
    bank_year_of_plenty, bank_monopoly, bank_road_building, bank_victory_point, bank_knight
)
VALUES (
    2, ARRAY[1, 2, 3], 1, TRUE, FALSE, 'Completed Game',
    NULL, NULL, NULL, NULL,
    NULL, NULL,
    19, 19, 19, 19, 19,
    2, 2, 2, 5, 14
);
