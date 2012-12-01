package fileServer;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LServerRMI extends Remote{
	String test() throws RemoteException;
	boolean getLock(String filepath) throws RemoteException;
}