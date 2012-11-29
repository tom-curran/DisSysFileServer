package fileServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;


public class FileServer implements FServerRMI{
	
	public FileServer(){}
	
	public String[] getFileList(){
		File dir = new File("C:/Users/Tom/Downloads/Testing/javaTest");
		
		File listDir[] = dir.listFiles();
		
		ArrayList<String> listOfFiles = new ArrayList<String>();
		
		for(int i=0; i<listDir.length; i++){
			if(listDir[i].isFile()) listOfFiles.add(listDir[i].getName());
		}
		
		String fileList[] = new String[listOfFiles.size()];
		listOfFiles.toArray(fileList);
		
		return fileList;
		
		//Object[] objList = listOfFiles.toArray();
		
		//return Arrays.asList(objList).toArray(new String[objList.length]);
	}
	
	public byte[] retrieveFile(String filename){
		String filepath = "C:/Users/Tom/Downloads/Testing/javaTest" + "/" + filename;
		File fileToRetrieve = new File(filepath);
		
		try{
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
			
		} catch(Exception e){
			System.err.println("Server exception: " + e.toString());
			e.printStackTrace();
		}
	}
}
