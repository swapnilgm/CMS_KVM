
package dbal;

import java.io.IOException;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.TimerTask;

import org.libvirt.Connect;
import org.libvirt.ConnectAuth;
import org.libvirt.ConnectAuthDefault;
import org.libvirt.LibvirtException;

import play.db.DB;

public class HostList extends TimerTask {
	public static String subnet;
	
	public void run() {
		//to probe the network and load list of hodt with hyperviso in database.
		int TIMEOUT=1000;    		
		String hostIP;
		Connect conn;
		Connection dbConn;
		PreparedStatement pstmt;
		Statement stmt;
		ResultSet rs;
		boolean found;
		String local=new String("localhost");
		try {
			stmt=DB.getConnection().createStatement();
			rs=stmt.executeQuery("SELECT COUNT(*) AS total FROM Host WHERE hostIP = '"+local+"'");
			if (rs.next()) {
				if(rs.getInt("total")==0){					
					stmt.executeUpdate("INSERT INTO Host VALUES('"+local+"','"+local+"')");
				}
			}
			rs.close();
			stmt.close();
		} catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		while(true){
			try {
				dbConn = DB.getConnection();
				stmt=dbConn.createStatement();
				pstmt=dbConn.prepareStatement("INSERT INTO Host VALUES(?,?)");
				for (int i=1;i<255;i++) {
					hostIP=subnet + "." + i;
					rs=stmt.executeQuery("SELECT COUNT(*) AS total FROM Host WHERE hostIP = '"+hostIP+"'");
					if (rs.next()) {
						found=rs.getBoolean("total");
					}
					else
						found=false;
					try {
						if (InetAddress.getByName(hostIP).isReachable(TIMEOUT)){
							String hostURI="qemu+tcp://"+hostIP+ "/system";
							ConnectAuth ca= new ConnectAuthDefault();
							conn=new Connect(hostURI,ca,0); //connecting to hypervisor		    
							if (conn.isConnected()){
								if(!found){
									pstmt.setString(1, hostIP);
									pstmt.setString(2, conn.getHostName());
									if(pstmt.executeUpdate()>0){
										System.out.println("Host "+hostIP+ " up.");
									}
								}
								conn.close();
								
							}else {
								if(found){
									stmt.executeUpdate("DELETE FROM Host WHERE hostIP = '"+hostIP+"'");
									System.out.println("Host "+ hostIP +" down.");
								}
							}
						}else {
							if(found){
								stmt.executeUpdate("DELETE FROM Host WHERE hostIP = '"+hostIP+"'");
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
