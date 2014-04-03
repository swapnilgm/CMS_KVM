package model;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;

import org.libvirt.*;

import play.libs.Json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dbal.Dba;
import dbal.VMStatus;

public class Host {
	
	public Connect conn;
	//private String hostIP;
	//private string hostName;
	
	public Host(String hostName) throws LibvirtException, SQLException  {
		
		String hostIP=null;
		Dba d=new Dba();
		hostIP=d.getIP(hostName);
		d.close();
		ConnectAuth ca= new ConnectAuthDefault();
		conn=new Connect("qemu+tcp://" + hostIP + "/system",ca,0); //connecting to hypervisor 
	}
	
	public void close() throws LibvirtException	{
		
		if(conn!=null)
			conn.close();		
	}
	
	public static boolean ishostExist(String hostName) throws SQLException {
		Dba db=new Dba();
		boolean found=db.ishostExist(hostName);
		db.close();
		return found; 
	}
	
	public JsonNode getHostInfo() throws LibvirtException
	{
		JsonNode js=Json.toJson(this.conn.nodeInfo());
		return js;
		
	}
	
	public boolean validVMName(String vmName) throws LibvirtException {
		if(this.conn.domainLookupByName(vmName)!=null){
			return true;
		}else {
			return false;
		}
	}
	
	public String validCreateVMParam(JsonNode json) {
		String vmName = json.findPath("vmName").textValue();
		if(vmName == null) {
			
			return "vmName";
		} else {
			int vcpu = json.findPath("vcpu").intValue();
			if(vcpu == 0) {
				
				return "vcpu";
			} else {
				
				int memory = json.findPath("memory").intValue();
				if(memory == 0) {
					
					return "memory";
				} else {
					
					String bootType = json.findPath("bootType").textValue();
					if(bootType == null) {
						
						return "bootType";
					} 
				}
			}
		}
		return null;
	}
	
	public int createVM(JsonNode json)  
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
		{
			xml=xml.concat("<bootmenu enable='"+bootmenu+"'>");
		}
		
		xml=xml.concat("</os>"+
				"<clock offset='utc'/>"+
				"<features>     <acpi/>       <apic/>	 <hap/><pae/>	     </features>"+
				"<on_reboot>restart</on_reboot>"+
				"<on_crash>restart</on_crash>"+
				"<on_poweroff>destroy</on_poweroff>"+
				"<devices>"+        
				"<emulator>/usr/bin/kvm-spice</emulator>");
		
		//disk device floppy", "disk", "cdrom
		if(bootDev.compareTo("cdrom")==0)
		{
			//case "fd": xml=xml.concat("<disk type='file' device='floppy'>");
			//	break;
			xml=xml.concat("<disk type='file' device='cdrom'>"+
					"<source file='/media/ISO/"+json.findPath("iso").asText()+".iso'/>"+
					"<target dev='hdc'/>"+
					"<readonly/>"+
					"</disk>");
		}else if (bootDev.compareTo("hd")==0) {
			xml=xml.concat("<disk type='file' device='disk'>"+
					"<source file='/media/ISO/"+json.findPath("iso").asText()+".iso'/>"+
					"<target dev='vda' bus='virtio'/>"+
					"</disk>");
		}else if (bootDev.compareTo("network")==0) {
			xml=xml.concat("<disk type='file' device='network'>"+
					"<source file='/media/ISO/"+json.findPath("iso").asText()+".iso'/>"+
					"<target dev='hdc'/>"+
					"<readonly/>"+
					"</disk>");
		}
		
		
		/*
      case "hd": xml=xml.concat("<disk type='volume' device='disk'>"+
    		  "<driver name='qemu' type='raw'/>"+
    		  "<source pool='iscsi' volume='unit:0:0:1' />"+
    		  "<target dev='vda' bus='virtio'/>"+
       		  "</disk>");
    	  
    	  /*xml=xml.concat("<disk type='volume' device='disk'>"+
    		  "<source file='"+json.findPath("disk").asText()+"/>"+
    		  "<target dev='hda'/>"+
    	      "</disk>");*/
		/*
      break;
      case "network": xml=xml.concat("<disk type='network' device='cdrom'>"+
    		  "<source protocol='iscsi' name='iqn.2014-01.com.cmskvm:storage-server/1'>"+
    		  "<host name='192.168.43.89' port='3260'/>"+
    		  "</source>"+
    		  "<target dev='hdc'/>"+
    		  "<readonly/>"+
    		  "</disk>");
      
      break;
      case "volume": xml=xml.concat("<disk type='volume' device='disk'>"+
    		  "<source pool='aslk' volume='unit:0:0:1' mode='direct'/>"+
    		  "<target dev='vda' bus='virtio'/>"+
       		  "</disk>");
      
      break;

	default:
		break;
	}*/
		
