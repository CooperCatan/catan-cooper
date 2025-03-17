package catan.util;

import java.sql.*;

public abstract class DataAccessObject<T extends DataTransferObject> {

    protected final Connection connection;
    protected final static String LAST_VAL = "SELECT last_value FROM ";
    protected final static String ACCOUNT_SEQUENCE = "account_id_seq"; // Changed from player_seq to account_id_seq

    public DataAccessObject(Connection connection) {
        super();
        this.connection = connection;
    }
<<<<<<< HEAD
=======

    public abstract T findById(long id);

>>>>>>> b3f1e1b1d3fd2f950e9c88745ac9dbe042cc8012
    public abstract T create(T dto);

    public abstract T read(long id);

    public abstract T update(T dto);

<<<<<<< HEAD
    public abstract T delete(long id);

=======
    public abstract T delete(T dto); // Added delete method

    protected int getLastVal(String sequence) {
        int key = 0;
        String sql = LAST_VAL + sequence;
        try (Statement statement = connection.createStatement()) {
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                key = rs.getInt(1);
            }
            return key;
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
>>>>>>> b3f1e1b1d3fd2f950e9c88745ac9dbe042cc8012
}