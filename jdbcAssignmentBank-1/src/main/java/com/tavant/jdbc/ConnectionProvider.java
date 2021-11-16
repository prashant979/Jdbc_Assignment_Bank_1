package com.tavant.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConnectionProvider {

	public static final String URLCONNECTION = "jdbc:mysql://localhost:3306/";
	public static final String DATABASE = "jdbc_assignment_1";
	public static final String USERNAME = "root";
	public static final String USERPASSWORD = "root";

	private static String query = "";

	public static Connection getConnection() {
		try (Connection conn = DriverManager.getConnection(URLCONNECTION, USERNAME, USERPASSWORD);
				Statement stmt = conn.createStatement();) {

			query = "CREATE DATABASE IF NOT EXISTS " + DATABASE;
			stmt.executeUpdate(query);
		//	System.out.println("Database created successfully...");
						
			query = "USE " + DATABASE;
			stmt.executeUpdate(query);
			
			query = "CREATE TABLE IF NOT EXISTS ACCOUNTS " +
	                   "(id INTEGER not NULL AUTO_INCREMENT, " +
	                   " name VARCHAR(255), " + 
	                   " password VARCHAR(255), " + 
	                   " balance INTEGER not NULL, " +  
	                   " PRIMARY KEY ( id ))"; 
			
			stmt.executeUpdate(query);
			
			query = "CREATE TABLE IF NOT EXISTS TRANSACTIONS " +
	                   "(id INTEGER not NULL AUTO_INCREMENT, " +          
	                   " balance INTEGER not NULL, " +  
	                   " status VARCHAR(255), " + 
	                   " whome VARCHAR(255), " + 
	           		   " transactionTime TimeStamp," +	    			 
	                   " accountId VARCHAR(255), " +   
	                   " PRIMARY KEY ( id ))"; 
			
			stmt.executeUpdate(query);
			conn.close();
			return conn;
		} catch (SQLException e) {
			System.out.println("Database Problem: " + e.getMessage());
		} 
		return null;
	}

}
