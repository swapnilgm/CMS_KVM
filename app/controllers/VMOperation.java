package controllers;

import java.sql.Connection;
import java.sql.SQLException;

import play.db.DB;
import play.mvc.*;


import org.libvirt.*;

import model.Host;

import com.fasterxml.jackson.databind.JsonNode;

public class VMOperation extends Controller{
	
	public static Result start(String vmName, String hostName) {
		try {
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			if(vm==null){
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			}
			if(vm.create()==0){
				return ok("started");
			}
			else {
				return ok("Failed to start");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to start");
		}    
	}
	
	public static Result shutdown(String vmName, String hostName) {
		try {
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			if(vm==null){
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			}
			if(vm.isActive()==1){
				vm.shutdown();
				return ok("shutdown signal sent");
			}
			else return badRequest("vm is not running.");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to shutdown");
		}       	
	}        

	//for following op check for the flags
	public static Result reboot(String vmName,String hostName) {
		try {
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			if(vm==null){
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			}
			if(vm.isActive()==1){
				vm.reboot(0);
				return ok("rebooted");
			}
			else return badRequest("vm is not running.");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to reboot");
		}
	}
	
	public static Result destroy(String vmName, String hostName) {
		try {
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			if(vm==null){
				return notFound("No vm "+vmName+" found on host"+hostName+".");		
			}
			vm.destroy();
			return ok("destroyed");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to destroy");
		}
	}        
	
	public static Result suspend(String vmName, String hostName){
		try {
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			if(vm==null)
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			if(vm.isActive()==1){
				vm.suspend();
				return ok("suspended");
			}
			else return badRequest("vm is not running.");
			
		} catch (LibvirtException e) {
			return internalServerError(e.getMessage());
		}
	}
	
	public static Result resume(String vmName, String hostName){
		try {
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			if(vm==null){
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			}
			vm.resume();				
			return ok("resumed");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Opps unable to resume");
		}
	}
	
	public static Result delete(String vmName, String hostName) {
		try {
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			if(vm==null){
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			}
			vm.undefine(3);
			return ok("deleted");
			
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to Delete");
		}
	}
	
	public static Result save(String vmName, String hostName) {
		try {
			String to=new String(); 
			Host tempHost=new Host(hostName);
			
			tempHost.conn.domainLookupByName(vmName).save(to);
			Connection dbConn=DB.getConnection();
			dbConn.close();
			return ok("saved");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to save domain");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity error");
		}
	}     
	
	public static Result snapshot(String vmName, String hostName){
		try {
			String xmlDesc=new String("<domainsnapshot>");
			JsonNode json=request().body().asJson();
       	    String name = json.findPath("Name").textValue();
       	    if(name != null) {
       	    	xmlDesc=xmlDesc.concat("<name>"+name+"</name>");
       	    } 
       	    String description = json.findPath("Description").textValue();
    	    if(description != null) {
    	    	xmlDesc=xmlDesc.concat("<name>"+description+"</name>");
    	    } 
    	    xmlDesc=xmlDesc.concat("</domainsnapshot>");
    	    Host tempHost=new Host(hostName);
    	    DomainSnapshot domSnap=tempHost.conn.domainLookupByName(vmName).snapshotCreateXML(xmlDesc);
    	    if(domSnap!=null){
    	    	return ok("snapshot created");
    	    }
    	    else {
    	    	return ok("Opps unable to create snapshot");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Opps connection error");
		}
	}
	
	public static Result revert(String vmName, String hostName, String snapshot){
		try {
			Host tempHost=new Host(hostName);
			Domain vm=tempHost.conn.domainLookupByName(vmName);
			DomainSnapshot vmSnap=vm.snapshotLookupByName(snapshot);
			if(vmSnap==null)
				return notFound("Sanpshot "+snapshot+"not found.");
			if(vm.revertToSnapshot(vmSnap)==0){
				return ok("Successful revert");
			}else {
				return ok("Unsuccessful revert");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Opps unable to revert");
		}
	}
}        
