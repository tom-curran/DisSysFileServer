package fileServer;

//import java.rmi.RemoteException;
import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Scanner;

public class DirectoryServer implements DServerRMI{
	
	Hashtable<String, String> fileTable = new Hashtable<String, String>();
	
	public DirectoryServer(){
		try{
			
			Registry registry = LocateRegistry.getRegistry(null);
			FServerRMI stub = (FServerRMI) registry.lookup("FileServer");
			
			String[] fileList = stub.getFileList();
			
			//Build hashmap of files (name -> filepath)
			File tempF;
			for(int i=0; i<fileList.length; i++){
				tempF = new File(fileList[i]);
				fileTable.put(tempF.getName(), fileList[i]);
				//System.out.println(fileList[i]);
			}			
			
		}
		catch(Exception e){
			System.err.println("Directory Server unable to start. Exception: " + e.toString());
		    e.printStackTrace();
		}
	}
	
	public String[] getFileList(){
		String filenameList[] = new String[fileTable.size()];
		int i=0;
		for(Enumeration<String> en = fileTable.keys(); en.hasMoreElements(); i++){
			filenameList[i] = en.nextElement();
		}
		return filenameList;
	}
	
	public String getFilePath(String file){
		return fileTable.get(file);
	}
	
	public static void main(String args[]){
		
		try{
			//Set the code base so rmi can see the class (classpath)
			System.setProperty("java.rmi.server.codebase", DServerRMI.class.getProtectionDomain().getCodeSource().getLocation().toString());
			
			DirectoryServer obj = new DirectoryServer();
			DServerRMI stub = (DServerRMI) UnicastRemoteObject.exportObject(obj, 0);
			
			//Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("DirectoryServer", stub);
			
			System.err.println("Directory Server ready");
			
			//To allow unbinding:
			Scanner sc = new Scanner(System.in);
			String x = "";
			while(!x.equals("exit")){
				x = sc.next();
			}
			registry.unbind("DirectoryServer");
			System.out.println("Directory Server unbound from registry");
			
		} catch(Exception e){
			System.err.println("Directory Server runtime exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
