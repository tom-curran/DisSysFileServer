package fileServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FServerRMI extends Remote{
	
	String[] getFileList() throws RemoteException;
	byte[] retrieveFile(String filepath) throws RemoteException;
	boolean writeNewFile(byte[] newFile, String filename) throws RemoteException;
	boolean overwriteFile(byte[] newFile, String filepath, String clientName) throws RemoteException;
}