		xml=xml.concat("<interface type='network'>"+
				"<source network='default'/>"+
				"</interface>"+
				"<input type='mouse' />"+
				"<graphics type='vnc' port='-1' autoport='yes'/>"+
				"</devices>"+
				"</domain>");
		
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
	
	public  int createStoragePool(JsonNode json)throws LibvirtException {
		String xmldesc=new String();
		String remoteHostName=json.findPath("remoteHostName").asText();
		String poolName=json.findPath("poolName").asText();
		try {
			if(conn.storagePoolLookupByName(poolName)!=null)
				return -3;
		} catch (LibvirtException e) {
			e.printStackTrace();    		
		}
		
		String storPath=json.findPath("storPath").asText();
		String poolType=json.findPath("poolType").asText();
		xmldesc=xmldesc.concat("<pool type=\""+ poolType +"\">"
				+ "<name>"+ poolName +"</name>"
				+"<source>"
				+"<host name=\""+ remoteHostName + "\"/>");
		
		if(poolType.compareTo("iscsi")==0) {
			
			xmldesc=xmldesc.concat("<device path=\""+ storPath +"\"/>"
					+"</source>"
					+"<target>"
					+"<path>/dev/disk/by-path</path>");
			
		}else if ((poolType.compareTo("netfs")==0)) {
			/*		File dir = new Fil;
    		if(!dir.mkdir())
    		{
    			System.out.println("unable to create directory");
    			return -1;
    		}
    		
			 */	xmldesc=xmldesc.concat("<dir path=\"" + storPath + "\"/>"
					 +"</source>"
					 +"<target>"
					 +"<path>/var/lib/libvirt/images</path>");
		}
		else return -2;
		xmldesc=xmldesc.concat("</target></pool>");    	
		
		StoragePool stp=conn.storagePoolDefineXML(xmldesc, 0);
		if(stp==null){    		
			return -1;
		} else {
			
			try {
				stp.create(0);
				
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				System.out.println(e.getMessage());
				stp.undefine();
				return 0;
			}
			stp.setAutostart(1);
			stp.free();
			return 1;
		}
	}    	
	
	public int deleteStoragePool(String poolName) throws LibvirtException {
		StoragePool stp=conn.storagePoolLookupByName(poolName);
		if(stp==null)
			return -1;
		
		try {
			if(stp.isActive()==1)
				stp.destroy();
			if(stp.isPersistent()==1)
			{
				stp.delete(0);
				stp.undefine();
			}
			stp.free();
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return 0;
		}
		
		return 1;
	}
	
