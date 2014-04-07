package controllers;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import dbal.Dba;
import model.Host;
import play.libs.Json;
import play.mvc.*;
import views.html.*;

import java.sql.*;
import java.util.ArrayList;


public class Application extends Controller {
	
	public static Result index()  {
		return ok(index.render(""));
	}
	
	public static Result getHostList()  {
		try{
			Dba db=new Dba();
			ArrayList<String> hostList = db.getHostList();
			db.close();
			JsonNode json=Json.toJson(hostList);        		
			response().setContentType("application/json");
			return ok(json);		  	
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		}	 
	}
	
	
	public static Result getStaticList(String hostName,int filter) {
		try {      	
			ArrayList<Domain> vmList;
			if(hostName.compareToIgnoreCase("all")==0) 
			{	        	     	
				vmList=Host.staticListAllVM(filter);
			}
			else {
				if(!Host.ishostExist(hostName))
					return notFound("Host "+hostName+" not found.");
				Host tempHost=new Host(hostName);
				vmList=tempHost.listVM(filter);
				tempHost.close();
			}
			ArrayList<ObjectNode> jsolist = new ArrayList<ObjectNode>();
			ObjectNode jso=Json.newObject();
			
			for (Domain vm : vmList) {
				jso=Json.newObject();
				jso.put("Name",vm.getName());
				if(vm.isActive()==1)
					jso.put("Status","Active");
				else jso.put("Status","InActive");
				//jso.put("Host",vm.getConnect().getHostName());
				jso.put("NoOfCPU",vm.getInfo().nrVirtCpu);
				jso.put("MaxMemory",vm.getMaxMemory());
				
				jsolist.add(jso);
				jso=null;
			}
			response().setContentType("application/json");
			return ok(Json.toJson(jsolist));		
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops unable to connect to host");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		}	            
	}
	
	
	public static Result validateVMName(String hostName,String vmName) {
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			Host tempHost=new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();
				return ok("vm name verified");
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops, Connection to Host is lost");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		}       	
	}        
	
	public static Result createVM(String hostName){
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return notFound("Cannot create Host connection");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		JsonNode json=request().body().asJson();
		if(json == null) {
			System.out.println("Expecting Json data");
			return badRequest("Expecting Json data");
		} else {
			String vmName = json.findPath("vmName").textValue();
			if(vmName == null) {
				System.out.println("Expecting vmname data");	
				return badRequest("Missing parameter [vmName]");
			} else {
				int vcpu = json.findPath("vcpu").intValue();
				if(vcpu == 0) {
					System.out.println("Expecting vcpu data");
					return badRequest("Missing parameter [vcpu]");
				} else {
					int memory = json.findPath("memory").intValue();
					if(memory == 0) {
						System.out.println("Expecting memorydata");
						return badRequest("Missing parameter [memory]");
					} else {
						String bootType = json.findPath("bootType").textValue();
						if(bootType == null) {
							System.out.println("Expecting Json data");
							return badRequest("Missing parameter [bootType]");
						} else {
							
							if(tempHost.createVM(json)==-1){
								
								return internalServerError("Server error");
							}else if (tempHost.createVM(json)==0){
								
								return badRequest("Cannot create vm");
							}else {
								return created("vm created");
							}
							
						}
					}
				}                	    
			}	    
		} 	    
	}
	
	public static Result hostInfo(String hostName) {
		
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			JsonNode js=tempHost.getHostInfo();
			tempHost.close();
			response().setContentType("application/json");
			return ok(js);		
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
	}
	
	public static Result getDynamicList(String hostName) {	
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			JsonNode js=tempHost.getRuntimeVMStatus();
			tempHost.close();
			response().setContentType("application/json");
			return ok(js);		
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return notFound("Oops, Connection to Host is lost");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Database connection error : "+ e.getMessage());
		}		
	}
		
	public static Result refresh(String hostURI, int filter) throws LibvirtException {
		return redirect(routes.Application.getStaticList(hostURI,filter));
	}
	
}
