package controllers;

import java.sql.Connection;
import java.sql.SQLException;

import javax.sql.DataSource;

import play.db.DB;
import play.mvc.*;
import scalaz.std.effect.sql.connection;

import org.libvirt.*;

import model.Host;

import com.fasterxml.jackson.databind.JsonNode;

public class VMOperation extends Controller{
	
	public static Result start(String vmName, String hostName) {
		try {
			Host tempHost=new Host(hostName);
			tempHost.conn.domainLookupByName(vmName).create();
			return ok("started");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to start");
		}    
	}
	
	public static Result shutdown(String vmName, String hostName) {
		try {
			Host tempHost=new Host(hostName);
			tempHost.conn.domainLookupByName(vmName).shutdown();
			return ok("shutdown");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to shutdown");
		}       	
	}        

	//for following op check for the flags
	public static Result reboot(String vmName,String hostName) {
		try {
			Host tempHost=new Host(hostName);
			tempHost.conn.domainLookupByName(vmName).reboot(0);
			return ok("rebooted");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to reboot");
		}
	}
	
	public static Result destroy(String vmName, String hostName) {
		try {
			Host tempHost=new Host(hostName);
			tempHost.conn.domainLookupByName(vmName).destroy();
			return ok("destroyed");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to destroy");
		}
	}        
	
	public static Result suspend(String vmName, String hostName){
		try {
			Host tempHost=new Host(hostName);
			tempHost.conn.domainLookupByName(vmName).suspend();
			return ok("suspended");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to suspend");
		}
	}
	
	public static Result resume(String vmName, String hostName){
		try {
			Host tempHost=new Host(hostName);
			tempHost.conn.domainLookupByName(vmName).resume();				
			return ok("resumed");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Opps unable to resume");
		}
	}
	
	public static Result delete(String vmName, String hostName) {
		try {
			Host tempHost=new Host(hostName);
			tempHost.conn.domainLookupByName(vmName).undefine(3);
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
	//		if()
			DataSource ds=DB.getDataSource();
			Connection dbConn=ds.getConnection();
			
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
			if(vm.revertToSnapshot(vmSnap)==0){
				return ok("Successful revert");
			}else {
				return ok("Unsuccessful revert");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Opps unable to resume");
		}
	}
}        
