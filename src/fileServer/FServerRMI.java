package fileServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface FServerRMI extends Remote{
	byte[] getFile(String filepath) throws RemoteException;
}
