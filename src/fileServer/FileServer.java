package fileServer;

import java.io.File;
import java.util.ArrayList;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class FileServer implements FServerRMI{
	
	private String homeDir = "C:/Users/Tom/Desktop/Servers/FileServer"; 
	
	private static Registry registry;
	private static RServerRMI RServerStub;
	
	public FileServer(){
		try{
			registry = LocateRegistry.getRegistry();
			RServerStub = (RServerRMI) registry.lookup("ReplicationServer");
		}catch(Exception e){}
	}
	
	public String[] getFileList(){
		File dir = new File(homeDir);
		
		File listDir[] = dir.listFiles();
		
		ArrayList<String> listOfFiles = new ArrayList<String>();
		
		//Create list of files, ignoring directories
		for(int i=0; i<listDir.length; i++){
			if(listDir[i].isFile()) listOfFiles.add(listDir[i].getAbsolutePath());
		}
		
		String fileList[] = new String[listOfFiles.size()];
		listOfFiles.toArray(fileList);
		
		return fileList;
	}
	
	public byte[] retrieveFile(String filepath){
		
		try{
			return Utils.getUtils().serialiseFile(filepath);		
		}
		catch(Exception e){
			System.err.println("Error serialising file " + e.toString());
			e.printStackTrace();
			return null;
		}
	}
	
	public boolean writeNewFile(byte[] newFile, String filename, String breadcrumbs){
		//Write newFile into new file 'filename'
		String filepath = homeDir + "/" + filename;
		try{
			File test = new File(filepath);
			while(test.exists()){
				filepath += "_1";
				test = new File(filepath);
			}
			Utils.getUtils().deSerialiseFile(newFile, filepath);
		}
		catch(Exception e){
			System.err.println("Could not write " + filepath + "to file server" + e.toString());
			e.printStackTrace();
			return false;
		}
		
		try{
			Registry registry = LocateRegistry.getRegistry(null);
			DServerRMI DServerStub = (DServerRMI) registry.lookup("DirectoryServer");
			
			DServerStub.addNewFile(breadcrumbs, filename, filepath);
			RServerStub.backupFile(newFile, filepath);
		}
		catch(RemoteException e){
			return false;
		}
		catch(NotBoundException e){
			return false;
		}
		
		return true;	//Successful write
	}
	
	public boolean overwriteFile(byte[] newFile, String filepath, String clientName){
		
		try{
			Registry registry = LocateRegistry.getRegistry(null);
			LServerRMI LServerStub = (LServerRMI) registry.lookup("LockServer");
			
			//Make sure client has lock on file
			if(LServerStub.checkLock(filepath, clientName)){
				Utils.getUtils().deSerialiseFile(newFile, filepath);
				LServerStub.dropLock(filepath, clientName);
			}
			else return false;
			
		}
		catch(Exception e){
			System.err.println("Could not write " + filepath + "to file server" + e.toString());
			e.printStackTrace();
			return false;
		}		
		
		return true;	//Successful write, lock has been dropped
	}
	
	public boolean rollbackFile(String filepath, String clientName){
		
		try{
			Registry registry = LocateRegistry.getRegistry(null);
			LServerRMI LServerStub = (LServerRMI) registry.lookup("LockServer");
			
			//Make sure client has lock on file
			if(LServerStub.checkLock(filepath, clientName)){
				//Utils.getUtils().deSerialiseFile(newFile, filepath);
				File latestVersion = new File(filepath);
				File lastVersion = new File(filepath + "_old");
				File tempName = new File(filepath + "_temp");
				
				if(!latestVersion.exists() || !lastVersion.exists()){
					LServerStub.dropLock(filepath, clientName);
					return false;
				}
				else{
					lastVersion.renameTo(tempName);
					latestVersion.renameTo(lastVersion);
					lastVersion.renameTo(latestVersion);
				}
				
				LServerStub.dropLock(filepath, clientName);
				return true;
			}
			else return false;
			
		}
		catch(Exception e){
			System.err.println("Could not write " + filepath + "to file server" + e.toString());
			e.printStackTrace();
			return false;
		}
	}
	
	public static void main(String args[]) throws RemoteException{
		
		//Set code base so RMI can see the classes (classpath)
		System.setProperty("java.rmi.server.codebase", DServerRMI.class.getProtectionDomain().getCodeSource().getLocation().toString());
		
		//Initialise File Server
		FileServer FServerObj = new FileServer();
		FServerRMI FServerStub = (FServerRMI) UnicastRemoteObject.exportObject(FServerObj, 0);
		//Bind to RMI registry
		registry.rebind("FileServer", FServerStub);
		System.out.println("File Server ready");
	}
}
