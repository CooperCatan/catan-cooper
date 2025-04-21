package catan;

import java.sql.Connection;
import java.sql.SQLException;

public class JDBCExecutor {

    public static void main(String... args) {
        System.out.println("Testing JDBC Connection and Account Operations");
        DatabaseConnectionManager dcm = new DatabaseConnectionManager("db",
                "catan", "postgres", "password");

        try {
            Connection connection = dcm.getConnection();
            AccountDAO accountDAO = new AccountDAO(connection);

            try {
                Account account = accountDAO.findById(1);
                System.out.println("Found account: " + account.toString());
            } catch (Exception e) {
                System.out.println("Could not find account with ID 1 (this is normal if no accounts exist yet)");
            }

            Account newAccount = new Account();
            newAccount.setUsername("TestUser");
            newAccount.setPassword("testpass123");
            newAccount.setTotalGames(0);
            newAccount.setTotalWins(0);
            newAccount.setTotalLosses(0);
            newAccount.setElo(1000);

            newAccount = accountDAO.create(newAccount);
            System.out.println("Created account: " + newAccount.toString());

            newAccount = accountDAO.updateUsername(newAccount.getId(), "UpdatedUsername");
            System.out.println("Updated username: " + newAccount.toString());

            newAccount = accountDAO.updatePassword(newAccount.getId(), "newpass456");
            System.out.println("Updated password: " + newAccount.toString());

            newAccount = accountDAO.updateElo(newAccount.getId(), 1200);
            System.out.println("Updated ELO: " + newAccount.toString());

            newAccount = accountDAO.incrementWins(newAccount.getId());
            System.out.println("After win: " + newAccount.toString());

            newAccount = accountDAO.incrementLosses(newAccount.getId());
            System.out.println("After loss: " + newAccount.toString());

            boolean deleted = accountDAO.delete(newAccount.getId());
            System.out.println("Account deleted: " + deleted);

        } catch(SQLException e) {
            System.err.println("Database connection error:");
            e.printStackTrace();
        } catch(Exception e) {
            System.err.println("Unexpected error:");
            e.printStackTrace();
        }
    }
}