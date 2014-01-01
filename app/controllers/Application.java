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
        	return ok(hostlist.render(hostList));
        }
        
        public static Result getHostList() {
        	
        	//ArrayList<String> hostURIList = Monitor.getHostList("192.168.43");
        	return ok(hostlist.render(hostList));   	
        
        }
        
        public static Result getStaticListALL(int filter) {
        	ArrayList<Domain> allVMList = new ArrayList<Domain>();
        	ArrayList<Domain> tempVMList;
        	if (hostList==null)
        		return ok("connection lost all");
        	for(String hostURI : hostList)
        	{	
        		
        		tempVMList=Monitor.staticListVM(hostURI, filter);
        		if(tempVMList == null)
        			continue;
        		for(Domain vm : tempVMList)
        			allVMList.add(vm);
        		
        	}  
            return ok(staticlist.render(allVMList));
        
        }
        public static Result getStaticList(String hostURI,int filter) {
        	              
            ArrayList<Domain> vm = Monitor.staticListVM(hostURI, filter);
        	if(vm == null)
        		return ok("Connection Lost");
               
            return ok(staticlist.render(vm));
        
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
