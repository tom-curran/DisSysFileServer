package fileServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class ReplicationServer implements RServerRMI {
	
	private static Registry registry;
	private ArrayList<BServerRMI> backupServers = new ArrayList<BServerRMI>();
	
	public ReplicationServer(){
		try{
			registry = LocateRegistry.getRegistry();
		}catch(Exception e){}
	}
	
	public boolean registerBackupServer(String serverName){
		try{
			BServerRMI tempStub = (BServerRMI) registry.lookup(serverName);		
			if(!backupServers.add(tempStub)) return false;
			else return true;
		}catch(Exception e){
			System.err.println("Couldn't register backup server " + e.toString());
			return false;
		}
	}
	
	
	public boolean backupFile(byte file[], String filepath){
		if(backupServers.isEmpty()) return false;
		
		try {
			for(int i=0; i<backupServers.size(); i++){
				backupServers.get(i).backupFile(file, filepath);
				System.out.println("BACKED UP");
			}
		} catch (RemoteException e) {
			return false;
		}			
		return true;
	}
	
	
	public byte[] retrieveBackup(String filepath){
		if(backupServers.isEmpty()) return null;
		
		byte file[] = null;
		try {
			for(int i=0; i<backupServers.size(); i++){
				file = backupServers.get(i).getFile(filepath);
				if(file != null) break;
			}
		} catch (RemoteException e) {
			return null;
		}			
		return file;
	}

	
	public static void main(String args[]){
		//Set code base so RMI can see the classes (classpath)
		System.setProperty("java.rmi.server.codebase", DServerRMI.class.getProtectionDomain().getCodeSource().getLocation().toString());
		
		try{
			//Initialise
			ReplicationServer RServerObj = new ReplicationServer();
			RServerRMI RServerStub = (RServerRMI) UnicastRemoteObject.exportObject(RServerObj, 0);
			//Bind to RMI registry
			registry.rebind("ReplicationServer", RServerStub);
			System.out.println("Replication Server ready");
		}catch(Exception e){
			
		}
	}
}
