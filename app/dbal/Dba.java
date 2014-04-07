
package dbal;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.libvirt.LibvirtException;
import org.libvirt.Network;

import model.Host;

import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.DB;
import play.libs.Json;

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
		String query="SELECT hostIP FROM Host WHERE hostName = '"+ hostName+"' AND active = '1'";
		rs = stmt.executeQuery(query);
		if(rs.next()){
			hostIP=rs.getString("hostIP");
			rs.close();
			return hostIP;
		}else{		
			rs.close();
			return null;
		}			
	}
	
	public synchronized ArrayList<String> getHostList() throws SQLException  {
		
		ArrayList<String> hostList;
		stmt=dbConn.createStatement();
		String query="SELECT hostName FROM Host where active = '1'";
		rs = stmt.executeQuery(query);
		hostList=new ArrayList<String>();
		while(rs.next()){
			hostList.add(rs.getString("hostName"));
		}
		rs.close();
		return hostList;
	}
	
	public boolean ishostExist(String hostName) throws SQLException {
		rs=stmt.executeQuery("SELECT COUNT(*) AS total FROM Host " +
				"WHERE hostName = '"+hostName+"' AND active = '1' ");
		if(rs.next()) {
			if(rs.getInt("total")!=0) {
				rs.close();
				return true;
			}else {
				rs.close();
				return false;
			}
		}
		rs.close();
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
	public int getStoreID (String hostName) throws SQLException {
		Boolean storeID=null;		
		String query="SELECT nfs1,nfs2,nfs3 FROM Host WHERE hostName = '"+ hostName+"' AND active = '1'";
		rs = stmt.executeQuery(query);
		if(rs.next()){
			storeID=rs.getBoolean(1);
			if(storeID) {
				storeID=rs.getBoolean(2);
				if(storeID) {
					storeID=rs.getBoolean(3);
					if(storeID) {
						rs.close();
						return 4;
					} else {
						rs.close();
						return 3;
					}
				} else {
					rs.close();
					return 2;
				}
			} else {
				rs.close();
				return 1;
			}				
			
		} else {		
			rs.close();
			return 0;
		}			
	}
	public boolean isName(String name) throws SQLException {
		rs=stmt.executeQuery("SELECT COUNT(*) AS total FROM Network WHERE NAME = '"+name+"'");
		if(rs.next()) {
			if(rs.getInt("total")!=0) {
				rs.close();
				return true;
			}else {
				rs.close();
				return false;
			}
		}
		rs.close();
		return false;					
		
		
	}
	
	
	
	public void addNetwork(String name, String hostName, String mode, String bridgeName, String autostart) throws SQLException {
		stmt.executeUpdate("INSERT INTO Network VALUES('"+name+"','"+hostName+"','"+mode+"','"+ bridgeName+"','"+ autostart+"')");
		
		
	}
	
	
	public void deleteNetwork(String name) throws SQLException {
		stmt.executeUpdate("DELETE FROM Network WHERE NAME = '"+name+"'");
		
	}
	
	public ArrayList<ObjectNode> getNetList() throws SQLException {
		
		ArrayList<ObjectNode> list=new ArrayList<ObjectNode>();
		
		ObjectNode json=Json.newObject();
		
		rs=stmt.executeQuery("SELECT NAME, HOST, MODE, BRIDGENAME, AUTOSTART FROM Network");
		while(rs.next())
		{
			json=Json.newObject();
			json.put("name",rs.getString("NAME"));	
			json.put("host",rs.getString("HOST"));
			json.put("mode",rs.getString("MODE"));
			json.put("bridgename",rs.getString("BRIDGENAME"));
			json.put("autostart",rs.getString("AUTOSTART"));
			
			try{
				Host tempHost=new Host(rs.getString("HOST"));
				Network net=tempHost.conn.networkLookupByName(rs.getString("NAME"));
				tempHost.close();
				if(net.isActive()==1)
					json.put("status","Active");
				else if(net.isActive()==0)
					json.put("status","Inactive");
				//else
				//throw new LibvirtException("Libvirt Error"); 
				
				
				
			}
			catch(LibvirtException e)
			{
				json.put("status","Not Connected");
				
			}
			finally{
				list.add(json);
				json=null;
				
			}
		}
		
		rs.close();
		if(stmt!=null)
			stmt.close();
		return list;
		
		
		
	}
	
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