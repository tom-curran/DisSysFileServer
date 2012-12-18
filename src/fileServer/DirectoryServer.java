package fileServer;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

public class DirectoryServer implements DServerRMI{
	
	private FileFolderMapPair fileFolderStruct = new FileFolderMapPair();
	
	private HashMap<String, Date> editTimes = new HashMap<String, Date>();
	
	//Initialising directory structure:
	public DirectoryServer(){
		try{			
			Registry registry = LocateRegistry.getRegistry(null);
			FServerRMI FServerStub = (FServerRMI) registry.lookup("FileServer");
			
			String[] fileList = FServerStub.getFileList();
			
			//Build hashmap of files (name -> filepath)
			File tempF;
			for(int i=0; i<fileList.length; i++){
				tempF = new File(fileList[i]);
				fileFolderStruct.files.put(tempF.getName(), fileList[i]);
			}
			System.out.println("Directory Server Ready.");			
		}
		catch(Exception e){
			System.err.println("Directory Server unable to start. Exception: " + e.toString());
		    e.printStackTrace();
		}
	}
	
	//Internal method to break down path given to final folder:
	private HashMap<String, String> getFileDir(String breadcrumbs){
		String crumbs[] = breadcrumbs.split("/");
		if(crumbs.length == 1 && crumbs[0].equals("")) crumbs = new String[0];
		
		if(crumbs.length == 0){
			return fileFolderStruct.files;
		}
		else{
			FileFolderMapPair temp = fileFolderStruct.folders.get(crumbs[0]);
			
			for(int i=1; i<crumbs.length; i++){
				if(temp == null) return null;
				temp = temp.folders.get(crumbs[i]);
			}
			if(temp.files == null) temp.files = new HashMap<String, String>();
			return temp.files;
		}
	}
	
	//Internal method for getting folder list at breadcrumb position (for adding/changing name of folders)
	private HashMap<String, FileFolderMapPair> getFolderDir(String breadcrumbs){
		String crumbs[] = breadcrumbs.split("/");
		if(crumbs.length == 1 && crumbs[0].equals("")) crumbs = new String[0];
		
		if(crumbs.length == 0){
			return fileFolderStruct.folders;
		}
		else{
			FileFolderMapPair temp = fileFolderStruct.folders.get(crumbs[0]);
			
			for(int i=1; i<crumbs.length; i++){
				if(temp == null) return null;
				temp = temp.folders.get(crumbs[i]);
			}
			if(temp.folders == null) temp.folders = new HashMap<String, FileFolderMapPair>();
			return temp.folders;
		}
	}
	
	private AtomicReference<HashMap<String, FileFolderMapPair>> getFolderRef(String breadcrumbs){
		String crumbs[] = breadcrumbs.split("/");
		if(crumbs.length == 1 && crumbs[0].equals("")) crumbs = new String[0];
		
		if(crumbs.length == 0){
			return new AtomicReference<HashMap<String, FileFolderMapPair>>(fileFolderStruct.folders);
		}
		else{
			System.err.println("HERE");
			AtomicReference<FileFolderMapPair> temp = new AtomicReference<FileFolderMapPair>(fileFolderStruct.folders.get(crumbs[0]));
			System.err.println("GOT HERE");
			for(int i=1; i<crumbs.length; i++){
				if(temp.get() == null) return null;
				temp = new AtomicReference<FileFolderMapPair>(temp.get().folders.get(crumbs[i]));
			}
			if(temp.get().folders == null) temp.get().folders = new HashMap<String, FileFolderMapPair>();
			return new AtomicReference<HashMap<String, FileFolderMapPair>>(temp.get().folders);
		}
	}
	
	//Returns list of files/folders in current directory
	public String[] getFileFolderList(String breadcrumbs){
		HashMap<String, String> fileDir = getFileDir(breadcrumbs);		
		HashMap<String, FileFolderMapPair> folderDir = getFolderDir(breadcrumbs);
		if(folderDir == null && fileDir == null) return null;
		
		ArrayList<String> fileFolderList = new ArrayList<String>();		
		if(fileDir != null){
			String[] files = fileDir.keySet().toArray(new String[fileDir.size()]);
			for(int i=0; i < files.length; i++){
				fileFolderList.add(files[i]);
			}			
		}	
		if(folderDir != null){
			String[] folders = folderDir.keySet().toArray( new String[folderDir.size()]);
			for(int i=0; i < folderDir.size(); i++){
				fileFolderList.add(folders[i] + "/");
			}
		}
		
		String fileFolderArray[] = new String[fileFolderList.size()];
		return fileFolderList.toArray(fileFolderArray);
	}
	
	public String getFilePath(String breadcrumbs, String file){
		HashMap<String, String> fileDir = getFileDir(breadcrumbs);
		if(fileDir == null) return null;
		
		return fileDir.get(file);
	}
	
	public boolean renameFile(String breadcrumbs, String currentFilename, String newFilename){
		HashMap<String, String> fileDir = getFileDir(breadcrumbs);
		if(fileDir == null) return false;
		
		if(!fileDir.containsKey(currentFilename) || fileDir.containsKey(newFilename)) return false;
		else{
			fileDir.put(newFilename, fileDir.get(currentFilename));
			fileDir.remove(currentFilename);
			return true;
		}
	}
	
	public boolean createFolder(String breadcrumbs, String newFolderName){
//		HashMap<String, FileFolderMapPair> folderDir = getFolderDir(breadcrumbs);
//		if(folderDir == null) return false;
//		
//		if(folderDir.containsKey(newFolderName)) return false;	//Cannot rename to existing folder name
//		else{
//			folderDir.put(newFolderName, new FileFolderMapPair());
//			return true;
//		}
		
		AtomicReference<HashMap<String, FileFolderMapPair>> folderRef = getFolderRef(breadcrumbs);
		if(folderRef.get() == null) return false;
		HashMap<String, FileFolderMapPair> folderDir = folderRef.get();
		//if(folderRef.get() == null) return false;
		
		if(folderDir.containsKey(newFolderName)) return false;	//Cannot rename to existing folder name
		else{
			folderDir.put(newFolderName, new FileFolderMapPair());
			folderRef.set(folderDir);
			return true;
		}
		
	}
	
	public boolean addNewFile(String breadcrumbs, String filename, String filepath){
		HashMap<String, String> fileDir = getFileDir(breadcrumbs);
		if(fileDir == null) fileDir = new HashMap<String, String>();
		
		if(fileDir.containsKey(filename)) return false;
		else{			
			fileDir.put(filename, filepath);
			return true;
		}
	}
	
	public boolean checkTimestamp(String filename, Date time){
		if(editTimes.containsKey(filename) && editTimes.get(filename) != null && editTimes.get(filename).equals(time)) return true;
		
		else return false;
	}
	public void updateTimestamp(String filename, Date time){
		if(editTimes.containsKey(filename)) editTimes.remove(filename);
		editTimes.put(filename, time);
	}
	
	public static void main(String args[]){
		//Set code base so RMI can see the classes (classpath)
		System.setProperty("java.rmi.server.codebase", DServerRMI.class.getProtectionDomain().getCodeSource().getLocation().toString());
		
		try {
			Registry registry = LocateRegistry.getRegistry();
			
			//Initialise Lock Server
			DirectoryServer DServerObj = new DirectoryServer();
			DServerRMI DServerStub = (DServerRMI) UnicastRemoteObject.exportObject(DServerObj, 0);
			//Bind to RMI registry			
		
			registry.rebind("DirectoryServer", DServerStub);
		} catch (Exception e) {
		}
	}
}
