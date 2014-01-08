package model;

import java.io.IOException;
import java.net.InetAddress;
import java.util.ArrayList;
import org.libvirt.*;
import java.io.*;
import java.sql.*;

public class VMOperations {

		private String hostname;
        private Connect conn;
        private Domain dom;
        
        public void connect_hyper(String hostName)
        {
        //	String hostIP;
        // 	#sql {select hostIPaddr into :hostIP from host where hostname =  :hostname };
          ConnectAuth ca= new ConnectAuthDefault();
          conn=new Connect("qemu+tcp://" + hostName + "/system",ca,0); //connecting to hypervisor 
        }
        
        public void create(String name, int vcpu, float ram, float mem, int boot_type, String hostname)  
        {
        		String boot_dev;
            try{ 
            switch(boot_type)
            {
            	case 0:	boot_dev="hd"; break;
            	case 1: boot_dev="fd"; break;
            	case 2: boot_dev="cdrom"; break;
            	case 3: boot_dev="network"; 
            
            }    
          //  OutputStream f = new FileOutputStream("/home/sumit/vm.xml") throws IOException{}
            String xml = "<domain type='kvm'>\n	<name>" + name + "</name>\n  <memory unit='KiB'>" + 1024*1024*ram + "</memory>\n  <vcpu placement='static'>" + vcpu + "</vcpu>\n  <os> \n    <type>hvm</type>\n    <boot dev='" + boot_dev + "'/>\n  </os>\n <on_poweroff>destroy</on_poweroff>\n  <on_reboot>restart</on_reboot>\n  <on_crash>restart</on_crash>\n</domain>";		//check if anything else is needed
            
          //  byte buf[] = xml.getbytes();
          //  f.write(buf);
          //  f.close();
           
         		 
            connect_hyper(hostname);
            conn.create(xml,0);
            #sql {insert into dom values(:domname,:hostname)};
            conn.close();
            }
            catch (IOException | LibvirtException e) {
                                e.printStackTrace();
                        }
            
            
        }
        
        public int shutdown(String domname)
        {
        	#sql {select hostname into :hostname from dom where domname = :domname};
        	connect_hyper(hostname);
        	int a = conn.DomainLookupByName(domname).DomainShutdown();
        	conn.close();
        	return a;
        }        
      
//for following op check for the flags

        public int destroy(String domname)
        {
        	String hostname;
        	#sql {select hostname into :hostname from dom where domname = :domname};
        	connect_hyper(hostname);
        	int a = conn.DomainLookupByName(domname).DomainDestroy();
        	int b = conn.DomainLookupByName(domname).DomainUndefine();
        	conn.close();
        	return a||b;
        }        
         
        public int start(String domname)
        {
        	#sql {select hostname into :hostname from dom where domname = :domname};
        	connect_hyper(hostname);
        	int a = conn.DomainLookupByName(domname).DomainStart;
        	conn.close();
        	return a;
        }
        
        public int reboot(String domname)
        {
        	#sql {select hostname into :hostname from dom where domname = :domname};
        	connect_hyper(hostname);
        	int a = conn.DomainLookupByName(domname).DomainReboot(0);
        	conn.close();
        	return a;
        }
        
        public int suspend(String domname)
        {
        	#sql {select hostname into :hostname from dom where domname = :domname};
        	connect_hyper(hostname);
        	int a = conn.DomainLookupByName(domname).DomainSuspend();
        	conn.close();
        	return a;
        }
        
        public int resume(String domname)
        {
        	#sql {select hostname into :hostname from dom where domname = :domname};
        	connect_hyper(hostname);
        	int a = conn.DomainLookupByName(domname).DomainResume();
        	conn.close();
        	return a;
        }                     
}        
