package model;

import java.sql.SQLException;

import org.libvirt.*;

import model.Host;

import com.fasterxml.jackson.databind.JsonNode;

public class VM {
	
	public int start(String vmName, String hostName) throws LibvirtException, SQLException {
		Host tempHost=new Host(hostName);
		Domain vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;		//notFound("No vm "+vmName+" found on host"+hostName+".");
		}
		if(vm.create()==0){
			return 1;		//("started");
		}
		else {
			return 0;		//("Failed to start");
		}
	}
	
	public int shutdown(String vmName, String hostName) throws LibvirtException, SQLException {
		Host tempHost=new Host(hostName);
		Domain vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;		//notFound("No vm "+vmName+" found on host"+hostName+".");
		}
		if(vm.isActive()==1){
			vm.shutdown();
			return 1;		//("shutdown signal sent");
		}
		else return 0;		//("vm is not running.");       	
	}        

	//for following op check for the flags
	public int reboot(String vmName,String hostName) throws LibvirtException, SQLException {
		Host tempHost=new Host(hostName);
		Domain vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");
		}
		if(vm.isActive()==1){
			vm.reboot(0);
			return 1;	//("rebooted");
		}
		else return 0;	//("vm is not running.");		
	}
	
	public int destroy(String vmName, String hostName) throws LibvirtException, SQLException {
		Host tempHost=new Host(hostName);
		Domain vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");		
		}
		vm.destroy();
		return 1;	//("destroyed");
		
	}        
	
	public int suspend(String vmName, String hostName) throws LibvirtException, SQLException{
		Host tempHost=new Host(hostName);
		Domain vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null)
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");
		if(vm.isActive()==1){
			vm.suspend();
			return 1;	//("suspended");
		}
		else return 0;	//("vm is not running.");
	}
	
	public int resume(String vmName, String hostName) throws LibvirtException, SQLException{
		Host tempHost=new Host(hostName);
		Domain vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");
		}
		vm.resume();				
		return 1;	//("resumed");
	}
	
	public int delete(String vmName, String hostName) throws LibvirtException, SQLException {
		Host tempHost=new Host(hostName);
		Domain vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");
		}
		vm.destroy();
		vm.undefine(3);
		return 1;	//("deleted");
	}
	
	public int save(String vmName, String hostName) throws LibvirtException, SQLException {
			String to=new String(); 
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			tempHost.close();
			if(vm==null){
				return -1;	//("No vm "+vmName+" found on host"+hostName+".");
			}
			vm.save(to);
			//Database  add memory state snapshot path
			return	1;	//("saved");
	}     
	
	public int snapshot(String hostName,String vmName,JsonNode json) throws LibvirtException, SQLException{
		String xmlDesc=new String("<domainsnapshot>");
		String name = json.findPath("Name").textValue();
		if(name != null) {
			//validateSnapshotName(name);
			xmlDesc=xmlDesc.concat("<name>"+name+"</name>");
		} 
		String description = json.findPath("Description").textValue();
		if(description != null) {
			xmlDesc=xmlDesc.concat("<description>"+description+"</description>");
		} 
		xmlDesc=xmlDesc.concat("</domainsnapshot>");
		Host tempHost=new Host(hostName);
		Domain vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null)
			return -1;	//vm not found
		DomainSnapshot vmSnap=vm.snapshotCreateXML(xmlDesc);
		if(vmSnap!=null){
			vmSnap.free();
			return	1;		//("snapshot created");
		}else {
			return 0;	//("Opps unable to create snapshot");
		}
	}
	
	public int revert(String vmName, String hostName, String snapshot) throws LibvirtException, SQLException{
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			tempHost.close();
			if(vm==null){
				return -1;		//not found
			}
			DomainSnapshot vmSnap=vm.snapshotLookupByName(snapshot);
			if(vmSnap==null)
				return -2;	//notFound("Sanpshot "+snapshot+"not found.");
			if(vm.revertToSnapshot(vmSnap)==0){
				return 1;	//("Successful revert");
			}else {
				return 0;	//("Unsuccessful revert");
			}
		
	}
}        
