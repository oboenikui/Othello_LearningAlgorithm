package com.oboenikui.othello.test;

import java.sql.SQLException;
import java.util.List;

import com.oboenikui.othello.OthelloData;
import com.oboenikui.othello.SQLiteDb;

public class SQLiteDbTest {
    public static void main(String[] args) throws SQLException {
        SQLiteDb sqDb = SQLiteDb.getInstance();
        List<OthelloData> data = sqDb.getData("-------------------X-------XX------XO---------------------------");
        for(OthelloData data1:data){
            L.o(data1.pattern,data1.next_x,data1.next_y);
        }
    }
}
