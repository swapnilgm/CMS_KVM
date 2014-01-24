package dbal;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.TimerTask;

import org.libvirt.Domain;
import org.libvirt.DomainInfo;
import org.libvirt.LibvirtException;

import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Host;
import play.db.DB;
import play.libs.Json;

public class VMStatus extends TimerTask{
	Connection dbConn;
	public VMStatus() {
		dbConn=DB.getConnection();
	}
	public void run() {
		ArrayList<Domain> vmList;
		PreparedStatement inprepstmt;
		PreparedStatement delprepstmt;
		DomainInfo vmInfo=null;
		float percpu,permem;
		try {
			inprepstmt = dbConn.prepareStatement("INSERT INTO VM VALUES(?,?,?,?)");
			delprepstmt = dbConn.prepareStatement("DELETE FROM VM WHERE OID IN "+
					"(SELECT OID FROM activeVM WHERE vmuuid = ? ORDER BY OID DESC OFFSET 100)");
			//missing case for deleting inactive vm list
			while(true){
				try {
					vmList=Host.staticListAllVM(1);
					for( Domain vm : vmList) {
						try {
							vmInfo = vm.getInfo();
							percpu = vmInfo.cpuTime*100;
							permem = vmInfo.memory*100/vmInfo.maxMem;
							inprepstmt.setString(1,vm.getUUIDString());
							inprepstmt.setString(2,vmInfo.state.toString());
							inprepstmt.setFloat(3,percpu);
							inprepstmt.setFloat(4,permem);
							inprepstmt.executeUpdate();
							delprepstmt.setString(1,vm.getUUIDString());
							delprepstmt.executeUpdate();
						} catch (SQLException | LibvirtException e) {
							e.printStackTrace();
						}
					}
				} catch (LibvirtException e1) {
					e1.printStackTrace();
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ObjectNode getVMStatus(String uuid) throws SQLException{
		
		String sql="SELELCT * FROM VMStatus WHERE vmuuid = ? ORDER BY OID DESC";
		PreparedStatement pstmt=dbConn.prepareStatement(sql);
		ResultSet rs=null;
		ObjectNode jso=null;
		pstmt.setString(1, uuid);
		rs=pstmt.executeQuery();
		int rowcount=0;
		if (rs.last()) {
			rowcount = rs.getRow();
			rs.beforeFirst(); // not rs.first() because the rs.next() below will move on, missing the first element
		}
		float[] cpu=new float[rowcount];
		float[] memory=new float[rowcount];
		String[] state=new String[rowcount];
		int index=0;
		while (rs.next()) {
			cpu[index]=rs.getFloat("cpu");
			memory[index]=rs.getFloat("memory");
			state[index]=rs.getString("state");				
		}
		rs.close();
		jso=Json.newObject();
		jso.put("Name",rs.getFloat("cpu"));
		jso.put("memory",rs.getFloat("memory"));
		jso.put("state",rs.getFloat("state"));
		return jso;
	}
	
}


