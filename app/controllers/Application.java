package controllers;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import model.Host;
import play.db.DB;
import play.libs.Json;
import play.mvc.*;
import views.html.*;

import java.sql.*;
import java.util.ArrayList;

import javax.sql.DataSource;

public class Application extends Controller {
	
		static ArrayList<String> hostList;
		static DataSource ds= DB.getDataSource();
		static Connection dbConn=null;
		static Statement stmt=null;
		
        public static Result index()  {
        	return ok(index.render(""));
        }
        
        public static Result getHostList()  {
        	try{
        		       	
        		ArrayList<String> hostList = Host.getHostList();
        		JsonNode json=Json.toJson(hostList);        		
        		//response().setContentType("application/json");
       	  		return ok(json);		  	
			  	
        	} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops database connection error");
			}	 
        }
        
      
        public static Result getStaticList(String hostName,int filter) {
        	
        	ArrayList<Domain> vmList;
        	
        	try {      	
        		if(hostName.compareToIgnoreCase("all")==0) 
        		{	        	     	
        			vmList=Host.staticListAllVM(filter);
        		}
        		else {
        		
        			Host tempHost=new Host(hostName);
        			vmList=tempHost.staticListVM(filter);
        		}
        		ArrayList<ObjectNode> jsolist = new ArrayList<ObjectNode>();
        		ObjectNode jso=null;jso=Json.newObject();
        		
        		for (Domain vm : vmList){
        			jso=Json.newObject();
        			jso.put("ID",vm.getID());
        			jso.put("Name",vm.getName());
        		//	jso.put("Host",vm.getConnect().getHostName());
        			jso.put("NoOfCPU",vm.getInfo().nrVirtCpu);
        			jso.put("MaxMemory",vm.getMaxMemory());
        			
        			jsolist.add(jso);
        			jso=null;
        		}
        		
        		return ok(Json.toJson(jsolist));		
        	} catch (LibvirtException | SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops getstaitc list");
			}	            
        }
       
        
        public static Result validateVMName(String hostName,String vmName) {
          	try {
            		Host tempHost=new Host(hostName);
    				if(tempHost.conn.domainLookupByName(vmName)!=null);
    				return ok();
    			} catch (LibvirtException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				return internalServerError("Oops, Connection to Host is lost");
    			}       	
            }        
        public static Result createVM(String hostName){
        	Host tempHost;
			try {
				tempHost = new Host(hostName);
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return notFound("Cannot create Host connection");
			} 
        	JsonNode json=request().body().asJson();
        	if(json == null) {
        		  return badRequest("Expecting Json data");
        	  } else {
        	    String vmName = json.findPath("vmName").textValue();
        	    if(vmName == null) {
        	    	
        	      return badRequest("Missing parameter [vmName]");
        	    } else {
        	    	int vcpu = json.findPath("vcpu").intValue();
        	    	if(vcpu == 0) {
        	    		
        	    		return badRequest("Missing parameter [vcpu]");
            	    } else {
            	    	
            	    	int memory = json.findPath("memory").intValue();
                	    if(memory == 0) {
                	    
                	      return badRequest("Missing parameter [memory]");
                	    } else {
                	
                	    	String bootType = json.findPath("bootType").textValue();
                    	    if(bootType == null) {
                    	
                    	      return badRequest("Missing parameter [bootType]");
                    	    } else {
                    	    	
                    	    	if(tempHost.create(json)==-1){
                    
                    	    		return internalServerError("Server error");
                    	    	}else if (tempHost.create(json)==0){
                    
                    	    		return badRequest("Cannot create vm");
                    	    	}else {
									return ok("vm created");
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
						tempHost = new Host(hostName);
					
						JsonNode js=Json.toJson(tempHost.conn.nodeInfo());
						return ok(js);		
					} catch (LibvirtException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return notFound("Oops, Connection to Host is lost");
					} 
            		  		
          	}
        
        public static Result getDynamicList(String hostURI, int filter) {
                
        //        ArrayList<Domain> vm = new Host(hostURI).dynamicListVM(filter);
          //      if(vm == null)
            //    	return ok("Connection Lost");
                
                
                return TODO;
        
        }
        
       
        public static Result refresh(String hostURI, int filter) throws LibvirtException {
                return redirect(routes.Application.getStaticList(hostURI,filter));
        }
        
}
