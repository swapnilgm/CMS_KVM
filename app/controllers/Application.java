package controllers;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import model.Monitor;
import play.db.DB;
import play.mvc.*;
import views.html.*;

import java.io.IOException;
import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.sql.DataSource;

public class Application extends Controller {
	
		static ArrayList<String> hostList;
		static DataSource ds= DB.getDataSource();
		static Connection dbConn=null;
		static Statement stmt=null;
		
        public static Result index()  {
    /*    
        	try {
				hostList=Monitor.getHostList("192.168.43");
				return ok(index.render(""));
			}         	
        	catch (IOException | LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return TODO;
			}	//later to add in init method for server       
      */
        	return TODO;
        }
        
        public static Result getHostList()  {
        	
        	//ArrayList<String> hostURIList = Monitor.getHostList("192.168.43");
    /*		if (hostList==null) {
    			
    			try {
					hostList=Monitor.getHostList("192.168.43");
				} catch ( IOException | LibvirtException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return TODO;
				}	//later to add in init method for server        			
    		}
    		
   */     	
        	try {
				dbConn =ds.getConnection();
			  	String query="SELECT hostName FROM Host";
			  	ResultSet rs = stmt.executeQuery(query);
			  	ArrayList<String> hostList=new ArrayList<String>();
			  	while(rs.next()){
			  		hostList.add(rs.getString("hostName"));
			  	}
			  	return ok(hostlist.render(hostList));
			  	
        	} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return TODO;
			}	       	   	
        
        }
        
      
        public static Result getStaticList(String hostURI,int filter) {
        	
        	ArrayList<Domain> vmList = new ArrayList<Domain>();
           	ArrayList<Domain> tempList;
        	
        	if(hostURI.compareToIgnoreCase("all")==0) 
        	{
        		       	
        		if (hostList==null) {
        			
        			hostList=Monitor.getHostList("192.168.43");	//later to add in init method for server        			
        		}
        		
        		for(String host : hostList)
        		{        		
        			tempList=Monitor.staticListVM(host, filter);
        			if(tempList == null) {        			      			
        				continue;
        			}
        			for(Domain vm : tempList)
        			{
        				vmList.add(vm);  
        			}
        		}
        		tempList=Monitor.staticListVM("qemu:///system", filter);
    			if(tempList != null) {        			      			
    				for(Domain vm : tempList)
    				{
    					vmList.add(vm);        		
    				}
    			}
        	}
        	else {
        			
        		vmList=Monitor.staticListVM(hostURI, filter);
        		
            	if(vmList == null)
            	{
            		return ok("Unable to connect to host.");				
            	}
			}     		
        		               
            return ok(staticlist.render(vmList));
        
        }
        
        public static Result getDynamicList(String hostURI, int filter) {
                
                ArrayList<Domain> vm = Monitor.dynamicListVM(hostURI, filter);
                if(vm == null)
                	return ok("Connection Lost");
                
                
                return TODO;
        
        }
        
       
        public static Result refresh(String hostURI, int filter) throws LibvirtException {
                return redirect(routes.Application.getStaticList(hostURI,filter));
        }
    
}
