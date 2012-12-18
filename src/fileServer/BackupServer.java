package fileServer;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

import java.util.Scanner;

public class BackupServer implements BServerRMI{
	
	private HashMap<String, String> filepaths = new HashMap<String, String>();	
	private static String backupDir;	
	private int internalNumber = 1;
	
	private BackupServer(){}
	
	public BackupServer(String directory, String serverName){
		backupDir = directory;
		try{			
			Registry registry = LocateRegistry.getRegistry(null);
			
			//Initialise Lock Server
			BackupServer BServerObj = new BackupServer();
			BServerRMI BServerStub = (BServerRMI) UnicastRemoteObject.exportObject(BServerObj, 0);
			//Bind to RMI registry			
			registry.rebind(serverName, BServerStub);
			
			RServerRMI RServerStub = (RServerRMI) registry.lookup("ReplicationServer");
			
			RServerStub.registerBackupServer(serverName);
		}
		catch(Exception e){
			System.err.println("Couldn't start backup server " + e.toString());
			e.printStackTrace();
		}
		System.out.println("Backup Server: " + serverName + " ready");
	}
	
	public boolean backupFile(byte file[], String filepath){
		String filename = (new File(filepath)).getName();
		String localpath = backupDir + filename + "_" + internalNumber;
		internalNumber++;
		try {
			Utils.getUtils().deSerialiseFile(file, localpath);
			System.err.println(localpath);
		} catch (Exception e) {
			return false;
		}
		filepaths.put(filepath, localpath);
		return true;
	}
	
	public byte[] getFile(String filepath){
		byte returnFile[] = null;
		try{
			returnFile =  Utils.getUtils().serialiseFile(filepaths.get(filepath));
		}catch(Exception e){
			return null;
		}
		
		return returnFile;
	}
	
	public static void main(String args[]){
		//Set code base so RMI can see the classes (classpath)
		System.setProperty("java.rmi.server.codebase", DServerRMI.class.getProtectionDomain().getCodeSource().getLocation().toString());
		
		Scanner scanner = new Scanner(System.in);
		
		System.out.print("Enter name for backup server: ");
		String name = scanner.next();
		System.out.print("Enter working directory for " + name + ": ");
		String dir = scanner.next();
		//"C:/Users/Tom/Desktop/Servers/Backups/BServ1/"
		new BackupServer(dir, name);
	}

}
