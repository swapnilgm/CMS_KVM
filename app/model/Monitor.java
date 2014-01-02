package model;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import org.libvirt.Connect;
import org.libvirt.ConnectAuth;
import org.libvirt.ConnectAuthDefault;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;
import org.libvirt.ConnectAuth.Credential;

public class Monitor {
	
	static Connect conn;
	        
	public static ArrayList<String> getHostList(String subnet)  {
		int timeout=1000;
	    ArrayList<String> hostURIList = new ArrayList<String>() ;
	    String hostName;
	    

	    for (int i=82;i<144;i++){
	    	hostName=subnet + "." + i; 	 
	        try {
				if (InetAddress.getByName(hostName).isReachable(timeout)){
					
					hostName="qemu+tcp://"+subnet + "." + i + "/system";
					ConnectAuth ca= new ConnectAuthDefault();
				    conn=new Connect(hostName,ca,0); //connecting to hypervisor		    
			  		    
				    if (conn.isConnected()){
				    	hostURIList.add(conn.getHostName());	                	                        	  
				    }
				    conn.close();
				}
			
			}
	        
	        catch (IOException | LibvirtException e) {
				e.printStackTrace();

			}
	    }
	    
		return hostURIList;
	}        
	 
	public static ArrayList<Domain> staticListVM(String hostURI, int filter) {
		ArrayList<Domain> vmList=new ArrayList<Domain>();
		try{
			ConnectAuth ca = new ConnectAuthDefault();
			conn=new Connect(hostURI,ca,0); //connecting to hypervisor	        
	                       
	        if(filter == 1) {
	        	int[] activeVMs = conn.listDomains();
	            for(int i = 0; i < conn.numOfDomains(); i++) 
	            	vmList.add(conn.domainLookupByID(activeVMs[i]));	               
	        }
	                       
	        else if(filter == 2) {
	        	int[] activeVMs = conn.listDomains();
	            for(int i = 0; i < conn.numOfDomains(); i++)
	            {
	            	vmList.add(conn.domainLookupByID(activeVMs[i]));
	            }
	                            
	            String[] defVMList=conn.listDefinedDomains();
	            for(int i = 0; i < conn.numOfDefinedDomains(); i++)
	            {
	            	vmList.add(conn.domainLookupByName(defVMList[i]));
	            }                                
	       }
	                   
	       else if(filter == 0){
	            String[] defVMList=conn.listDefinedDomains();
	            for(int i = 0; i < conn.numOfDefinedDomains(); i++)
	            {
	            	vmList.add(conn.domainLookupByName(defVMList[i]));
	            }                        
	       }        
	                        
	       conn.close();
	       return vmList;
		}
	                
	    catch(LibvirtException le){
	    	
	    	System.out.println(le.getMessage());	                       
	    }
	    return null;                           
	}
	        
	public static ArrayList<Domain> dynamicListVM(String hypURI, int filter) {
		try{
			
	        conn=new Connect(hypURI,true); //connecting to hypervisor
	        ArrayList<Domain> vmList=new ArrayList<Domain>();
	                       
	        if(filter == 1) {
	        	int[] activeVMs = conn.listDomains();
	            for(int i = 0; i < conn.numOfDomains(); i++)
	            {
	            	vmList.add(conn.domainLookupByID(activeVMs[i]));
	            }                                
	        }                        
	                                
	        conn.close();
	        return vmList;
        }
	                
        catch(LibvirtException le){
        	System.out.println(le.getMessage());              
        }
	    
		return null;   
	                       
	}
	
	/*
	private enum ListVMFilter {
	LIST_VM_FILTER, ACTIVE_VM_FILTER, DEFINED_VM_FILTER
	}
	*/
}
