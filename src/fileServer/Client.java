package fileServer;

//import java.rmi.RMISecurityManager;
//import java.rmi.RemoteException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Arrays;
import java.util.Scanner;

public class Client {
	
	//Globals
	private String workingDir;
	private String clientName = null;
	private Scanner scanner = new Scanner(System.in);
	
	//RMI registry
	private Registry registry;
	//Server stubs
	private FServerRMI FServerStub;
    private DServerRMI DServerStub;
    private LServerRMI LServerStub;
    
    //Constructor(s)
	public Client(){}
	public Client(String workspace){workingDir = workspace;}		//To be removed
	
	
	public void setup(){		
		try{
			//System.setSecurityManager(new RMISecurityManager());
			
			//Get RMI registry
			registry = LocateRegistry.getRegistry(null);
			
			//Get stubs for File, Directory, and Lock Servers
		    FServerStub = (FServerRMI) registry.lookup("FileServer");
		    DServerStub = (DServerRMI) registry.lookup("DirectoryServer");
		    LServerStub = (LServerRMI) registry.lookup("LockServer");
		    
		    //Get client's name, register with Lock Server
			while(clientName == null){
				System.out.print("Enter username: ");
			    String cName = scanner.next();
			    if(!LServerStub.checkAndAddName(cName)){
			    	System.out.println("Name taken, try again.");
			    }
			    else{
			    	clientName = cName;
			    	System.out.println("Name set with lock server. Hello " + clientName);
			    }			    
			}
		} catch(Exception e){
			System.err.println("Exception setting up client " + e.toString());
			e.printStackTrace();
		}
	}
	public void closeConnection(){
		//Remove client name from lock server when done
		try{
			LServerStub.removeName(clientName);
		} catch(RemoteException e){
			System.err.println("Exception removing client name from lock server " + e.toString());
			e.printStackTrace();
		}
	}
	
	//Main menu for interface
	public void mainMenu(){
		System.out.println("Main Menu\n");
		System.out.println("File List:");		
		try{
			//Getting list of files from directory server
		    String[] fileList = DServerStub.getFileList();
		    //Print file list
		    for(int i=0; i<fileList.length; i++){
		    	System.out.println(fileList[i]);
		    }
	    } catch(RemoteException e){
	    	System.err.println("Exception retrieving file list " + e.toString());
	    	e.printStackTrace();
	    }
	}
	
	public void runTestClient(){		
		try{
			setup();
			
		    //Getting list of files from directory server
		    String[] fileList = DServerStub.getFileList();
		    //Print file list
		    System.out.println("\nFiles Available:");
		    for(int i=0; i<fileList.length; i++){
		    	System.out.println(fileList[i]);
		    }
		    
		    //User chooses file
		    System.out.print("\nChoose File: ");
		    String fileChosen = scanner.next();
		    
		    //If choice is in the list given, retrieve file and write to workingDir/CLIENTCOPY_filename"
		    if(Arrays.asList(fileList).contains(fileChosen)){		    	
		    	//Get full file server filepath of file needed from directory server
		    	String filepath = DServerStub.getFilePath(fileChosen);
		    	
		    	//Retrieve file from file server:
		    	byte[] fileBytes = FServerStub.retrieveFile(filepath);
		    	
		    	if(FServerStub.overwriteFile(fileBytes, filepath, clientName)){
		    		System.out.println("File " + fileChosen + " overwritten!");
		    	}
		    	else{
		    		System.out.println("Overwrite failed!");
		    	}
		    	
		    	//Deserialise byte[] into file on client
//		    	String localPath = (workingDir + "/CLIENTCOPY_" + fileChosen);
//		    	Utils.getUtils().deSerialiseFile(fileBytes, localPath);
			    
			    
		    }
		    else{
		    	System.out.println("File specified is not available.");
		    }
		    
		    //Test new file writing to server:
//		    byte sendFile[] = Utils.getUtils().serialiseFile(workingDir + "/Silhouette.mp3");
//		    FServerStub.writeNewFile(sendFile, "silTest.mp3");
		    
		    closeConnection();		    	    
		    
		} catch(Exception e){
			System.err.println("Client exception: " + e.toString());
		    e.printStackTrace();
		}
	}
}
