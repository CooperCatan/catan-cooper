INSERT INTO account (username, password, total_wins, total_losses, total_games, elo)
VALUES 
('Surinderpal', 'sheepnotsyphilis', 5, 3, 8, 1200),
('Mohammed', 'iloveports123', 3, 4, 7, 1150),
('IsaacS', 'bestatcatan', 4, 2, 6, 1250),
('IsaacM', 'stonenotore', 2, 5, 7, 1100);

INSERT INTO game_state (game_id, turn_number, winner_id, robber_location, is_game_over)
VALUES
(1, 1, NULL, 1, FALSE),
(2, 1, 1, 2, TRUE);

INSERT INTO bank_cards_remaining (game_id, turn_number, brick, ore, sheep, wheat, wood, year_of_plenty, monopoly, road_building, victory_point, knight)
VALUES
(1, 1, 19, 19, 19, 19, 19, 2, 2, 1, 5, 14),
(2, 1, 19, 19, 19, 19, 19, 1, 2, 2, 5, 14);

INSERT INTO player_state (account_id, game_id, turn_number, ore, sheep, wheat, wood, brick, victory_point, knight, num_settlements, num_roads)
VALUES
(1, 1, 1, 2, 1, 3, 2, 1, 0, 1, 2, 3),
(2, 1, 1, 1, 2, 1, 3, 2, 0, 0, 1, 2),
(3, 1, 1, 3, 1, 2, 1, 1, 0, 1, 2, 2),
(4, 1, 1, 1, 3, 1, 2, 2, 0, 0, 1, 3);

INSERT INTO trade (trade_id, game_id, turn_number, from_player_id, to_player_id, given_resource, given_amount, received_resource, received_amount, is_accepted)
VALUES
(1, 1, 1, 1, 2, 'wood', 2, 'brick', 1, TRUE),
(2, 1, 1, 3, 4, 'ore', 1, 'sheep', 2, FALSE);

INSERT INTO game_action (game_id, turn_number, action_type)
VALUES
(1, 1, 'ROLL_DICE'),
(2, 1, 'BUILD_SETTLEMENT'); 