package fileServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Scanner;


public class RunSystem {
	
	public static void main(String args[]){
		
		try{
			
			//Set the code base so rmi can see the class (classpath)
			System.setProperty("java.rmi.server.codebase", DServerRMI.class.getProtectionDomain().getCodeSource().getLocation().toString());
			
			//Get handle on RMI registry
			Registry registry = LocateRegistry.getRegistry();
			
			//Initialise File Server
			FileServer FServerObj = new FileServer();
			FServerRMI FServerStub = (FServerRMI) UnicastRemoteObject.exportObject(FServerObj, 0);
			//Bind to RMI registry
			registry.bind("FileServer", FServerStub);
			System.out.println("File Server ready");
			
			//Initialise Directory Server
			DirectoryServer DServerObj = new DirectoryServer();
			DServerRMI DServerStub = (DServerRMI) UnicastRemoteObject.exportObject(DServerObj, 0);
			//Bind to RMI registry			
			registry.bind("DirectoryServer", DServerStub);
			System.out.println("Directory Server ready");
			
			//Run test client
			Client client = new Client();			
			client.runClient();
						
			//To allow unbinding:
			Scanner sc = new Scanner(System.in);
			String x = "";
			while(!x.equals("exit")){
				x = sc.next();
			}			
			registry.unbind("FileServer");
			System.out.println("File Server unbound from registry");
			registry.unbind("DirectoryServer");
			System.out.println("Directory Server unbound from registry");
			
		} catch(Exception e){
			System.err.println("System setup runtime exception: " + e.toString());
			e.printStackTrace();
		}		
	}

}
