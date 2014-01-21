package model;
	
import java.sql.*;
import java.util.ArrayList;

import org.libvirt.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.db.DB;
import play.libs.Json;

public class Host {
	
	public Connect conn;
	static Connection dbConn=DB.getConnection();
	static Statement stmt;
	static ResultSet rs;
	//private String hostIP;
	//private string hostName;
	
	public Host(String hostName) throws LibvirtException
    {
		String hostIP=null;
    // 	#sql {select hostIPaddr into :hostIP from host where hostname =  :hostname };
		try {
			dbConn =DB.getConnection();
			stmt=dbConn.createStatement();
			
			String query="SELECT hostIP FROM Host WHERE hostName = '"+ hostName+"'";
			rs = stmt.executeQuery(query);
			if(rs.next()){
				hostIP=rs.getString("hostIP");
			}
			stmt.close();
			dbConn.close();
		} catch(SQLException e){
			System.err.println(e.getMessage());
		}
      ConnectAuth ca= new ConnectAuthDefault();
      System.out.println("qemu+tcp://" + hostIP + "/system");
      conn=new Connect("qemu+tcp://" + hostIP + "/system",ca,0); //connecting to hypervisor 
    }
	
	
	
	public static ArrayList<String> getHostList() throws SQLException  {
			dbConn =DB.getConnection();
			stmt=dbConn.createStatement();
		  	String query="SELECT hostName FROM Host";
		  	rs = stmt.executeQuery(query);
		  	ArrayList<String> hostList=new ArrayList<String>();
		  	while(rs.next()){
		  		hostList.add(rs.getString("hostName"));
		  	}
		  	stmt.close();
		    dbConn.close();
		  	return hostList;
	}
	
    public int create(JsonNode json)  
    {
    	String xml=new String();
    	//String shortdesc="creating with test aspp";
    	String bootDev=json.findPath("bootType").asText();
		 //os boot device "fd", "hd", "cdrom" or "network"
		xml="  <domain type='kvm'>"+
				"<name>"+json.findPath("vmName").textValue()+"</name>"+
				"<memory unit='MiB'>"+String.valueOf(json.findPath("memory").asInt())+"</memory>"+
				"<vcpu>"+String.valueOf(json.findPath("vcpu").asText())+"</vcpu>";
		String title=json.findPath("title").textValue();
		if(title!=null)
			xml=xml.concat("<title>"+title+"</title>");
		String description=json.findPath("description").textValue();
		if(description!=null)
			xml=xml.concat("<description>"+description+"/description>");
		String arch=json.findPath("arch").textValue();
		if(arch==null)
			arch="'x86_64'";
		xml=xml.concat("<os>"+
				"<type arch='x86_64'>hvm</type>"+
				"<boot dev='"+bootDev+"'/>");
		String bootmenu=json.findPath("bootmenu").textValue();
		if(bootmenu!=null)
			xml=xml.concat("<bootmenu enable='"+bootmenu+"'>");
		xml=xml.concat("</os>"+
				"<clock offset='utc'/>"+
				"<features>     <acpi/>       <apic/>	 <hap/><pae/>	     </features>"+
				"<on_reboot>restart</on_reboot>"+
				"<on_crash>restart</on_crash>"+
				"<on_poweroff>destroy</on_poweroff>"+
				"<devices>"+        
				"<emulator>/usr/bin/kvm-spice</emulator>");
	
		//disk device floppy", "disk", "cdrom
      switch (bootDev) {
      //case "fd": xml=xml.concat("<disk type='file' device='floppy'>");
		//	break;
      case "cdrom": xml=xml.concat("<disk type='file' device='cdrom'>"+
			"<source file='"+json.findPath("iso").asText()+"'/>"+
			"<target dev='hdc'/>"+
			"<readonly/>"+
    		  "</disk>");
	
      break;
      case "hd": xml=xml.concat("<disk type='file' device='disk'>"+
    		  "<source file='"+json.findPath("disk").asText()+"/>"+
    		  "<target dev='hda'/>"+
    	      "</disk>");
      
      break;
      case "network": xml=xml.concat("<disk type='net'>");
	
      break;


	default:
		break;
	}
      
      xml=xml.concat("<interface type='network'>"+
    		  "<source network='default'/>"+
    		  "</interface>"+
    		  "<input type='mouse' />"+
    		  "<graphics type='vnc' port='-1' autoport='yes'/>"+
    		  "</domain>"+
    		  "</devices>");
      
	    try {
	    	if(json.findPath("persistant").asText().compareTo("Persistent")==0){
	    		Domain vm=conn.domainDefineXML(xml);
	    		if(vm!=null){
	    			vm.create();
	    			return 1;
	    		}
				else return 0;
	    	}
	    	else{
	    		if(conn.domainCreateXML(xml,0)!=null)
	    		    			return 1;
				else return 0;
	    	}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			
			return -1;
		}
        
    }
   /* public  ArrayList<Domain> createStoragePool() {
    	<pool type="iscsi">
        <name>virtimages</name>
        <source>
          <host name="192.168.43.89:3260"/>
          <device path="iqn.2014-01.com.cmskvm:storage-server/images"/>
          <auth type='chap' username='CMS_KVM_ST'>
            <secret usage='libvirtiscsi'/>
          </auth>
        </source>
        <target>
          <path>/dev/sdb1</path>
        </target>
      </pool>
    	return TODO;
    }
*/
	public synchronized static ArrayList<Domain> staticListAllVM(int filter) throws LibvirtException, SQLException {
		Statement stmt=dbConn.createStatement();
    	String query="SELECT hostName FROM Host";
	  	ResultSet rs = stmt.executeQuery(query);
	  	ArrayList<Domain> vmList=new ArrayList<Domain>();
	  	Host tempHost;
	  	while (rs.next())
		{
	  		try{
	  			tempHost=new Host(rs.getString("hostName"));
	  			vmList.addAll(tempHost.staticListVM(filter));						
	  		}catch(LibvirtException e){
	  			//ignore
	  		}
	  		
		}
	  	rs.close();
	  	stmt.close();
	  	return vmList;		                           
	}
	 
