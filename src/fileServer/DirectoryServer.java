package fileServer;

import java.io.File;
import java.util.HashMap;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class DirectoryServer implements DServerRMI{
	
	private HashMap<String, String> fileMap = new HashMap<String, String>();
	
	public DirectoryServer(){
		
		try{
			
			Registry registry = LocateRegistry.getRegistry(null);
			FServerRMI FServerStub = (FServerRMI) registry.lookup("FileServer");
			
			String[] fileList = FServerStub.getFileList();
			
			//Build hashmap of files (name -> filepath)
			File tempF;
			for(int i=0; i<fileList.length; i++){
				tempF = new File(fileList[i]);
				fileMap.put(tempF.getName(), fileList[i]);
			}			
			
		}
		catch(Exception e){
			System.err.println("Directory Server unable to start. Exception: " + e.toString());
		    e.printStackTrace();
		}
	}
	
	public String[] getFileList(){
		
		return (String[])(fileMap.keySet().toArray( new String[fileMap.size()]));
	}
	
	public String getFilePath(String file){
		return fileMap.get(file);
	}
}
