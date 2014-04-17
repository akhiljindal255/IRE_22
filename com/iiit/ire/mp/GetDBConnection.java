package com.iiit.ire.mp;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class GetDBConnection {

	private String dbUrl;
	public static Connection con;
	private static GetDBConnection instance = null;
	
	protected GetDBConnection() {
		dbUrl = "jdbc:mysql://localhost/nasdaq?user=root&password=123";
		String dbClass = "com.mysql.jdbc.Driver";
		try {
			Class.forName(dbClass);
			con = DriverManager.getConnection(dbUrl);
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	public static synchronized GetDBConnection getInstance() {
        if (instance == null) {
            instance = new GetDBConnection();
        }
        return instance;
    }
}