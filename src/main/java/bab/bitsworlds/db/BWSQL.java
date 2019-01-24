package bab.bitsworlds.db;

import bab.bitsworlds.BitsWorlds;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class BWSQL {
    public static boolean sqlite;

    //DB Logins
    public static String host;
    public static int port = 0;
    public static String databaseName;
    public static String user = "";
    public static String pw = "";

    public static Connection dbCon;

    private static void setup() {

    }

    public static void connect() throws SQLException {
        try {
            if (sqlite) {

            }
            else {
                if (host == null || databaseName == null) {
                    throw new NullPointerException("Invalid MySQL credentials in config.yml");
                }

                Class.forName("com.mysql.jdbc.Driver");

                dbCon = DriverManager.getConnection("jdbc:mysql://" + host + ":" + port + databaseName, user, pw);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void disconnect() {
        if (dbCon != null) {
            try {
                dbCon.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void setupDB() {

    }
}
