
package dbal;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Host;
import play.db.DB;
import play.libs.Json;

public class VMStatus extends Thread {
	static Connection dbConn=DB.getConnection();
	
	
	public void run() {
		PreparedStatement inprepstmt;
		PreparedStatement delprepstmt;
		DomainInfo vmInfoSample1;
		DomainInfo vmInfoSample2;
		double percpu,permem;
		long cpuTimeSample1;
		long cpuTimeSample2;
		long cpuTimeNodeDiff;
		long cpuTImeVMdiff;
		int cores;
		Host tempHost;
		ArrayList<String> hostList;
		ArrayList<Domain> vmList;
		
		try {
			Dba db=new Dba();
			inprepstmt = dbConn.prepareStatement("INSERT INTO VM VALUES(?,?,?,?)");
			delprepstmt = dbConn.prepareStatement("DELETE FROM VM WHERE OID IN "+
					"(SELECT OID FROM VM WHERE vmuuid = ? ORDER BY OID DESC OFFSET 35)");
			//missing case for deleting inactive vm listho
			while(true) {
				hostList=db.getHostList();
				for(String hostName : hostList) {
					try{
						tempHost=new Host(hostName);
						vmList=tempHost.listVM(1);
						
						cores=tempHost.conn.nodeInfo().cores;
						for( Domain vm : vmList) {
							try {
								vmInfoSample1 = vm.getInfo();
								cpuTimeSample1=System.nanoTime();
								try {
									sleep(500);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}								
								vmInfoSample2 = vm.getInfo();
								cpuTimeSample2=System.nanoTime();
								cpuTimeNodeDiff=cpuTimeSample2-cpuTimeSample1;
								cpuTImeVMdiff=vmInfoSample2.cpuTime-vmInfoSample1.cpuTime;
								//System.out.println("vm diff"+cpuTImeVMdiff);
								//System.out.println("node diff"+cpuTimeNodeDiff);
								percpu=(100*cpuTImeVMdiff)/(cpuTimeNodeDiff*cores);
								//System.out.println("getting status of vm : "+ vm.getName() +"");
								//System.out.println("getting status of vmcpu : "+ percpu +"");
								permem = vmInfoSample2.memory*100/vmInfoSample2.maxMem;
								//System.out.println("getting status of vmmemory : "+ permem +"");
							
								inprepstmt.setString(1,vm.getUUIDString());
								inprepstmt.setString(2,vmInfoSample2.state.toString());
								inprepstmt.setBigDecimal(3,new BigDecimal(percpu).setScale(2));
								inprepstmt.setBigDecimal(4,new BigDecimal(permem).setScale(2));
								inprepstmt.executeUpdate();
								delprepstmt.setString(1,vm.getUUIDString());
								delprepstmt.executeUpdate();
								vm.free();
							} catch (SQLException | LibvirtException e) {
								e.printStackTrace();
							}
						}
						
						tempHost.close();
						tempHost=null;
					} catch (LibvirtException e1) {
						e1.printStackTrace();
					}
				}
			}
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ObjectNode getVMStatus(String uuid) throws SQLException{
		
		String sql="SELECT * FROM VM WHERE vmuuid = '"+uuid+"' ORDER BY OID DESC LIMIT 30";
		Statement stmt=dbConn.createStatement();
		ResultSet rs=null;
		ObjectNode jso=null;
		rs=stmt.executeQuery(sql);
		//int rowcount=0;
		/*if (rs.last()) {
			rowcount = rs.getRow();
			rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
		}*/
		BigDecimal[] cpu=new BigDecimal[30];
		BigDecimal[] memory=new BigDecimal[30];
		String[] state=new String[30];
		int index=0;
		while (rs.next()) {
			cpu[index]=rs.getBigDecimal("cpu");
			memory[index]=rs.getBigDecimal("memory");
			state[index]=rs.getString("state");
			index++;
		}
		if(rs!=null)
		rs.close();
		if(stmt!=null)
		stmt.close();
		jso=Json.newObject();
		jso.put("cpu",Json.toJson(cpu));
		jso.put("memory",Json.toJson(memory));
		jso.put("state",Json.toJson(state));
		return jso;
	}
	
}

