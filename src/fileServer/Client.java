package fileServer;

//import java.io.File;
import java.io.FileOutputStream;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Arrays;
import java.util.Scanner;

public class Client {
	
	String workingDir = "C:/Users/Tom/Downloads";
	
	public Client(){}
	
	public void runClient(){
		Scanner scanner = new Scanner(System.in);
		
		try{
			//Getting RMI registry
			Registry registry = LocateRegistry.getRegistry(null);
			
			//Getting stub for Directory server:
		    DServerRMI DServerStub = (DServerRMI) registry.lookup("DirectoryServer");		    
		    //Get stub for File Server
		    FServerRMI FServerStub = (FServerRMI) registry.lookup("FileServer");
		    
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
		    
		    //If choice is in list given, retrieve file and write to "C:/Users/Tom/Downloads/CLIENTCOPY_FILE"
		    if(Arrays.asList(fileList).contains(fileChosen)){
		    	
		    	//Get full filepath from directory server
		    	String filepath = DServerStub.getFilePath(fileChosen);
		    	
		    	//Retrieve file from file server:
		    	byte[] fileBytes = FServerStub.retrieveFile(filepath);
		    	
			    //Write file to local workspace:
			    FileOutputStream newF = new FileOutputStream(workingDir + "/CLIENTCOPY_" + fileChosen);
			    newF.write(fileBytes);
			    
			    System.out.println("File " + fileChosen + " copied!");
		    }
		    else{
		    	System.out.println("File specified is not available.");
		    }
		    
		} catch(Exception e){
			System.err.println("Client exception: " + e.toString());
		    e.printStackTrace();
		}
	}
	
	public static void main(String args[]){
			
			String host = (args.length < 1) ? null : args[0];
			
			Scanner scanner = new Scanner(System.in);
			
			try{
				//Getting RMI registry
				Registry registry = LocateRegistry.getRegistry(host);
				//Getting stub for Directory server:
			    //DServerRMI DServerStub = (DServerRMI) registry.lookup("DirectoryServer");
			    
				//Get stub for File Server
			    FServerRMI stub = (FServerRMI) registry.lookup("FileServer");
			    
			    //Getting list of files from directory
			    String[] fileList = stub.getFileList();
			    //Print file list
			    System.out.println("FILES:");
			    for(int i=0; i<fileList.length; i++){
			    	System.out.println(fileList[i]);
			    }
			    
			    //Request user chooses file
			    System.out.print("\nChoose File: ");
			    String fileChosen = scanner.next();
			    
			    //If choice is in list given, retrieve file and write to "C:/Users/Tom/Downloads/CLIENTCOPY_FILE"
			    if(Arrays.asList(fileList).contains(fileChosen)){
			    	byte[] fileBytes = stub.retrieveFile(fileChosen);
				    
				    FileOutputStream newF = new FileOutputStream("C:/Users/Tom/Downloads/CLIENTCOPY_" + fileChosen);
				    newF.write(fileBytes);
				    
				    System.out.println("File " + fileChosen + " copied!");
			    }
			    else{
			    	System.out.println("You can't do anything right.");
			    }
			    
			} catch(Exception e){
				System.err.println("Client exception: " + e.toString());
			    e.printStackTrace();
			}
		}

}
