INSERT INTO account (username, password, total_wins, total_losses, total_games, elo)
VALUES 
('Surinderpal', 'sheepnotsyphilis', 5, 3, 8, 1200),
('Mohammed', 'iloveports123', 3, 4, 7, 1150),
('IsaacS', 'bestatcatan', 4, 2, 6, 1250),
('IsaacM', 'stonenotore', 2, 5, 7, 1100);

INSERT INTO game_state (game_id, turn_number, winner_id, robber_location, is_game_over, bank_brick, bank_ore, bank_sheep, bank_wheat, bank_wood, bank_year_of_plenty, bank_monopoly, bank_road_building, bank_victory_point, bank_knight)
VALUES
(1, 1, NULL, '{"hex": "desert"}', FALSE, 19, 19, 19, 19, 19, 2, 2, 1, 5, 14),
(2, 1, 1, '{"hex": "desert"}', TRUE, 19, 19, 19, 19, 19, 1, 2, 2, 5, 14);

INSERT INTO player_state (account_id, game_id, turn_number, hand_ore, hand_sheep, hand_wheat, hand_wood, hand_brick, hand_victory_point, hand_knight, num_settlements, num_roads)
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
