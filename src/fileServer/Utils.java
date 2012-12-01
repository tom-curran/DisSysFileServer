package fileServer;

//Imports to use:
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

//Exceptions:
import java.io.FileNotFoundException;
import java.io.IOException;

public class Utils {
	
	private static final Utils instance = new Utils();
	
	private Utils(){}
	
	public static Utils getUtils(){
		return instance;
	}
	
	//Serialises file specified by filepath string, returns byte array
	public byte[] serialiseFile(String filepath) throws FileNotFoundException, IOException{
		
		//Check if file exists:
		File fileToSerialise = new File(filepath);
		if(!fileToSerialise.exists()){
			System.err.println("File not found.");
			return null;
		}
		
		//Open file and read into byte array fileByteArray[], return
		FileInputStream fileStream = new FileInputStream(fileToSerialise);
		byte fileByteArray[] = new byte[(int)fileToSerialise.length()];			
		fileStream.read(fileByteArray);
		
		return fileByteArray;
		
	}
	
	//Deserialises byte array specified into file 'filepath', if file already exists, renames old file
	public void deSerialiseFile(byte[] serialisedFile, String filepath) throws FileNotFoundException, IOException{
		
		//Check if file already exists in this path, if yes, rename existing file (append '_old')
		File testExists = new File(filepath);
		if(testExists.exists()){
			System.out.println("File already exists with this name, renaming old file.");
			File renamedFile = new File(filepath + "_old");
			testExists.renameTo(renamedFile);
		}
		
		//Write byte[] into file 'filepath'
		FileOutputStream newF = new FileOutputStream(filepath);
	    newF.write(serialisedFile);
	    
	    System.out.println("File " + (new File(filepath).getName()) + " written!");
		
	}
}
