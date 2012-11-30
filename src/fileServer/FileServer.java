package fileServer;

import java.io.File;
import java.util.ArrayList;

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
		
		try{
			return Utils.getUtils().serialiseFile(filepath);		
		}
		catch(Exception e){
			System.err.println("Error serialising file " + e.toString());
			e.printStackTrace();
			return null;
		}
	}
	
	public void writeNewFile(byte[] newFile, String filename){
		//Write newFile into new file 'filename'
		String filepath = homeDir + "/" + filename;
		try{
			Utils.getUtils().deSerialiseFile(newFile, filepath);
		}
		catch(Exception e){
			System.err.println("Could not write " + filename + "to file server" + e.toString());
			e.printStackTrace();
			
		}
	}
}
