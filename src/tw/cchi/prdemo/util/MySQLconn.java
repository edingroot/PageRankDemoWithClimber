package tw.cchi.prdemo.util;

import java.sql.Connection; 
import java.sql.DriverManager; 
import java.sql.SQLException; 

public class MySQLconn { 
	private static String host = "localhost";
	private static String database = "chi_pagerank";
	private static String username = "chi_pagerank";
	private static String password = "bBjlab6Q7umf";
	
	public static Connection getConnObject() {
		Connection conn = null;
		try {
			Class.forName("com.mysql.jdbc.Driver");
			String connStr = String.format("jdbc:mysql://%s/%s?useUnicode=true&characterEncoding=utf8", host, database);
			//註冊driver
			conn = DriverManager.getConnection (connStr, username, password); 
		} catch (ClassNotFoundException e) { 
			System.out.println("DriverClassNotFound: " + e.toString()); 
		} 
		catch(SQLException e) { 
			System.out.println("Error connecting database: " + e.toString()); 
		}
		return conn;
	}
	
	// remember: close all object to avoid connection poor problem when waiting timeout 
}