	public ArrayList<ObjectNode> listStoragePool(int filter) throws LibvirtException {
		String [] stpListActive;
		String [] stpListInactive;
		String [] stpList=null;
		
		switch(filter) {
		case 0:
			stpList=conn.listStoragePools();
			break;
			
		case 1:
			stpList=conn.listDefinedStoragePools();
			break;
			
		case 2:
			stpListActive=conn.listStoragePools();
			stpListInactive=conn.listDefinedStoragePools();
			stpList=new String[(stpListActive.length+stpListInactive.length)];
			int index=0;
			for(int i=0;i<stpListActive.length;i++,index++) {
				stpList[index]=stpListActive[i];
			}    	   	   		
			for(int i=0;i<stpListInactive.length;i++,index++) {
				stpList[index]=stpListInactive[i];
			}
			break;
		default : return null;
		}
		StoragePool stp =null;
		StoragePoolInfo stpInfo= null;
		ArrayList<ObjectNode> jsolist = new ArrayList<ObjectNode>();
		ObjectNode jso=null;
		long temp,capacity;
		for (String stpName : stpList) {
			stp = conn.storagePoolLookupByName(stpName);				
			jso=Json.newObject();
			jso.put("Name",stpName);
			
			stpInfo=stp.getInfo();
			//				stpInfo.state
			//	stp.getInfo().allocation;
			
			capacity=stpInfo.capacity;
			jso.put("Capacity",capacity/1024/1024);
			temp=stpInfo.available;
			jso.put("Available",temp*100/capacity);
			temp=stpInfo.allocation;
			jso.put("Allocation",temp*100/capacity);
			jso.put("persistant",stp.isPersistent());
			jso.put("noOfVol",stp.numOfVolumes());				
			stp.free();
			jsolist.add(jso);
			jso=null;
		}
		return jsolist;    	
	}    
	
	public synchronized static ArrayList<Domain> staticListAllVM(int filter) throws LibvirtException, SQLException {
		Host tempHost;
		Dba db=new Dba();
		ArrayList<Domain> vmList=new ArrayList<Domain>();
		ArrayList<String> hostList=db.getHostList();
		db.close();
		for(String hostName : hostList) {
			try{
				tempHost=new Host(hostName);
				vmList.addAll(tempHost.listVM(filter));
				tempHost.close();
				tempHost=null;
			}catch(LibvirtException e){
				System.out.println(e.getMessage());
			}
		}
		return vmList;		                           
	}
	
