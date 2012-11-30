package fileServer;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Scanner;


public class FileServer implements FServerRMI{
	
	private String homeDir = "C:/Users/Tom/Downloads/Testing/javaTest"; 
	
	public FileServer(){}
	
	public String[] getFileList(){
		File dir = new File(homeDir);
		
		File listDir[] = dir.listFiles();
		
		ArrayList<String> listOfFiles = new ArrayList<String>();
		
		for(int i=0; i<listDir.length; i++){
			if(listDir[i].isFile()) listOfFiles.add(listDir[i].getAbsolutePath());
		}
		
		String fileList[] = new String[listOfFiles.size()];
		listOfFiles.toArray(fileList);
		
		return fileList;
	}
	
	public byte[] retrieveFile(String filepath){
		
		//Check if file exists:
		File fileToRetrieve = new File(filepath);
		if(!fileToRetrieve.exists()){
			System.out.println("File not found.");
			return null;
		}
		
		//Otherwise attempt to open:
		try{
			//Open file and read into byte array fileByteArray[], return
			FileInputStream fileStream = new FileInputStream(fileToRetrieve);
			byte fileByteArray[] = new byte[(int)fileToRetrieve.length()];			
			fileStream.read(fileByteArray);
			
			return fileByteArray;
		}
		catch(FileNotFoundException e){
			return null;
		}
		catch(IOException e){
			return null;
		}		
	}
	
	public void writeNewFile(byte[] newFile, String filename){
		//Write newFile into new file filename
	}
	
	//MAIN:
	public static void main(String args[]){
		
		try{
			
			//Set the code base so rmi can see the class (classpath)
			System.setProperty("java.rmi.server.codebase", FServerRMI.class.getProtectionDomain().getCodeSource().getLocation().toString());
			
			FileServer obj = new FileServer();
			FServerRMI stub = (FServerRMI) UnicastRemoteObject.exportObject(obj, 0);
			
			//Bind the remote object's stub in the registry
			Registry registry = LocateRegistry.getRegistry();
			registry.bind("FileServer", stub);
			
			System.err.println("File Server ready");
			
			//To allow unbinding:
			Scanner sc = new Scanner(System.in);
			String x = "";
			while(!x.equals("exit")){
				x = sc.next();
			}
			registry.unbind("FileServer");
			System.out.println("File Server unbound from registry");
			
		} catch(Exception e){
			System.err.println("File Server exception: " + e.toString());
			e.printStackTrace();
		}
		
		
	}
}
