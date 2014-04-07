package model;

import java.sql.SQLException;
import java.util.ArrayList;

import javax.xml.xpath.XPath;

import org.libvirt.LibvirtException;
import org.libvirt.StoragePool;
import org.libvirt.StorageVol;
import org.libvirt.StorageVolInfo;

import play.libs.Json;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class StorageDisk {
	Host tempHost;
	StoragePool stp;
	StorageVol stv;
	
	public StorageDisk(String hostName, String poolName) throws LibvirtException, SQLException{
		Host tempHost = new Host(hostName);
		stp=tempHost.conn.storagePoolLookupByName(poolName);
	}
	
	public void close() throws LibvirtException {
		if(tempHost!=null)
			tempHost.close();
		if(stp!=null)
		stp.free();		
		if(stv!=null)
			stv.free();
	}
	
	static public boolean isPoolExist(String hostName, String poolName)
			throws LibvirtException, SQLException {
		Host tempHost = new Host(hostName);
		if(tempHost.conn.storagePoolLookupByName(poolName)==null)
			return false;
		else
			return true;
	}
	
	public int createStorageVol (JsonNode json) throws LibvirtException {
		String xmlDesc=new String();
		String volName=json.findPath("volName").asText();
		int capacity=json.findPath("capacity").asInt();
		try {
			stv=stp.storageVolLookupByName(volName);
		} catch (LibvirtException e) {
			// TODO Auto-generated catch block
			System.out.println(e.getMessage());			
		}
		if(stv!=null){
			return -3;
		} else{
			//String storPath=json.findPath("capacity").asText();
			String poolXML=stp.getXMLDesc(1);
			//System.out.println(poolXML);
			//xml parsing to get storage pool path
			int startIndex=poolXML.indexOf("type='");
			System.out.println("si="+startIndex);
			//			<pool type='netfs'>
			
			int lastIndex=poolXML.indexOf("'>",startIndex+6);
			//System.out.println("li="+lastIndex);
			String path=poolXML.substring(startIndex+6, lastIndex);
			//System.out.println("path="+path);
			
			//check type iscsi or netfs
			if(path.compareToIgnoreCase("iscsi")==0)
				return -2;
			
			startIndex=poolXML.indexOf("<target>");
			//System.out.println("si="+startIndex);
			startIndex=poolXML.indexOf("<path>",startIndex);
			//System.out.println("si="+startIndex);
			lastIndex=poolXML.indexOf("</path>",startIndex+6);
			//System.out.println("li="+lastIndex);
			path=poolXML.substring(startIndex+6, lastIndex);
			//	System.out.println("path="+path);
			xmlDesc=xmlDesc.concat("<volume>"
					+ "<name>" +volName+".img" +"</name>"
					+"<allocation>0</allocation>"
					+"<capacity unit=\"K\">" + capacity + "</capacity>"
					+"<target>"
					+"<path>" + path + volName + ".img" + "</path>"
					+"<permissions>"
					+"<owner>107</owner>"
					+"<group>107</group>"
					+"<mode>0744</mode>"
					+"<label>virt_image_t</label>"
					+"</permissions>"
					+"</target>"
					+"</volume>");
			
			if(stp==null){    		
				return -1;    		
			} else {
				//check type iscsi or netfs
				stv=stp.storageVolCreateXML(xmlDesc, 0);
				if(stv==null){
					return 0;
				} else{
					stv.free();
					return 1;
				}
			} 
		}
	}
	
	
	public int deleteStorageVol(String volName) throws LibvirtException {
		
		if(stp==null){    		
			return -2;
		} else {	
			String poolXML=stp.getXMLDesc(1);
			//System.out.println(poolXML);
			//xml parsing to get storage pool path
			int startIndex=poolXML.indexOf("type='");
			System.out.println("si="+startIndex);
			//			<pool type='netfs'>
			
			int lastIndex=poolXML.indexOf("'>",startIndex+6);
			System.out.println("li="+lastIndex);
			String path=poolXML.substring(startIndex+6, lastIndex);
			System.out.println("path="+path);
			
			//check type iscsi or netfs
			if(path.compareToIgnoreCase("iscsi")==0)
				return -2;
			try {
			stv=stp.storageVolLookupByName(volName);
			} catch (LibvirtException e) {
				System.out.println(e.getMessage());
			}
			
			if(stv==null){
				return -1;
			} else{
				stv.delete(0);				
				stv.free();
				return 1;
			}
		} 
		
	}
	
	public ArrayList<ObjectNode> listStorageVol() throws LibvirtException {
    	
    	if(stp==null)
    		return null;	//pool not found
    	StorageVol stv = null;
    	String [] stvList=stp.listVolumes();
    	ArrayList<ObjectNode> jsolist = new ArrayList<ObjectNode>();
		ObjectNode jso=null;
    	for(String stvName : stvList)
    	{
    		jso=Json.newObject();
    		jso.put("Name", stvName);
    		stv=stp.storageVolLookupByName(stvName);
    		StorageVolInfo stvInfo=stv.getInfo();
    		//jso.put("Type", stvInfo.type.name()); unn
    		jso.put("Capacity", stvInfo.capacity/1024/1024);
    		jso.put("Allocation",stvInfo.allocation*100/stvInfo.capacity);
    		stv.free();
    		jsolist.add(jso);
    	}
    	stp.free();
    	return jsolist;
    }

}