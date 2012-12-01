package fileServer;

//import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Arrays;
import java.util.Scanner;

public class Client {
	
	String workingDir = "C:/Users/Tom/Downloads/Testing/javaTest_CLIENT_COPIES";
	String clientName = null;
	
	public Client(){}
	
	public void runClient(){
		Scanner scanner = new Scanner(System.in);
		
		try{
			//System.setSecurityManager(new RMISecurityManager());
			
			//Getting RMI registry
			Registry registry = LocateRegistry.getRegistry(null);
			
			//Getting stub for Directory server:
		    DServerRMI DServerStub = (DServerRMI) registry.lookup("DirectoryServer");		    
		    //Get stub for File Server
		    FServerRMI FServerStub = (FServerRMI) registry.lookup("FileServer");
		    
		    //Get client's name
			while(clientName == null){
				System.out.print("Enter username: ");
			    String cName = scanner.next();
//			    if(lockServer.nameTaken()){
//			    	System.out.println("Name taken, try again.");
//			    }
//			    else{
			    	clientName = cName;
			    	System.out.println("Name set. Hello " + clientName);
//			    }
			    
			}
		    
		    //Getting list of files from directory server
		    String[] fileList = DServerStub.getFileList();
		    //Print file list
		    System.out.println("FILES:");
		    for(int i=0; i<fileList.length; i++){
		    	System.out.println(fileList[i]);
		    }
		    
		    //Request user chooses file
		    System.out.print("\nChoose File: ");
		    String fileChosen = scanner.next();
		    
		    //If choice is in list given, retrieve file and write to workingDir/CLIENTCOPY_filename"
		    if(Arrays.asList(fileList).contains(fileChosen)){
		    	
		    	//Get full file server filepath of file needed from directory server
		    	String filepath = DServerStub.getFilePath(fileChosen);
		    	
		    	//Retrieve file from file server:
		    	byte[] fileBytes = FServerStub.retrieveFile(filepath);
		    	
		    	//Deserialise byte[] into file on client
		    	String localPath = (workingDir + "/CLIENTCOPY_" + fileChosen);
		    	Utils.getUtils().deSerialiseFile(fileBytes, localPath);
			    
			    System.out.println("File " + fileChosen + " copied!");
		    }
		    else{
		    	System.out.println("File specified is not available.");
		    }
		    
		    //Test new file writing to server:
		    byte sendFile[] = Utils.getUtils().serialiseFile(workingDir + "/Silhouette.mp3");
		    FServerStub.writeNewFile(sendFile, "silTest.mp3");
		    
		    
		} catch(Exception e){
			System.err.println("Client exception: " + e.toString());
		    e.printStackTrace();
		}
	}
}
