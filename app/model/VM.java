package model;

import java.sql.SQLException;
import java.util.ArrayList;

import org.libvirt.*;

import play.libs.Json;

import model.Host;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class VM {
	Host tempHost=null;
	Domain vm=null;
	
	public int start(String vmName, String hostName) throws LibvirtException, SQLException {
		 tempHost=new Host(hostName);
		 vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		
		if(vm==null){
			return -1;		//notFound("No vm "+vmName+" found on host"+hostName+".");
		}
		if(vm.isActive()!=1)
		{
			if(vm.create()==0){
				vm.free();
				return 1;		//("started");
			}
			else {
				return 0;		//("Failed to start");
			}
		}
		else return -2;
		
	}
	
	public int shutdown(String vmName, String hostName) throws LibvirtException, SQLException {
		tempHost=new Host(hostName);
		vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;		//notFound("No vm "+vmName+" found on host"+hostName+".");
		}
		if(vm.isActive()==1){
			vm.shutdown();
			vm.free();
			return 1;		//("shutdown signal sent");
		}
		else return 0;		//("vm is not running.");       	
	}        

	//for following op check for the flags
	public int reboot(String vmName,String hostName) throws LibvirtException, SQLException {
		tempHost=new Host(hostName);
		vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");
		}
		if(vm.isActive()==1){
			vm.reboot(0);
			vm.free();
			return 1;	//("rebooted");
		}
		else return 0;	//("vm is not running.");		
	}
	
	public int destroy(String vmName, String hostName) throws LibvirtException, SQLException {
		tempHost=new Host(hostName);
		vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");		
		}
		vm.destroy();
		vm.free();
		return 1;	//("destroyed");
		
	}        
	
	public int suspend(String vmName, String hostName) throws LibvirtException, SQLException{
		tempHost=new Host(hostName);
		vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null)
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");
		if(vm.isActive()==1){
			vm.suspend();
			vm.free();
			return 1;	//("suspended");
		}
		else return 0;	//("vm is not running.");
	}
	
	public int resume(String vmName, String hostName) throws LibvirtException, SQLException{
		tempHost=new Host(hostName);
		vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");
		}
		vm.resume();
		vm.free();
		return 1;	//("resumed");
	}
	
	public int delete(String vmName, String hostName) throws LibvirtException, SQLException {
		tempHost=new Host(hostName);
		vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");
		}
		if(vm.isActive()>0)
			vm.destroy();
		vm.undefine(3);
		vm.free();
		return 1;	//("deleted");
	}
	/*
	public int save(String vmName, String hostName) throws LibvirtException, SQLException {
			String to=new String(); 
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			//need modifiaction to be done after storage
			tempHost.close();
			if(vm==null){
				return -1;	//("No vm "+vmName+" found on host"+hostName+".");
			}
			vm.save(to);
			//Database  add memory state snapshot path
			return	1;	//("saved");
	}     
	*/
	public String [] snapshotList(String vmName, String hostName) throws LibvirtException, SQLException {
		tempHost=new Host(hostName);
		vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return null;	//("No vm "+vmName+" found on host"+hostName+".");
		} else {
			String [] snapList=vm.snapshotListNames();
			vm.free();
			return snapList;
		}
	}

	public int snapshotDelete(String vmName, String hostName, String snapshot) throws LibvirtException, SQLException {
		tempHost=new Host(hostName);
		vm=tempHost.conn.domainLookupByName(vmName);
		tempHost.close();
		if(vm==null){
			return -1;	//("No vm "+vmName+" found on host"+hostName+".");
		} else {
			DomainSnapshot ds=vm.snapshotLookupByName(snapshot);
			if(ds==null) {
				vm.free();
				return -2;	//snapshot not found
			} else {
				int res=ds.delete(0);
				vm.free();
				if(res==0)
					return 1;
				else return 0;
			}
		}
	}
	
	public int snapshotCreate(String hostName,String vmName,JsonNode json) throws LibvirtException, SQLException{
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
		try {
			if(vm.snapshotLookupByName(name)!=null)
				return -2;				//snapshot already exist
		} catch (LibvirtException e) {
			//ignore no snapshot found
		}	
			
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
	
	public int attachStorage(String hostName, String vmName,  String poolName, String volName) throws SQLException, LibvirtException{
		Host tempHost=new Host(hostName);
		Domain vm=tempHost.conn.domainLookupByName(vmName);
		if(vm==null){
			tempHost.close();
			return -1;		//not found
		}
		StoragePool stp=tempHost.conn.storagePoolLookupByName(poolName);
		tempHost.close();
		if(stp==null){
			return -2;		//not found
		}
	
		
		StorageVol stv;
		try {
			stv = stp.storageVolLookupByName(volName);
		} catch (LibvirtException e) {
			stp.free();
			// TODO Auto-generated catch block
			e.printStackTrace();
			return -3;		//not found
		}
		if(stv==null){
			stp.free();
			return -3;		//not found
		}
		stv.free();
		stp.free();
		
		char dev='a';
		String xmlDesc=new String("<disk type='volume' device='disk'>"
				+"<source pool='"+ poolName + "' volume='" + volName + "'/>"
				+"<target dev='hd"+ Character.toString(dev) +"'/>" //try change hd'a' based on avalible values 
				+"</disk>");
		boolean flag=true;
		while(flag)
		{
			flag=false;			
			try{			
				vm.attachDeviceFlags(xmlDesc, 0);
			} catch (LibvirtException e) {
				if(e.getError().getCode().ordinal()==55){
					if(dev-'a' > 24 )
						return 0;
					dev++;
					System.out.println(dev + " and "+Character.toString(dev));
					xmlDesc=xmlDesc.replaceAll("<target dev='hd([a-z])","<target dev='hd"+Character.toString(dev));
					System.out.println(xmlDesc);
					flag=true;
				}
				else
					throw e;
				// TODO Auto-generated catch block
//			return -3;		//not found
			}
		}
		return 1;	//("Successful attachment");		
	}
	
	
}        
