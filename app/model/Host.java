package model;
	
import java.io.IOException;
import java.net.*;
import java.sql.*;
import java.util.ArrayList;
import javax.sql.DataSource;
import org.libvirt.*;

import com.fasterxml.jackson.databind.JsonNode;

import play.db.DB;

public class Host {
	
	public Connect conn;
	static DataSource ds=DB.getDataSource();
	static Connection dbConn;
	static Statement stmt;
	static ResultSet rs;
	//private String hostIP;
	//private string hostName;
	
	public Host(String hostName) throws LibvirtException
    {
		String hostIP=null;
    // 	#sql {select hostIPaddr into :hostIP from host where hostname =  :hostname };
		try {
			dbConn =ds.getConnection();
			stmt=dbConn.createStatement();
			String query="SELECT hostIP FROM Host WHERE hostName = '"+ hostName+"'";
			rs = stmt.executeQuery(query);
			if(rs.next()){
				hostIP=rs.getString("hostIP");
			}
	  	  	stmt.close();
	  	  	dbConn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
      ConnectAuth ca= new ConnectAuthDefault();
      System.out.println("qemu+tcp://" + hostIP + "/system");
      conn=new Connect("qemu+tcp://" + hostIP + "/system",ca,0); //connecting to hypervisor 
    }
	
	
	
	public static ArrayList<String> getHostList() throws SQLException  {
			dbConn =ds.getConnection();
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
	
	public static void loadHostList(String subnet) throws SQLException  {
		//to probe the network and load list of hodt with hyperviso in database.
		int timeout=1000;
		
	//    ArrayList<String> hostURIList = new ArrayList<String>() ;
	    String hostIP;
	    Connect conn;
	    dbConn=ds.getConnection();
    	stmt=dbConn.createStatement();
    	
	    for (int i=82;i<144;i++){
	    	hostIP=subnet + "." + i; 	 
	        try {
				if (InetAddress.getByName(hostIP).isReachable(timeout)){
				
						String hostURI="qemu+tcp://"+subnet + "." + i + "/system";
						ConnectAuth ca= new ConnectAuthDefault();
					    conn=new Connect(hostURI,ca,0); //connecting to hypervisor		    
				  		    
					    if (conn.isConnected()){
					    	if(stmt.executeUpdate("INSERT INTO Host VALUES('"+hostIP+"','"+conn.getHostName()+"')")>0);
					    		System.out.println("row added to table");
					   // 	hostURIList.add(conn.getHostName());	                	                        	  
					    }
					    conn.close();
				}
			} catch ( IOException | LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	        
			        
	     }
	    stmt.close();
	    dbConn.close();
	    
		//return hostURIList;
	}
	
    public int create(JsonNode json)  
    {
    	//String shortdesc="creating with test aspp";
		String xml=new String();
		String bootDev=json.findPath("bootType").asText();
		 //os boot device "fd", "hd", "cdrom" or "network"
      xml="  <domain type='kvm'>"+
      "<name>"+json.findPath("vmName").textValue()+"</name>"+
      "<memory unit='MiB'>"+String.valueOf(json.findPath("memory").asInt())+"</memory>"+
      "<vcpu>"+String.valueOf(json.findPath("vcpu").asText())+"</vcpu>"+
      "<os>"+
        "<type arch='x86_64'>hvm</type>"+
        "<boot dev='"+bootDev+"'/>"+
      "</os>"+
      "<clock offset='utc'/>"+
      "<features>     <acpi/>       <apic/>	 <hap/><pae/>	     </features>"+
      "<on_poweroff>destroy</on_poweroff>"+
      "<on_reboot>restart</on_reboot>"+
      "<on_crash>restart</on_crash>"+
          "<devices>"+        
       " <emulator>/usr/bin/kvm-spice</emulator>";
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
      
      /* "<disk type='file' device='cdrom'>"+
          "<source file='/media/swapnil/Softwares/OS images/"+json.findPath("iso").asText()+".iso'/>"+
         "<target dev='hdc'/>"+
       "<readonly/>"+
       "<boot order='1'/>"+
       "</disk>"+ 
        */
     /* "<disk type='file' device='disk'>"+
        "<source file='/media/swapnil/Storage/"+json.findPath("vmName").textValue()+".img'/>"+
        "<target dev='hda'/>"+
      "</disk>"+*/
      xml=xml.concat("<interface type='network'>"+
          "<source network='default'/>"+
        "</interface>"+
        "<input type='mouse' />"+
        "<graphics type='vnc' port='-1' autoport='yes'/>"+
      "</devices>"+
    "</domain>");
      System.out.println(xml);
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
			e.printStackTrace();
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
	public  static ArrayList<Domain> staticListAllVM(int filter) throws LibvirtException, SQLException {
		DataSource ds = DB.getDataSource();
    	Connection dbConn=ds.getConnection();
    	Statement stmt=dbConn.createStatement();
    	String query="SELECT hostName FROM Host";
	  	ResultSet rs = stmt.executeQuery(query);
	  	ArrayList<Domain> vmList=new ArrayList<Domain>();
	  	Host tempHost;
	  	while (rs.next())
		{
	  		tempHost=new Host(rs.getString("hostName"));
			vmList.addAll(tempHost.staticListVM(filter));						
		}
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
	        
	public  ArrayList<Domain> dynamicListVM(String hypURI, int filter) throws LibvirtException {
		
	        conn=new Connect(hypURI,true); //connecting to hypervisor
	        ArrayList<Domain> vmList=new ArrayList<Domain>();
	                       
	        if(filter == 1) {
	        	int[] activeVMs = conn.listDomains();
	            for(int i = 0; i < conn.numOfDomains(); i++)
	            {
	            	vmList.add(conn.domainLookupByID(activeVMs[i]));
	            }                                
	        }                        
	                                
	        conn.close();
	        return vmList;
                           
	}
	
	
	/*
	private enum ListVMFilter {
	LIST_VM_FILTER, ACTIVE_VM_FILTER, DEFINED_VM_FILTER
	}
	*/
}