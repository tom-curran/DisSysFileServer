package fileServer;

import java.util.HashMap;
import java.util.HashSet;

public class LockServer implements LServerRMI{
	
	private HashMap<String,String> lockMap = new HashMap<String,String>();	//Mapping from filepath to client name
	private HashSet<String> clientSet = new HashSet<String>();				//Set of client names
	
	public LockServer(){}
	
	//Add new client name, if name is available
	public boolean checkAndAddName(String clientName){
		if(clientSet.contains(clientName)){
			return false;
		}
		else{
			clientSet.add(clientName);
			return true;
		}
	}	
	//Remove client name, client will call at logout
	public void removeName(String clientName){
		clientSet.remove(clientName);
	}
	
	
	//Get lock on file 'filepath' for 'clientName'
	public boolean getLock(String filepath, String clientName){
		//If client has not been registered, return false
		if(!clientSet.contains(clientName)) return false;
		
		String lockHolder = lockMap.get(filepath);
		if(lockHolder == null){
			//Grant lock, return true
			lockMap.put(filepath, clientName);
			return true;
		}
		else if(lockHolder == clientName){
			return true;	//Client already has the lock
		}
		else{
			return false;	//Someone else has lock
		}
	}
	//Drop existing lock on file 'filepath' held by 'clientName'
	public boolean dropLock(String filepath, String clientName){
		
		if(!lockMap.containsKey(filepath)) return false;	//Lock not held
		
		//Check if client is the lock holder, drop lock if they are
		String lockHolder = lockMap.get(filepath);
		if(clientName.equals(lockHolder)){
			lockMap.remove(filepath);
			return true;
		}
		else return false;
	}
	
	//Method for file server to check if client has lock on file to write to
	public boolean checkLock(String filepath, String clientName){
		
		if(!lockMap.containsKey(filepath)) return false;	//No one has lock on this file
		
		String lockHolder = lockMap.get(filepath);		
		if(clientName.equals(lockHolder)) return true;
		else return false;
	}

}
