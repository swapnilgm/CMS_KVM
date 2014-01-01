package controllers;

import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import model.Monitor;
import play.mvc.*;
import views.html.*;
import java.util.ArrayList;

public class Application extends Controller {
        public static Result index() {
                
         return ok(index.render("Home"));
        }
        
        public static Result hostList()
        {
        	ArrayList<String> hostURIList = Monitor.getHostList("192.168.43");
        	return TODO;        	
        }
        
        public static Result staticList(String hostURI,int filter) {
        	              
            ArrayList<Domain> vm = Monitor.staticListVM(hostURI, filter);
        	if(vm == null)
        		return ok("Connection Lost");
               
            return ok(staticlist.render(vm));
        
        }
        
        public static Result dynamiclist(String hostURI, int filter) {
                
                ArrayList<Domain> vm = Monitor.dynamicListVM(hostURI, filter);
                if(vm == null)
                	return ok("Connection Lost");
                
                return TODO;
        
        }
        
        public static Result refresh(String hostURI, int filter) throws LibvirtException {
                return redirect(routes.Application.staticList(hostURI,filter));
        }
    
}
