package controllers;

import play.mvc.*;

public class VM extends Controller{
	
        public static Result create(String name, int vcpu, float ram, float mem, int boot_type, String hostname){
        	return TODO;        
        }
        
        public static Result shutdown(String vmName, String hostName) {
        	return TODO;
        }        
      
//for following op check for the flags

        public static Result destroy(String vmName, String hostName) {
        		return TODO;
        }        
         
        public static Result start(String vmName, String hostName) {
        	return TODO;
        }
        
        public static Result reboot(String vmName,String hostName) {
        	return TODO;
        }
        
        public static Result suspend(String vmName, String hostName){
        	return TODO;
        }
        
        public static Result resume(String vmName, String hostName){
        	return TODO;
        }                     
}        
 