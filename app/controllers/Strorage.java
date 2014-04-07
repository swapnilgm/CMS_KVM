package controllers;

import java.sql.SQLException;

import model.Host;

import org.libvirt.LibvirtException;

import com.fasterxml.jackson.databind.JsonNode;

import play.libs.Json;
import play.mvc.*;

public class Strorage extends Controller{

	public static Result createStoragePool()
	{
		JsonNode json=request().body().asJson();
		if(json == null) {
			System.out.println("Expecting Json data");
			return badRequest("Expecting Json data");
		} else {
			String hostName = json.findPath("hostName").textValue();
			if(hostName == null) {
				System.out.println("Expecting hostname data");	
				return badRequest("Missing parameter [hostName]");
			} else {				
				String poolName = json.findPath("poolName").textValue();
				if(poolName == null) {
					System.out.println("Expecting poolName data");
					return badRequest("Missing parameter [poolName]");
				} else {
					String iqn = json.findPath("iqn").textValue();
					if(iqn == null) {
						System.out.println("Expecting Json data");
						return badRequest("Missing parameter [iqn]");
					} else {
						Host tempHost;
						try {
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
						
						try {
							if(tempHost.createStoragePool(json)==-1){
								tempHost.close();
								return internalServerError("Server error");
							}else {
								tempHost.close();
								return created();
							}
						} catch (LibvirtException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							return internalServerError("Pool create time error");
						}		
					}
				}
			}                	    
		}	    
	}
	
	public static Result deleteStoragePool(String hostName,String poolName)
	{
		try {
			Host tempHost = new Host(hostName);
			tempHost.deleteStoragePool(poolName);
			tempHost.close();
			return ok("pool deleted");
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return notFound("Cannot create Host connection");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		}
	}
	
	public static Result listStoragePool(String hostName,int filter)
	{
		try {
			Host tempHost = new Host(hostName);
			JsonNode js=Json.toJson(tempHost.listStoragePool(filter));
			tempHost.close();
			return ok(js);
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return notFound("Cannot create Host connection");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		}
	}
	
	
	public static Result listStorageVol(String hostName,String poolName)
	{
		try {
			Host tempHost = new Host(hostName);
			String[] list=tempHost.listStorageVol(poolName);
			tempHost.close();
			if(list!=null){
				JsonNode js=Json.toJson(list);
				return ok(js);
			}else {
				return notFound("No storage pool "+poolName +" on host " + hostName+ " found."); 
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return notFound("Cannot create Host connection");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		}	
	}
	
}
