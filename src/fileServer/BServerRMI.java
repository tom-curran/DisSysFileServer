package fileServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface BServerRMI extends Remote{
	
	boolean backupFile(byte file[], String filepath) throws RemoteException;
	byte[] getFile(String filepath) throws RemoteException;

}
