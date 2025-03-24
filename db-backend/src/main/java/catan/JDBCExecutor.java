package catan;

import java.sql.Connection;
import java.sql.SQLException;

public class JDBCExecutor {

    public static void main(String... args) {
        System.out.println("Hello Learning JDBC");
        DatabaseConnectionManager dcm = new DatabaseConnectionManager("localhost",
                "rps", "postgres", "password");

        try {
            Connection connection = dcm.getConnection();
            PlayerDAO playerDAO = new PlayerDAO(connection);

            Player player = playerDAO.findById(1);
            System.out.println(player.toString());

            /*
            Player player2 = new Player();
            player2.setUserName("Surinderpal");
            player2.setPassword("1");
            player2.setTotalGames(3);
            player2.setTotalWins(3);

            player2 = playerDAO.create(player2);
            System.out.println(player2.toString());
            */
            player.setTotalGames(500);
            player.setTotalWins(600);
            player.setTotalLosses(-100);
            player = playerDAO.update(player);
            System.out.println(player.toString());

        }
        catch(SQLException e) {
            e.printStackTrace();
        }
    }
}