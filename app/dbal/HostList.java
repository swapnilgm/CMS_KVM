
package dbal;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Properties;
import java.util.TimerTask;

import model.IPv4;

import org.libvirt.Connect;
import org.libvirt.ConnectAuth;
import org.libvirt.ConnectAuthDefault;
import org.libvirt.LibvirtException;

import play.Play;
import play.db.DB;

public class HostList extends TimerTask {
	public static String subnet;
	
	public void run() {
		//to probe the network and load list of host with hypervisor in database.
		int TIMEOUT=1000;    		
		Connect conn;
		Connection dbConn=null;
		PreparedStatement pstmt;
		Statement stmt;
		ResultSet rs;
		boolean found, active;
		Properties defaultProps = new Properties();
		FileInputStream in;
		
		try {
			in = new FileInputStream(Play.application().getFile("/conf/datacenter.conf"));
			defaultProps.load(in);
			in.close();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		subnet=defaultProps.getProperty("subnet");
		IPv4 net=new IPv4(subnet);
		String local=new String("localhost");
		try {
			stmt=DB.getConnection().createStatement();
			rs=stmt.executeQuery("SELECT COUNT(*) AS total FROM Host WHERE hostIP = '"+local+"'");
			if (rs.next()) {
				if(rs.getInt("total")==0){					
					stmt.executeUpdate("INSERT INTO Host VALUES('"+local+"','"+local+"','0','0','0','1')");
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		List<String> compList=net.getAvailableIPs();
		while(true){
			try {
				if(dbConn==null)
					dbConn = DB.getConnection();
				stmt=dbConn.createStatement();
				pstmt=dbConn.prepareStatement("INSERT INTO Host VALUES(?,?,'0','0','0','1')");
				
				//probe each machine in subnet
				for (String hostIP : compList) {
					//check if its already in db					
					rs=stmt.executeQuery("SELECT active FROM Host WHERE hostIP = '"+ hostIP +"'");
					if (rs.next()) {
						found=true;
						active=rs.getBoolean("active");
					}
					else {
						found=false;
						active=false;
					}
					try {
						if (InetAddress.getByName(hostIP).isReachable(TIMEOUT)){
							//System.out.println("Connecting to libvirt on :: "+ hostIP);
							String hostURI="qemu+tcp://"+hostIP+ "/system";
							ConnectAuth ca= new ConnectAuthDefault();
							conn=new Connect(hostURI,ca,0); //connecting to hypervisor		    
							if (conn.isConnected()){
								if(!found){
									//if not there in db, add new entry 
									pstmt.setString(1, hostIP);
									pstmt.setString(2, conn.getHostName());
									if(pstmt.executeUpdate()>0){
										System.out.println("Host "+hostIP+ " up.");
									}
								}
								else if (!active) {								
									//logically activate
									stmt.executeUpdate("UPDATE Host set active = '1' WHERE hostIP = '"+hostIP+"'");
									System.out.println("Host "+ hostIP +" up.");
								}
								conn.close();
								
							}else {
								if(found && active){
									//delete logically
									stmt.executeUpdate("UPDATE Host set active = '0' WHERE hostIP = '"+hostIP+"'");
									System.out.println("Host "+ hostIP +" down.");
								}
							}
						}else {
							if(found && active){ 
								//delete logically
								stmt.executeUpdate("UPDATE Host SET active= '0' WHERE hostIP = '"+hostIP+"'");
								System.out.println("Host "+hostIP+ " down.");
							}
						}
					} catch ( IOException | LibvirtException | SQLException e) {
						// TODO Auto-generated catch block
						System.err.println(e.getMessage());
					}
				}
			}catch (SQLException e) {
				// TODO Auto-generated catch block
				
				System.err.println(e.getMessage());
			}
		}
	}
}
