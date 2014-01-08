import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.*;

import javax.sql.DataSource;

import model.Monitor;

import play.Application;
import play.GlobalSettings;
import play.db.*;


public class Global extends GlobalSettings {
	
    public void onStart(Application app) {
    	loadHost();
    	//get subnet 
    	BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
    	System.out.println("Enter subnet /24 :");
	    try {
			String subnet = bufferRead.readLine();
			Monitor.loadHostList(subnet);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
    }
    private void loadHost() {
    	//create tables
    	DataSource ds = DB.getDataSource();
    	Connection dbConn=null;
    	Statement stmt=null;
    	
		try {
			System.out.println("Creating table in given database...");
			dbConn = ds.getConnection();
		    stmt= dbConn.createStatement();
		    String sql="CREATE TABLE IF NOT EXISTS Host " +
	                "(hostIP VARCHAR(255), " + 
	                " hostName VARCHAR(255) NOT NULL, " + 
	                " PRIMARY KEY ( ip ))"; 
	    	if((stmt.executeUpdate(sql))<0)
	    		System.out.println("Created host table in given database...");
	    	String sql="CREATE TABLE IF NOT EXISTS VM " +
	                "(hostIP VARCHAR(255), " + 
	                " vm VARCHAR(255), " + 
	                " PRIMARY KEY(vm),"+
	                "foreign key(hostIP) references host(hostIP))"; 
	    	if((stmt.executeUpdate(sql))<0)
	    		System.out.println("Created domain table in given database...");
	    		    	
	    	stmt.close();
	    	dbConn.close();
	    			    
		    
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}
