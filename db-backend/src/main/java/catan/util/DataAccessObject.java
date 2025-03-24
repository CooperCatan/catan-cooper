package catan.util;

import java.sql.*;

public abstract class DataAccessObject<T extends DataTransferObject> {

    protected final Connection connection;
    protected final static String LAST_VAL = "SELECT last_value FROM ";
    protected final static String ACCOUNT_SEQUENCE = "account_id_seq";

    public DataAccessObject(Connection connection){
        super();
        this.connection = connection;
    }

    public abstract T findById(long id);
    public abstract T create(T dto);
    public abstract boolean delete(long id);
    
    protected int getLastVal(String sequence){
        int key = 0;
        String sql = LAST_VAL + sequence;
        try(Statement statement = connection.createStatement()){
            ResultSet rs = statement.executeQuery(sql);
            while(rs.next()){
                key=rs.getInt(1);
            }
            return key;
        }catch (SQLException e ){
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}