package model;

import java.io.IOException;
import java.net.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;


import javax.sql.DataSource;

import org.libvirt.*;
import play.db.DB;

public class Monitor {
	
	private Connect conn;
	private String hostName;
	//private String hostIP;
	//private string hostName;
	//public static ArrayList<String> getHostList(String subnet)  {    
	public static void loadHostList(String subnet) throws SQLException  {
		
		int timeout=1000;
		
	//    ArrayList<String> hostURIList = new ArrayList<String>() ;
	    String hostIP;
	    Connect conn;
	    DataSource ds = DB.getDataSource();
    	Connection dbConn=ds.getConnection();
    	Statement stmt=dbConn.createStatement();
    	
	    for (int i=82;i<144;i++){
	    	hostIP=subnet + "." + i; 	 
	        try {
				if (InetAddress.getByName(hostIP).isReachable(timeout)){
				
						String hostURI="qemu+tcp://"+subnet + "." + i + "/system";
						ConnectAuth ca= new ConnectAuthDefault();
					    conn=new Connect(hostURI,ca,2); //connecting to hypervisor		    
				  		    
					    if (conn.isConnected()){
					    	stmt.executeUpdate("INSERT INTO host VALUE("+hostIP+conn.getHostName()+")");					    	
					   // 	hostURIList.add(conn.getHostName());	                	                        	  
					    }
					    conn.close();
				}
			} catch ( IOException | LibvirtException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	        
			        
	     }
	    stmt.close();
	    dbConn.close();
	    
		//return hostURIList;
	}
	
	
	 
	public  ArrayList<Domain> staticListVM(String hostURI, int filter) {
		ArrayList<Domain> vmList=new ArrayList<Domain>();
		try{
			if(hostURI.compareTo("qemu:///system")!=0)
			{
				hostURI="qemu+tcp://" + hostURI + "/system";
			}
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
	        
	public  ArrayList<Domain> dynamicListVM(String hypURI, int filter) throws LibvirtException {
		
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
	
	
	/*
	private enum ListVMFilter {
	LIST_VM_FILTER, ACTIVE_VM_FILTER, DEFINED_VM_FILTER
	}
	*/
}
