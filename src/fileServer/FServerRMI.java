package fileServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FServerRMI extends Remote{
	String[] getFileList() throws RemoteException;
	byte[] retrieveFile(String filepath) throws RemoteException;
	void writeNewFile(byte[] newFile, String filename) throws RemoteException;
}
