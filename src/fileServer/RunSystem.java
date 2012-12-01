package fileServer;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

//import java.util.Scanner;


public class RunSystem {
	
	private static Registry registry;
	private static FServerRMI FServerStub;
	private static DServerRMI DServerStub;
	private static LServerRMI LServerStub;
	
	
	public static void setup() throws RemoteException{
		//Get handle on RMI registry
		registry = LocateRegistry.getRegistry();
		
		//Initialise File Server
		FileServer FServerObj = new FileServer();
		FServerStub = (FServerRMI) UnicastRemoteObject.exportObject(FServerObj, 0);
		//Bind to RMI registry
		registry.rebind("FileServer", FServerStub);
		System.out.println("File Server ready");
		
		//Initialise Directory Server
		DirectoryServer DServerObj = new DirectoryServer();
		DServerStub = (DServerRMI) UnicastRemoteObject.exportObject(DServerObj, 0);
		//Bind to RMI registry			
		registry.rebind("DirectoryServer", DServerStub);
		System.out.println("Directory Server ready");
		
		//Initialise Lock Server
		LockServer LServerObj = new LockServer();
		LServerStub = (LServerRMI) UnicastRemoteObject.exportObject(LServerObj, 0);
		//Bind to RMI registry			
		registry.rebind("LockServer", LServerStub);
		System.out.println("Lock Server ready");
	}
	
	public static void setdown() throws Exception{
		//To unbind names from RMI registry
		System.out.println("\nExiting...");
		registry.unbind("FileServer");		
		System.out.println("File Server unbound from registry");
		registry.unbind("DirectoryServer");
		System.out.println("Directory Server unbound from registry");
		registry.unbind("LockServer");
		System.out.println("Lock Server unbound from registry");
	}
	
	public static void main(String args[]){
		
		//Set code base so RMI can see the classes (classpath)
		System.setProperty("java.rmi.server.codebase", DServerRMI.class.getProtectionDomain().getCodeSource().getLocation().toString());
		
		try{
			//Set up file, directory, and lock servers in RMI registry
			setup();			
		} catch(RemoteException e){
			System.err.println("System setup exception: " + e.toString());
			e.printStackTrace();
		}		
		
		
		//Run test client
		Client client = new Client("C:/Users/Tom/Downloads/Testing/javaTest_CLIENT_COPIES");			
		client.runTestClient();
		
		
		try{
			//Unbind servers from RMI registry
			setdown();			
		} catch(Exception e){
			System.err.println("Exception while unbinding servers from RMI registry: " + e.toString());
			e.printStackTrace();
		}
	}

}
