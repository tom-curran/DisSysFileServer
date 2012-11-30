package fileServer;

import java.io.File;
import java.io.FileOutputStream;

public class Utils {
	
	private static final Utils instance = new Utils();
	
	private Utils(){}
	
	public static Utils getUtils(){
		return instance;
	}
	
	public byte[] serialiseFile(File x){
		return null;
	}
	
	public void deSerialiseFile(byte[] serialisedFile, String filepath) throws Exception{
		//Check if file already exists in this path, if yes, rename existing file (append '_old')
		File testExists = new File(filepath);
		if(testExists.exists()){
			System.err.println("File already exists with this name, renaming old file.");
			File renamedFile = new File(filepath + "_old");
			testExists.renameTo(renamedFile);
		}
		//Write byte[] into file 'filepath'
		FileOutputStream newF = new FileOutputStream(filepath);
	    newF.write(serialisedFile);
	    
	    System.out.println("File " + (new File(filepath).getName()) + " written to server!");
		
	}
}
