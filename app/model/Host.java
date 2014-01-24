package model;
	
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
	
	public Host(String hostName) throws LibvirtException, SQLException
    {
		String hostIP=null;
		Dba d=new Dba();
		hostIP=d.getIP(hostName);
		d.close();
      ConnectAuth ca= new ConnectAuthDefault();
      System.out.println("Connecting to qemu+tcp://" + hostIP + "/system ...");
      conn=new Connect("qemu+tcp://" + hostIP + "/system",ca,0); //connecting to hypervisor 
    }
	
	public void close() throws LibvirtException
	{
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
	
	
	/*
	private enum ListVMFilter {
	LIST_VM_FILTER, ACTIVE_VM_FILTER, DEFINED_VM_FILTER
	}
	*/
}
