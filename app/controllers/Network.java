package controllers;

import play.libs.Json;
import java.sql.*;
import java.util.ArrayList;

import org.libvirt.*;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import dbal.Dba;
import model.Host;
import play.mvc.*;

public class Network extends Controller {
public static Result listNW(){
    	//list networks
	try{    	
		
		ArrayList<ObjectNode> List=new ArrayList<ObjectNode>();
			
		Dba db=new Dba();
		
		List=db.getNetList();
		
		db.close();
	

			JsonNode js=Json.toJson(List);
			response().setContentType("application/json");
    	return ok(js);	
	}
	 catch (SQLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
}

	
    public static Result createNW(){
    	//create a network from an XML file
		JsonNode json=request().body().asJson();
		Host tempHost;
		try {
			tempHost = new Host(json.findPath("host").asText());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("\nOops database connection error\n");
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("\nLibvirt Error\n");
		} 
		
		if(json == null) {
			System.out.println("Expecting Json data");
			return badRequest("Expecting Json data");
		} 
		else {
										
			switch(tempHost.createNetwork(json))
			{	
				case -2:	return badRequest("\nName already Exists\n");
				case 1:		return created();
				case -1:	return internalServerError("\nServer error\n"); 
				case -3:	return internalServerError("\nDatabase error\n");
				default:	return internalServerError("\nunknown error\n"); 		 
			}
			       	
    		}
}
    
    public static Result stopNW(String netName, String hostName){
    	//destroy (stop) a network
		
		try {
				Host tempHost=new Host(hostName);
						
			switch(tempHost.stopNW(netName,hostName)){
			
			case 0:
			tempHost.close();
				return ok("Failed to stop");
			case 1:
			tempHost.close();
				return ok("stopped");
			default: 
			tempHost.close();
				return ok("ok");
			}
			
		} catch (LibvirtException e1) {
			e1.printStackTrace();
			return internalServerError("Oops unable to delete");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}
    				
   
    	
}
  
    
    
    
    public static Result startNW(String netName, String hostName){
    	//start a (previously defined) inactive network
    
    
		try {
		Host tempHost=new Host(hostName);
			switch(tempHost.startNW(netName,hostName)){
			
			case 0:
			tempHost.close();
				return ok("Failed to start");
			case 1:
			tempHost.close();
				return ok("started");
			default: 
			tempHost.close();
				return ok("ok");
			}
			
		} catch (LibvirtException e1) {
			e1.printStackTrace();
			return internalServerError("Oops unable to delete");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}
}
    
public static Result deleteNW(String netName, String hostName){
    	//undefine an inactive network

    	try {
		
		Host tempHost=new Host(hostName);
			switch(tempHost.deleteNW(netName,hostName)){
			
			case 0:
				tempHost.close();
				return ok("Failed to delete");
			case 1:
				tempHost.close();
				return ok("deleted");
			default: 
				tempHost.close();
	
				return ok("ok");
			}
	
		} catch (LibvirtException e1) {
			e1.printStackTrace();
			return internalServerError("Oops unable to delete");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}
    
    }
}