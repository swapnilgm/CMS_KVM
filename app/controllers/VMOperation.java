package controllers;

import java.sql.SQLException;

import org.libvirt.LibvirtException;

import play.mvc.*;
import model.Host;
import model.VM;

public class VMOperation extends Controller{
	
	public static Result start(String vmName, String hostName) {
		VM vm;
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		try {
			vm=new VM();
			switch(vm.start(vmName, hostName)){
			case -1:
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			case 0:
				return ok("Failed to start");
			case 1:
				return ok("started");
			default: 
				return ok("ok");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();

			return internalServerError("Oops unable to send start signal");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}    
	}
	
	public static Result shutdown(String vmName, String hostName) {
		VM vm;
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		try {
			vm=new VM();
			switch(vm.shutdown(vmName, hostName)){
			case -1:
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			case 0:
				return badRequest("vm is not running.");
			case 1:
				return ok("shutdown signal sent");
			default: 
				return ok("ok");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();
		return internalServerError("Oops unable to send shutdown signal");

		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}   		   	
	}        
	
	//for following op check for the flags
	public static Result reboot(String vmName, String hostName) {
		VM vm;
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		try {
			vm=new VM();
			switch(vm.reboot(vmName, hostName)){
			case -1:
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			case 0:
				return badRequest("vm is not running.");
			case 1:
				return ok("rebooted");
			default: 
				return ok("ok");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to reboot");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity problem.");
		}   	
	}
	
	public static Result destroy(String vmName, String hostName) {
		VM vm;
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		try {
			vm=new VM();
			switch(vm.destroy(vmName, hostName)){
			case -1:
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			case 1:
				return ok("destroyed");	//force oof
			default: 
				return ok("ok");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to destroy");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}   
	}        
	
	public static Result suspend(String vmName, String hostName){
		VM vm;
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		try {
			vm=new VM();
			switch(vm.suspend(vmName, hostName)){
			case -1:
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			case 0:
				return badRequest("vm is not running.");
			case 1:
				return ok("suspended");
			default: 
				return ok("ok");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to suspend");
		}	catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}   
	}
	
	public static Result resume(String vmName, String hostName){
		VM vm;
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		try {
			vm=new VM();
			switch(vm.resume(vmName, hostName)){
			case -1:
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			case 1:
				return ok("resumed");
			default: 
				return ok("ok");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to resume");
		}	catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}   
	}
	
	public static Result delete(String vmName, String hostName) {
		VM vm;
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		try {
			vm=new VM();
			switch(vm.delete(vmName, hostName)){
			case -1:
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			case 1:
				return ok("deleted");
			default: 
				return ok("ok");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to delete");
		}catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}   
	}	
	
	public static Result save(String vmName, String hostName) {
		VM vm;
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		try {
			vm=new VM();
			switch(vm.save(vmName, hostName)){
			case -1:
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			case 1:
				return ok("saved");
			default: 
				return ok("ok");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to save domain");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}   
	}     
	
	public static Result snapshot(String vmName, String hostName){
		VM vm;
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		try {
			vm=new VM();
			switch(vm.snapshot(hostName, vmName, request().body().asJson())){
			case -1:
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			case 0:
				return ok("Opps snapshot can't be create");
			case 1:
				return ok("snapshot created");
			default: 
				return ok("ok");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();

			return internalServerError("Oops unable to create snapshot");

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}	
		
	}
	
	public static Result revert(String vmName, String hostName, String snapshot){
		VM vm;
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops libvirt error");
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database connection error");
		} 
		
		//validateHostName
		//validteVMName
		//snapshot name validation
		try {
			vm=new VM();
			switch(vm.revert(vmName, hostName, snapshot)){
			case -2:return notFound("snapshot not found");
			case -1:
				return notFound("No vm "+vmName+" found on host"+hostName+".");
			case 0:
				return ok("Unsuccessful revert");
			case 1:
				return ok("Successful revert");
			default: 
				return ok("ok");
			}
		} catch (LibvirtException e) {
			e.printStackTrace();
			return internalServerError("Oops unable to revert");
		}	catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return internalServerError("Oops database server connectivity.");
		}   
		
	}

	public static Result attachStorage(String vmName, String hostName, String poolName, String volName) {
		Host tempHost;
		try {
			if(!Host.ishostExist(hostName))
				return notFound("Host "+hostName+" not found.");
			tempHost = new Host(hostName);
			if(tempHost.validVMName(vmName)){				
				tempHost.close();		
			} else {
				return notFound("VM "+vmName+" not found.");
			}
			VM vm = new VM();
			int res=vm.attachStorage(hostName, vmName, poolName, volName);
			if(res==1)
				return created("attached");
			else if (res==-3) {
				return notFound("Pool "+poolName +" not found.");
			}else
				return badRequest();	//chk once again
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
}        

