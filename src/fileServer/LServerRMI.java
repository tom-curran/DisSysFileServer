package fileServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LServerRMI extends Remote{

	boolean checkAndAddName(String clientName) throws RemoteException;
	void removeName(String clientName) throws RemoteException;
	boolean getLock(String filepath, String clientName) throws RemoteException;
	boolean dropLock(String filepath, String clientName) throws RemoteException;
	boolean checkLock(String filepath, String clientName) throws RemoteException;
	
}