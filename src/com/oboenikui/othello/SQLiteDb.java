package com.oboenikui.othello;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class SQLiteDb {
    public static final int DB_VERSION = 2;
    public static final String TABLE_DATA =         "data";
    public static final String COLUMN_PATTERN =     "pattern";
    public static final String COLUMN_WIN =         "win";
    public static final String COLUMN_LOSE =        "lose";
    public static final String COLUMN_NEXT_X =      "next_x";
    public static final String COLUMN_NEXT_Y =      "next_y";
    
    public static final String SQL_CREATE_DATA =
            "CREATE TABLE " + TABLE_DATA + "(" +
                    COLUMN_PATTERN +     " TEXT," +
                    COLUMN_WIN +        " INTEGER," +
                    COLUMN_LOSE +       " INTEGER," +
                    COLUMN_NEXT_X +     " INTEGER," +
                    COLUMN_NEXT_Y +     " INTEGER);";
    
    public static final String TABLE_VERSION =      "version";
    public static final String COLUMN_VERSION =     "version";
    public static final String SQL_CREATE_VERSION =
            "CREATE TABLE " + TABLE_VERSION + "(" +
                    COLUMN_VERSION +    " INTEGER);";
    private Connection connection;
    private static Statement statement;
    private static SQLiteDb mSQLiteDb = null;
    private static final String SQLITE_CONNECTION = "jdbc:sqlite:data.db";

    public static SQLiteDb getInstance(){
        if(mSQLiteDb==null){
            return mSQLiteDb = new SQLiteDb();
        } else {
            return mSQLiteDb;
        }
    }
    
    private SQLiteDb(){
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection(SQLITE_CONNECTION);
            statement = connection.createStatement();
            createTables();

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void insertData(String pattern, int x, int y, boolean iswon) throws SQLException{
        String[] patterns = OthelloData.getOthelloPatterns(pattern);
        int[] xs = OthelloData.getXs(x, y);
        int[] ys = OthelloData.getYs(x, y);
        String query = "SELECT * FROM "+TABLE_DATA+" WHERE ";
        for(int i=0;i<8;i++){
            query += "("+COLUMN_PATTERN + "='" + patterns[i] +"' and "+COLUMN_NEXT_X+"="+xs[i]+" and "+COLUMN_NEXT_Y+"="+ys[i]+") or ";
        }
        query = query.substring(0, query.length()-3)+";";
        ResultSet rs = statement.executeQuery(query);
        
        if(rs.next()){
            String p = rs.getString(COLUMN_PATTERN);
            long win = rs.getLong(COLUMN_WIN);
            long lose = rs.getLong(COLUMN_LOSE);
            int next_x = rs.getInt(COLUMN_NEXT_X);
            int next_y = rs.getInt(COLUMN_NEXT_Y);
            if(iswon){
                query = "UPDATE "+TABLE_DATA+" SET "+COLUMN_WIN+"="+(win+1)+" where "+COLUMN_PATTERN+"='"+p+"' and "+COLUMN_NEXT_X+"="+next_x+" and "+COLUMN_NEXT_Y+"="+next_y+";";
            } else {
                query = "UPDATE "+TABLE_DATA+" SET "+COLUMN_LOSE+"="+(lose+1)+" where "+COLUMN_PATTERN+"='"+p+"' and "+COLUMN_NEXT_X+"="+next_x+" and "+COLUMN_NEXT_Y+"="+next_y+";";
            }
        } else {
            query = insert(TABLE_DATA, pattern, iswon?1:0, iswon?0:1, x, y);
        }
        synchronized (statement) {
            statement.execute(query);
        }
    }

    public void close() throws SQLException{
        connection.close();
        statement.close();
    }

    public List<OthelloData> getData(String pattern) throws SQLException{
        String query = "select * from " + TABLE_DATA + " where ";
        String[] subpatterns = OthelloData.getOthelloPatterns(pattern);
        for(String subpattern:subpatterns){
            query += COLUMN_PATTERN+"='"+subpattern+"' or ";
        }
        ResultSet rs;
        synchronized (statement) {
            rs = statement.executeQuery(query.substring(0, query.length()-3)+";");
        }
        List<OthelloData> data = new ArrayList<OthelloData>();
        while(rs.next()){
            String p = rs.getString(COLUMN_PATTERN);
            int i;
            for(i=0;i<8;i++){
                if(subpatterns[i].equals(p)){
                    break;
                }
            }
            int[] coordinate = OthelloData.restoreCoordinate(rs.getInt(COLUMN_NEXT_X), rs.getInt(COLUMN_NEXT_Y), i);
            data.add(new OthelloData(pattern,
                                   rs.getLong(COLUMN_WIN),
                                   rs.getLong(COLUMN_LOSE),
                                   coordinate[0],
                                   coordinate[1]));
        }
        return data;
    }

    public void onCreate() throws SQLException{
        statement.execute(SQL_CREATE_DATA);
        statement.execute(SQL_CREATE_VERSION);
        statement.execute(insert(TABLE_VERSION, String.valueOf(DB_VERSION)));
    }

    public void onUpgrade(){

    }

    private void createTables() throws SQLException{
        ResultSet resultSet = null;
        connection = DriverManager.getConnection(SQLITE_CONNECTION);
        resultSet = statement.executeQuery("SELECT * FROM sqlite_master WHERE type='table';");
        boolean hasVersionTable = false;
        while(resultSet.next()){
            if(resultSet.getString(2).equals(TABLE_VERSION)){
                hasVersionTable = true;
                break;
            }
        }
        resultSet.close();
        if(hasVersionTable){
            resultSet = statement.executeQuery("SELECT * FROM "+TABLE_VERSION+";");
            if(resultSet.next()&&resultSet.getInt(COLUMN_VERSION)!=DB_VERSION){
                onUpgrade();
            }
        } else {
            onCreate();
        }
    }

    private String insert(String table, Object... params){
        String out = "INSERT INTO "+table+" VALUES(";
        for(Object param:params){
            if(param instanceof String){
                out += " '"+param+"',";
            } else {
                out += " "+String.valueOf(param)+",";
            }
        }
        out = out.substring(0, out.length()-1);
        out += ");";
        return out;
    }
}