	public  ArrayList<Domain> listVM(int filter) throws LibvirtException {
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
		//conn.close();
		return vmList;		                           
	}
	
	
	public JsonNode getRuntimeVMStatus() throws SQLException, LibvirtException {
		ObjectNode jso=null;
		Domain vm=null;
		int [] vmID=conn.listDomains();
		ArrayList<JsonNode> jsolist=new ArrayList<>();
		VMStatus vmstat=new VMStatus();
		for(int id: vmID) {
			try {
				vm=conn.domainLookupByID(id);
				jso=vmstat.getVMStatus(vm.getUUIDString());
				if(vm.isPersistent()==1)
					jso.put("Persistant","Yes");
				else
					jso.put("Persistant","No");
				jso.put("name",vm.getName());
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jsolist.add(jso);
			jso=null;
		}	
		
		return Json.toJson(jsolist);      
	}
	
	public boolean isNetName(String name) throws SQLException {
		Dba db=new Dba();
		boolean found=db.isName(name);
		db.close();
		return found; 
	}
	
	public int createNetwork(JsonNode json)  
	{
		try{
			if(isNetName(json.findPath("name").asText()))
			{
				
				return -2;
			}}
		catch(SQLException e)
		{
			System.out.println(e.getMessage());
			
		}
		String xml=new String();
		int mode=json.findPath("mode").asInt();
		int dev=json.findPath("dev").asInt();
		String hostName=json.findPath("host").asText();
		String hostIP=json.findPath("ip").asText();
		
		String name=json.findPath("name").asText();
		String bridgeName=new String();
		bridgeName="vir";
		bridgeName=bridgeName.concat(json.findPath("bridgename").asText());
		
		
		xml="<network> ";
		xml=xml.concat("<name>"+name+"</name>");   
		xml=xml.concat("	<bridge name='"+bridgeName+"' stp='on' delay='0'/>");
		/*	
     <domain name='example.com'/>
     <dns>
          <txt name='example' value='example value' />
          <forwarder addr='8.8.8.8'/>
          <forwarder addr='8.8.4.4'/>
          <srv service='name' protocol='tcp' domain='test-domain-name' target='.' port='1024' priority='10' weight='10'/>
          <host ip='192.168.122.2'>
            <hostname>myhost</hostname>
            <hostname>myhostalias</hostname>
          </host>
    </dns>
		 */
		
		xml=xml.concat("<forward ");
		/*switch(dev)
	{	
		case 1: break;
		case 2:xml=xml.concat("dev='eth0' ");break;
		case 3:xml=xml.concat("dev='wireless' "); break; 
		case 4:xml=xml.concat("dev='dial up' "); break;
		case 5:xml=xml.concat("dev='vpn' "); break;
	}	
		 */
		xml=xml.concat("mode='");
		String mode1=new String();
		mode1="Private Network";
		switch(mode)
		{	
		case 1:mode1="NAT";
		xml=xml.concat("nat' >"+
				"<nat>"+
				"<address start='1.2.3.4' end='1.2.3.10'/>"+
				"<port start='500' end='1000'/>"+
				"</nat>");
		break;
		case 2:
			mode1="Route";
			xml=xml.concat("route' >"); break; 
		case 3:
			mode1="Bridge";
			xml=xml.concat("bridge' >"); break;
		}		
		
		xml=xml.concat("	</forward>");
		
		xml=xml.concat("<ip ");
		xml=xml.concat("address='"+hostIP+"' ");
		xml=xml.concat("netmask='255.255.255.0'>");
		
		xml=xml.concat("<dhcp>"+
				"<range start='"+json.findPath("dhcpStart").asText()+"' end='"+json.findPath("dhcpEnd").asText()+"' />"+
				"</dhcp>"+	
				"</ip>");
		xml=xml.concat("</network>");
		
		String Autostart=new String();
		Autostart="False";
		
		try {
			Network net=conn.networkDefineXML(xml);
			
			
			if(json.findPath("autostart").asBoolean())
			{
				net.setAutostart(true);
				Autostart="True";
				net.create();
			}
			
			
			Dba db=new Dba();
			db.addNetwork(name,hostName,mode1,bridgeName,Autostart);
			db.close();
			return 1;
		} 
		catch (LibvirtException e) {
			System.out.println(e.getMessage());
			return -1;	
		}
		catch(SQLException e) {
			System.out.println(e.getMessage());
			return -3;
		}
		
	}
	
	
	public int startNW(String networkName, String hostName) throws LibvirtException, SQLException {
		try{
			Host tempHost=new Host(hostName);
			Network net=tempHost.conn.networkLookupByName(networkName);
			tempHost.close();
			
			net.create();				//started
			return 1;		
		}
		catch(LibvirtException e){
			System.out.println(e.getMessage());
			
			
			return 0;
		}
	}
	
	public int stopNW(String networkName, String hostName) throws LibvirtException, SQLException {
		try{
			Host tempHost=new Host(hostName);
			Network net=tempHost.conn.networkLookupByName(networkName);
			tempHost.close();
			net.destroy();
			return 1;	//("stopped");
		}
		catch(LibvirtException e){
			System.out.println(e.getMessage());
			return 0;}
	} 
	
	public int deleteNW(String NetworkName, String hostName) throws LibvirtException, SQLException {
		try{		Host tempHost=new Host(hostName);
		
		Network net=tempHost.conn.networkLookupByName(NetworkName);
		tempHost.close();
		
		if(net.isActive()>0)
			net.destroy();
		net.undefine();
		Dba db=new Dba();
		db.deleteNetwork(NetworkName);
		db.close();		
		return 1;	//("deleted");
		}
		catch(LibvirtException e){
			System.out.println(e.getMessage());
			return 0;}
	}
	
	/*
	private enum ListVMFilter {
	LIST_VM_FILTER, ACTIVE_VM_FILTER, DEFINED_VM_FILTER
	}
	 */
}
