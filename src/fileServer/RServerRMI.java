package fileServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface RServerRMI extends Remote{
	
	boolean registerBackupServer(String serverName) throws RemoteException;
	boolean backupFile(byte file[], String filepath) throws RemoteException;
	byte[] retrieveBackup(String filepath) throws RemoteException;

}
