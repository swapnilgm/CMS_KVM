package controllers;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import model.Monitor;
import play.mvc.*;
import views.html.*;
import java.util.ArrayList;

public class Application extends Controller {
	
		static ArrayList<String> hostList;
		
        public static Result index() {
        
        	hostList=Monitor.getHostList("192.168.43");	//later to add in init method for server       
        	return ok(index.render(""));
        }
        
        public static Result getHostList() {
        	
        	//ArrayList<String> hostURIList = Monitor.getHostList("192.168.43");
    		if (hostList==null) {
    			
    			hostList=Monitor.getHostList("192.168.43");	//later to add in init method for server        			
    		}
    
        	return ok(hostlist.render(hostList));   	
        
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