	public  ArrayList<Domain> staticListVM(int filter) throws LibvirtException {
		ArrayList<Domain> vmList=new ArrayList<Domain>();
			if(filter == 1) {
	        	int[] activeVMs = conn.listDomains();
	            for(int i = 0; i < conn.numOfDomains(); i++) 
	            	vmList.add(conn.domainLookupByID(activeVMs[i]));	               
	        }
	                       
	        else if(filter == 2) {
	        	int[] activeVMs = conn.listDomains();
	            for(int i = 0; i < conn.numOfDomains(); i++)
	            {
	            	vmList.add(conn.domainLookupByID(activeVMs[i]));
	            }
	                            
	            String[] defVMList=conn.listDefinedDomains();
	            for(int i = 0; i < conn.numOfDefinedDomains(); i++)
	            {
	            	vmList.add(conn.domainLookupByName(defVMList[i]));
	            }                                
	       }
	                   
	       else if(filter == 0){
	            String[] defVMList=conn.listDefinedDomains();
	            for(int i = 0; i < conn.numOfDefinedDomains(); i++)
	            {
	            	vmList.add(conn.domainLookupByName(defVMList[i]));
	            }                        
	       }        
	                        
	       conn.close();
	       return vmList;		                           
	}
	        
	public  JsonNode dynamicListVM() throws LibvirtException, SQLException {
		dbConn=DB.getConnection();
		String sql="SELELCT * FROM activeVM WHERE vmuuid = ? ORDER BY OID DESC";
		PreparedStatement pstmt=dbConn.prepareStatement(sql);
		ResultSet rs=null;
		int [] vmID = conn.listDomains();
		ArrayList<ObjectNode> jsolist = new ArrayList<ObjectNode>();
		ObjectNode jso=null;
		Domain vm=null;
		for(int id: vmID) {
			vm=conn.domainLookupByID(id);
			pstmt.setString(1, vm.getUUIDString());
			rs=pstmt.executeQuery();
			while (rs.next()) {
				jso=Json.newObject();
				jso.put("id",id);
				jso.put("name",vm.getName());
				jso.put("cpu",rs.getFloat("cpu"));
				jso.put("memory",rs.getFloat("memory"));
				jso.put("state",rs.getFloat("state"));
				jsolist.add(jso);
				jso=null;
			}	
		}
		return Json.toJson(jsolist);      
	}
	
	public static void loadDynamicList() {
		new Thread(new DynamicVM()).start();
		
	}
	
	private static class DynamicVM implements Runnable{
		
		public void run() {
			ArrayList<Domain> vmList;
			Connection dbConn=DB.getConnection();
			PreparedStatement inprepstmt,delprepstmt;
			DomainInfo vmInfo=null;
			float percpu,permem;
			try {
				inprepstmt = dbConn.prepareStatement("INSERT INTO activeVM VALUES(?,?,?,?)");
				delprepstmt = dbConn.prepareStatement("DELETE FROM activeVM WHERE OID IN "+
						"(SELECT ROWID FROM activeVM WHERE vmuuid = ? ORDER BY OID DESC OFFSET 100)");
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
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					} catch (LibvirtException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
	}

	
	/*
	private enum ListVMFilter {
	LIST_VM_FILTER, ACTIVE_VM_FILTER, DEFINED_VM_FILTER
	}
	*/
}
