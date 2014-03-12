
import java.sql.*;

import javax.sql.DataSource;

import dbal.HostList;
import dbal.VMStatus;
import play.Application;
import play.GlobalSettings;
import play.db.*;
import java.util.Timer;

public class Global extends GlobalSettings {
	public void onStart(Application app) {

		initDB();
		//get subnet
//		HostList.subnet="10.42.0";
		Timer nwHostProber=new Timer(true);
		nwHostProber.schedule(new HostList(), 1000);
		Timer nwVMProber=new Timer(true);
		nwVMProber.schedule(new VMStatus(), 1000);
		//new Thread(new HostList()).start();
	//new Thread(new VMStatus()).start();
		
	}
	
	private void initDB() {
		//create tables
		DataSource ds=DB.getDataSource();
		Connection dbConn=null;
		Statement stmt=null;
		
		try {
			System.out.println("Creating table in given database ...");
			dbConn = ds.getConnection();
			stmt= dbConn.createStatement();
			String sql="CREATE TABLE IF NOT EXISTS Host " +
					"(hostIP VARCHAR(255), " + 
					" hostName VARCHAR(255) NOT NULL, " + 
					" PRIMARY KEY ( hostIP ))"; 
			stmt.executeUpdate(sql);
			stmt.executeUpdate("TRUNCATE TABLE Host");
			sql = "CREATE TABLE IF NOT EXISTS snapshot " +
					"(vmuuid VARCHAR(255) NOT NULL, " +
					" path VARCHAR(255) NOT NULL, "+
					"PRIMARY KEY ( vmuuid ))"; 
			if((stmt.executeUpdate(sql))<0)
				System.out.println("Created snapshot table in given database...");
			stmt.executeUpdate("TRUNCATE TABLE snapshot");
			sql = "CREATE TABLE IF NOT EXISTS VM " +
					"(vmuuid VARCHAR(255) NOT NULL, " +
					" state VARCHAR(255) NOT NULL, "+
					" cpu DECIMAL(5,2) NOT NULL, "+
					" memory DECIMAL(5,2) NOT NULL "+	
					") WITH OIDS";
			//		" time DATETIME NOT NULL)";
			if((stmt.executeUpdate(sql))<0)
				System.out.println("Created vmSaveSnapshot table in given database...");
			stmt.executeUpdate("TRUNCATE TABLE VM");
			stmt.close();
			dbConn.close();
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
}

