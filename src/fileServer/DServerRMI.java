package fileServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface DServerRMI extends Remote{
	
	String[] getFileList() throws RemoteException;
	String getFilePath(String file) throws RemoteException;
	
}
