package controllers;

import org.libvirt.*;

import model.Host;
import play.mvc.*;

public class VMOperation extends Controller{
	
        public static Result create(String name, int vcpu, float ram, float mem, int boot_type, String hostname){
        	return TODO;        
        }
        
        public static Result shutdown(String vmName, String hostName) throws LibvirtException {
        	try {
        		Host tempHost=new Host(hostName);
				tempHost.conn.domainLookupByName(vmName).shutdown();
				return ok("shutdown");
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops unable to shutdown");
			}       	
        	        	
        }        
      
//for following op check for the flags

        public static Result destroy(String vmName, String hostName) {
        	try {
        		Host tempHost=new Host(hostName);
				tempHost.conn.domainLookupByName(vmName).destroy();
				return ok("destroyed");
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops unable to destroy");
			}       	
        	
        }        
         
        public static Result start(String vmName, String hostName) {
        	try {
        		Host tempHost=new Host(hostName);
				tempHost.conn.domainLookupByName(vmName).create();
				
				//add flags
				return ok("started");
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops unable to start");
			}       	
        	
        }
        
       public static Result reboot(String vmName,String hostName) {
        	
        	try {
        		Host tempHost=new Host(hostName);
				tempHost.conn.domainLookupByName(vmName).reboot(0);
				return ok("rebooted");
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops unable to reboot");
			}       	
        	
        }
        
        public static Result suspend(String vmName, String hostName){
        	try {
        		Host tempHost=new Host(hostName);
				tempHost.conn.domainLookupByName(vmName).suspend();
				return ok("suspended");
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Oops unable to suspend");
			}
        	
        }
        
        /*/public static Result snapshot(String vmName, String hostName){
        	try {
        		Host tempHost=new Host(hostName);
				tempHost.conn.domainLookupByName(vmName).save(;
				return ok("");
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return TODO;
			}
        	return TODO;
        }/*/
        
        public static Result resume(String vmName, String hostName){
        	try {
        		Host tempHost=new Host(hostName);
				tempHost.conn.domainLookupByName(vmName).resume();				
				return ok("resumed");
			} catch (LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return internalServerError("Opps unable to resume");
			}
        	
        }                     
}        
 