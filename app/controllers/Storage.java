
package controllers;

import java.sql.SQLException;
import java.util.ArrayList;

import model.Host;
import model.StorageDisk;

import org.libvirt.LibvirtException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import play.libs.Json;
import play.mvc.*;

public class Storage extends Controller{
	
	public static Result createStoragePool(String hostName)
	{
		JsonNode json=request().body().asJson();
		if(json == null) {
			System.out.println("Expecting Json data");
			return badRequest("Expecting Json data");
		} else {
			String remoteHostName = json.findPath("remoteHostName").textValue();
			if(remoteHostName == null) {
				System.out.println("Expecting hostname data");	
				return badRequest("Missing parameter [hostName]");
			} else {				
				String poolName = json.findPath("poolName").textValue();
				if(poolName == null) {
					System.out.println("Expecting poolName data");
					return badRequest("Missing parameter [poolName]");
				} else {
					String poolType = json.findPath("poolType").textValue();
					if(poolType == null) {
						System.out.println("Expecting poolType data");
						return badRequest("Missing parameter [poolType]");
					} else {
						String storPath = json.findPath("storPath").textValue();
						if(storPath == null) {
							System.out.println("Expecting Json data");
							return badRequest("Missing parameter [storPath]");
						} else {
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
							
							try {
								if(tempHost.createStoragePool(json)==-1){
									tempHost.close();
									return internalServerError("Server error");
								}else if(tempHost.createStoragePool(json)==0){
									tempHost.close();
									return badRequest("No iscsi target found.");
								} else {
									tempHost.close();
									return created("pool created.");
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
	}
	
	public static Result deleteStoragePool(String hostName,String poolName)
	{
		try {
			Host tempHost;
			try {
				if(!Host.ishostExist(hostName))
					return notFound("Host "+hostName+" not found.");
				tempHost = new Host(hostName);
				
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops cannot establish host connection");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops database connection error");
			}
			
			int poolStatus=tempHost.deleteStoragePool(poolName);
			if(poolStatus==-1)
				return notFound("No storage pool "+poolName +" on host " + hostName+ " found."); 
			else if (poolStatus==0) {
				return internalServerError("Error while deleting storage pool"+poolName +" on host " + hostName+ " found.");
			}
			tempHost.close();
			return ok("pool deleted");
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return notFound("Cannot create Host connection");
		} 
	}
	
	public static Result createStorageVol(String hostName,String poolName)
	{
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			
			if(!StorageDisk.isPoolExist(hostName,poolName)==true)
				return notFound("Pool "+poolName+" on host "+hostName+" not found.");			
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
			String volName = json.findPath("volName").textValue();
			if(volName == null) {
				System.out.println("Expecting volname data");	
				return badRequest("Missing parameter [volName]");
			} else {				
				String capacity = json.findPath("capacity").textValue();
				if(capacity == null) {
					System.out.println("Expecting capacity data");
					return badRequest("Missing parameter [capacity]");
				} else {
					StorageDisk st;
					try {
						st = new StorageDisk(hostName, poolName);
											
					int res=st.createStorageVol(json);
					st.close();
					
					if(res==-1){
						return internalServerError("Pool not found");
					}else if (res==0) {
						return ok("vol not created");
					}else{
						return created("vol created.");
					}
					} catch (LibvirtException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return internalServerError("Oops cannot establish host connection");
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return internalServerError("Oops database connection error");
					}
					
				}
			}
		}
	}                	    
	
	public static Result deleteStorageVol(String hostName,String poolName, String volName)
	{
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			
			if(!StorageDisk.isPoolExist(hostName,poolName)==true)
				return notFound("Pool "+poolName+" on host "+hostName+" not found.");			
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return notFound("Cannot create Host connection");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		StorageDisk st;
		try {
			st = new StorageDisk(hostName, poolName);			
			int res=st.deleteStorageVol(volName);
			st.close();
			
			if(res==-1){
				return internalServerError("vol not found");
			}else{
				return ok("vol deleted");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops cannot establish host connection");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		}
		
	}

	public static Result listStoragePool(String hostName,int filter)
	{
		try {
			Host tempHost;
			try {
				if(!Host.ishostExist(hostName))
					return notFound("Host "+hostName+" not found.");
				tempHost = new Host(hostName);
				
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops cannot establish host connection");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops database connection error");
			}
			ArrayList<ObjectNode> stpList = tempHost.listStoragePool(filter);
			
			response().setContentType("application/json");
			return ok(Json.toJson(stpList));
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return notFound("Cannot create Host connection");
		} 
	}
	
	
	public static Result listStorageVol(String hostName,String poolName)
	{
		
			try {
				if(!Host.ishostExist(hostName))
					return notFound("Host "+hostName+" not found.");
				if(!StorageDisk.isPoolExist(hostName,poolName)==true)
					return notFound("Pool "+poolName+" on host "+hostName+" not found.");			
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops cannot establish host connection");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops database connection error");
			}
			try {
			StorageDisk st =new StorageDisk(hostName, poolName);
			ArrayList<ObjectNode> list= st.listStorageVol();
			st.close();
			if(list!=null){
				JsonNode js=Json.toJson(list);
				return ok(js);
			}else {
				return notFound("No storage pool "+poolName +" on host " + hostName+ " found."); 
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return notFound("Error while retriving poollist");		
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
	}
}