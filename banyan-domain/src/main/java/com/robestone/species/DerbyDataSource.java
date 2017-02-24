package com.robestone.species;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;

import javax.sql.DataSource;

import org.apache.commons.io.IOUtils;
import org.apache.derby.jdbc.EmbeddedDataSource;
import org.apache.derby.tools.ij;

public class DerbyDataSource {

	public static void main(String[] args) throws Exception {
		create();
	}
	private static DataSource dataSource;
	
	/**
	 * http://db.apache.org/derby/docs/10.2/tuning/rtunproper81359.html#rtunproper81359
	 * derby.storage.pageCacheSize
	 * Default 1000 pages.
	 * The minimum value is 40 pages
	 */
	private static String numberOfCachedPages = "40";
	
//	public static String dbPath = "D:\\banyan-db\\derby-bak-1";
	public static String dbPath = null;
	
	private static String defaultWindowsPath = "D:\\banyan-db\\derby";
	private static String defaultLinuxPath = "/home/public/banyan/banyan-db";
	
	private static void create() throws Exception {
		EmbeddedDataSource ds = (EmbeddedDataSource) getDataSource();
		ds.setCreateDatabase("create"); // only needed once
		Connection connection = ds.getConnection();

		runScript("all-tables-create.sql", connection);
		runScript("create-tree-of-life-root.sql", connection);
	}
	
	private static void runScript(String file, Connection connection) throws Exception {
		byte[] sql = IOUtils.toByteArray(new FileInputStream(
				"D:\\eclipse-workspaces\\git\\banyan\\banyan-domain\\src\\main\\resources\\sql\\" + file
				));
		
		InputStream input = new ByteArrayInputStream(sql);
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		ij.runScript(connection, input, "UTF-8", output, "UTF-8");
		System.out.println(new String(output.toByteArray()));
	}
	
	public static DataSource getDataSource() {
		if (dataSource == null) {
			System.setProperty("derby.system.home", 
//					"D:\\eclipse-workspaces\\git\\roots-web\\src\\main\\derby"
//					"D:\\eclipse-workspaces\\git\\banyan\\banyan-domain\\src\\main\\derby"
//					"D:\\banyan-db\\derby"
//					"D:\\banyan-db\\derby-bak-1"
					getDerbyHome()
					);
			System.setProperty("derby.language.sequence.preallocator", "1");
			System.setProperty("derby.storage.pageCacheSize", numberOfCachedPages);
			
			EmbeddedDataSource ds = new EmbeddedDataSource();
			ds.setDatabaseName("species");
//			ds.setConnectionAttributes("shutdown=true");
			
			dataSource = ds;
		}
		return dataSource;
	}
	private static String getDerbyHome() {
		if (dbPath != null) {
			return dbPath;
		} else if (new File(defaultWindowsPath).exists()) {
			return defaultWindowsPath;
		} else {
			return defaultLinuxPath;
		}
	}
	
}
