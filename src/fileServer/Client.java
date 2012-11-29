package fileServer;

//import java.io.File;
import java.io.FileOutputStream;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Arrays;
import java.util.Scanner;

public class Client {
	
	private Client(){}
	
	public static void main(String args[]){
			
			String host = (args.length < 1) ? null : args[0];
			
			Scanner scanner = new Scanner(System.in);
			
			try{
				Registry registry = LocateRegistry.getRegistry(host);
			    FServerRMI stub = (FServerRMI) registry.lookup("FileServer");

			    String[] fileList = stub.getFileList();
			    
			    System.out.println("FILES:");
			    for(int i=0; i<fileList.length; i++){
			    	System.out.println(fileList[i]);
			    }
			    
			    //Request user chooses file
			    System.out.print("\nChoose File: ");
			    String fileChosen = scanner.next();
			    
			    if(Arrays.asList(fileList).contains(fileChosen)){
			    	byte[] fileBytes = stub.retrieveFile(fileChosen);
				    
				    FileOutputStream newF = new FileOutputStream("C:/Users/Tom/Downloads/CLIENTCOPY_" + fileChosen);
				    newF.write(fileBytes);
				    
				    System.out.println("File " + fileChosen + " copied!");
			    }
			    else{
			    	System.out.println("You can't do anything right.");
			    }

			    //System.out.println("response: " + response);
			    //System.out.println("resp2: " + lkupResp);
			    
			} catch(Exception e){
				System.err.println("Client exception: " + e.toString());
			    e.printStackTrace();
			}
		}

}
