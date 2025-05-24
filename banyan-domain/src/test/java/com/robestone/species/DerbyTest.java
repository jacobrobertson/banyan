package com.robestone.species;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class DerbyTest {
	
	public static void main(String[] args) throws Exception {
		String databaseURL;
		
		
		databaseURL = "jdbc:derby:testDB;create=true";
		test(databaseURL);
		databaseURL = "jdbc:derby:/c:/Users/jacob/eclipse-workspace-banyan/banyan-db/derby/species;create=false";
		test(databaseURL);
	}
	public static void test(String databaseURL) throws Exception {
		try {
	//		Class.forName("org.apache.derby.jdbc.EmbeddedDriver").newInstance();
			// "C:\\Users\\jacob\\eclipse-workspace-banyan\\banyan-db\\derby"
			Connection conn = DriverManager.getConnection(databaseURL);
			Statement statement = conn.createStatement();
			String sql = "select * from species WHERE latin_NAME like 'Testudo%' OR id = 15531";
			statement.execute(sql);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
