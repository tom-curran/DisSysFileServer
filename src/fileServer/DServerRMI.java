package fileServer;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

public interface DServerRMI extends Remote{
	
	String[] getFileFolderList(String breadcrumbs) throws RemoteException;
	String getFilePath(String breadcrumbs, String file) throws RemoteException;
	boolean renameFile(String breadcrumbs, String currentFilename, String newFilename) throws RemoteException;
	boolean createFolder(String breadcrumbs, String newFolderName) throws RemoteException;
	boolean addNewFile(String breadcrumbs, String filename, String filepath) throws RemoteException;
	boolean checkTimestamp(String filename, Date time) throws RemoteException;
	void updateTimestamp(String filename, Date time) throws RemoteException;
}
