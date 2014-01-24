package dbal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import play.db.DB;

public class Dba {
	Connection dbConn;
	Statement stmt;
	ResultSet rs;
	public Dba() throws SQLException {
		dbConn =DB.getConnection();
		stmt=dbConn.createStatement();
	}
	
	public String getIP(String hostName) throws SQLException {
		String hostIP=null;		
		String query="SELECT hostIP FROM Host WHERE hostName = '"+ hostName+"'";
		rs = stmt.executeQuery(query);
		if(rs.next()){
			hostIP=rs.getString("hostIP");
			rs.close();
			stmt.close();
			return hostIP;
		}else{		
			rs.close();
			return null;
		}			
	}
	
	public ArrayList<String> getHostList() throws SQLException  {
		
		ArrayList<String> hostList;
		stmt=dbConn.createStatement();
	  	String query="SELECT hostName FROM Host";
	  	rs = stmt.executeQuery(query);
	  	hostList=new ArrayList<String>();
	  	while(rs.next()){
	  		hostList.add(rs.getString("hostName"));
	  	}
	  	rs.close();
	  	return hostList;
	}

	public boolean ishostExist(String hostName) throws SQLException {
		rs=stmt.executeQuery("SELECT COUNT(*) AS total FROM Host WHERE hostName = '"+hostName+"'");
		if(rs.next()) {
			if(rs.getInt("total")!=0) {
				return true;
			}else {
				return false;
			}
		}
		return false;					
	}
	
/*	public boolean isIPExist(String hostIP) throws SQLException {
		rs=stmt.executeQuery("SELECT COUNT(*) AS total FROM Host WHERE hostIP = '"+hostIP+"'");
		if(rs.next()) {
			if(rs.getInt("total")!=0) {
				return true;
			}else {
				return false;
			}
		}
		return false;					
	}
	
	public void addHost(String hostIP, String hostName) throws SQLException {
		stmt.executeUpdate("INSERT INTO Host VALUES('"+hostIP+"','"+hostName+"')");
		return;	
	}
	
	public void deleteHost(String hostIP) throws SQLException {
		stmt.executeUpdate("DELETE FROM Host WHERE hostIP = '"+hostIP+"'");
		return;
	}
	*/	
	public void close() throws SQLException {
		if(rs!=null)
			rs.close();
		if(stmt!=null)
			stmt.close();
		if(dbConn!=null)
			dbConn.close();		
	}
	
	protected void finalize() throws SQLException  {
		if(rs!=null)
			rs.close();
		if(stmt!=null)
			stmt.close();
		if(dbConn!=null)
			dbConn.close();
	}
}
