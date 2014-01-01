package model;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import org.libvirt.Connect;
import org.libvirt.Domain;
import org.libvirt.LibvirtException;

public class Monitor {
	
	static Connect conn;
	        
	public static ArrayList<String> getHostList(String subnet)  {
		int timeout=1000;
	    ArrayList<String> hostURIList = new ArrayList<String>() ;
	    String hostName;
	    
	    for (int i=1;i<254;i++){
	    	hostName=subnet + "." + i; 	 
	        try {
				if (InetAddress.getByName(hostName).isReachable(timeout)){
					
					hostName="qemu+tcp://"+subnet + "." + i + "/system";
					
				    conn=new Connect(hostName,true); //connecting to hypervisor
				    if (conn.isConnected()){
				    	hostURIList.add(hostName);	                	                        	  
				    }
				    conn.close();
				}
			} catch (IOException | LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
		return hostURIList;
	}        
	 
	public static ArrayList<Domain> staticListVM(String hostURI, int filter) {
	
		try{
			conn=new Connect(hostURI,true); //connecting to hypervisor
	        ArrayList<Domain> vmList=new ArrayList<Domain>();
	                       
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
