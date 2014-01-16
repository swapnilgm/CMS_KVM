
import java.net.UnknownHostException;
import java.sql.*;
import java.io.*;

import javax.sql.DataSource;
import model.Host;

import play.Application;
import play.GlobalSettings;
import play.db.*;


public class Global extends GlobalSettings {
	
    public void onStart(Application app) {
    	initDB();
    	  	//get subnet 
    	//BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in));
    	//System.out.println("Enter subnet /24 :");
	    try {
			//String subnet = bufferRead.readLine();
			//System.out.println(subnet);
			Host.loadHostList("192.168.43");
		}//catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			//
			//e1.printStackTrace();
		//} catch (IOException e1) {
			// TODO Auto-generated catch block
			//e1.printStackTrace();
		//} 
    catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private void initDB() {
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
	                " PRIMARY KEY ( hostIP ))"; 
	    	stmt.executeUpdate(sql);
	    		System.out.println("Created host table in given database...");
	   /* 	sql = "CREATE TABLE IF NOT EXISTS SavedImage " +
	                "(uuid VARCHAR(255), " + 
	                " path VARCHAR(255), " + 
	                " PRIMARY KEY(vm),"+
	                "foreign key(hostIP) references host(hostIP))"; 
	    	if((stmt.executeUpdate(sql))<0)
	    		System.out.println("Created domain table in given database...");
	   */ 		    	
	    	stmt.close();
	    	dbConn.close();
	    			    
		    
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
