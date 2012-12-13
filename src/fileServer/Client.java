package fileServer;

//import java.rmi.RMISecurityManager;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.Arrays;
import java.util.Scanner;

public class Client {
	
	//Globals
	private String workingDir;
	private String clientName = null;
	private static Scanner scanner = new Scanner(System.in);
	
	private static String currentDir = "";
	
	//RMI registry
	private Registry registry;
	//Server stubs
	private FServerRMI FServerStub;
    private static DServerRMI DServerStub;
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
			    	System.out.println("Name set with lock server. Hello " + clientName + "!");
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
	public static void main(String args[]){
		//mainMenu();
	}
	//Main menu for interface
	public void mainMenu(){
		setup();
		
		boolean exit = false;
		String choiceInput;
		
		while(!exit){
			System.out.println("\n-------------");
			System.out.println("Main Menu\n");
			System.out.println("1) Get File List");
			System.out.println("2) Change Directory");
			System.out.println("3) Rename File");
			System.out.println("4) Copy File");
			System.out.println("5) Read/Edit Text File");
			System.out.println("6) Create New File");
			System.out.println("7) Create Directory");
			System.out.println("8) Exit");
			
			System.out.print("Selection: ");
			choiceInput = scanner.next();
			while(!choiceInput.equals("1") && !choiceInput.equals("2") && !choiceInput.equals("3") && !choiceInput.equals("4")
					&& !choiceInput.equals("5") && !choiceInput.equals("6") && !choiceInput.equals("7") && !choiceInput.equals("8")){
				System.out.print("Invalid choice, try again: ");
				choiceInput = scanner.next();
			}
			System.out.println("\n");
			
			int choice = Integer.parseInt(choiceInput);			
			switch(choice){
			case 1:
				//Get file list
				System.out.println("File List:");		
				try{
					//Getting list of files from directory server
				    String[] fileList = DServerStub.getFileFolderList(currentDir);
				    if(fileList != null){
					    //Print file list
					    for(int i=0; i<fileList.length; i++){
					    	System.out.println(fileList[i]);
					    }
				    }
			    } catch(RemoteException e){
			    	System.err.println("Exception retrieving file list " + e.toString());
			    	e.printStackTrace();
			    }
				break;
			case 2:
				//Change Directory
				System.out.println("Enter directory name to move to (.. for up)");
				String cd = scanner.next();
				try{
					if(cd.equals("..")){
						System.out.println(currentDir);
						String crumbs[] = currentDir.split("/");
						if(crumbs.length == 0 && crumbs[0].equals("")) System.out.println("Already at root");
						else{
							currentDir = crumbs[0];
							for(int i=1; i < crumbs.length-1; i++){
								currentDir += ("/" + crumbs[i]);
								System.out.println(crumbs[i]);
							}
							System.out.println(currentDir);
						}
					}
					else{
						//Getting list of files/folders from directory server
					    String[] fileList = DServerStub.getFileFolderList(currentDir);
					    //Ensure folder is in list				    
					    if(fileList != null || Arrays.asList(fileList).contains(cd + "/")){
					    	currentDir += ("/" + cd);
					    }
					    else System.out.println("No directory with that name");
					}
			    } catch(RemoteException e){
			    	System.err.println("Exception retrieving file list " + e.toString());
			    	e.printStackTrace();
			    }
				break;
			case 3:
				//Rename File			
				System.out.print("File to rename: ");
				String mv1 = scanner.next();
				System.out.print("New name: ");
				String mv2 = scanner.next();
				
				try{
					if(!DServerStub.renameFile(currentDir, mv1, mv2)) System.out.println("Failed to rename file");
				}
				catch(RemoteException e){
					System.err.println("Exception retrieving file list " + e.toString());
			    	e.printStackTrace();
				}
				break;
			case 4:
				//Copy File
				System.out.print("File to copy: ");
				String cp1 = scanner.next();
				try{
					String cp2 = DServerStub.getFilePath(currentDir, cp1);
					if(cp2 == null) System.out.println("File not available");
					else{
						//Copy file to local directory
						byte copiedFile[] = FServerStub.retrieveFile(cp2);
						Utils.getUtils().deSerialiseFile(copiedFile, (workingDir + "/" + cp1));
						System.out.println("File copied to local directory: " + (workingDir + "/" + cp1));
					}
				}
				catch(Exception e){
					System.err.println("Exception copying file " + e.toString());
			    	e.printStackTrace();
				}
				break;
			case 5:
				//Read/Edit File
				System.out.println("CAN'T DO THAT YET!");
				System.out.print("File to edit: ");
				String gedit = scanner.next();
				//CHECK CACHE
				try{
					String serverFilepath = DServerStub.getFilePath(currentDir, gedit);
					if(serverFilepath == null) System.out.println("File not available");
					else{
						//Copy file to local directory
						byte copiedFile[] = FServerStub.retrieveFile(serverFilepath);
						Utils.getUtils().deSerialiseFile(copiedFile, (workingDir + "/cache/" + gedit));	//Copied to cache, must write again when editing is finished
					}
				}
				catch(Exception e){
					System.err.println("Exception copying file " + e.toString());
			    	e.printStackTrace();
				}
				break;
			case 6:
				//Create File
				try{
					File tempFile = new File(workingDir + "/cache/" + "tempWrite.txt"); 
					FileWriter fstream = new FileWriter(tempFile);
					BufferedWriter out = new BufferedWriter(fstream);
					boolean finished = false;
					String lineInput;
					
					System.out.println("Enter text to write to file (enter a single period to end \".\")");
					while(!finished){
						lineInput = scanner.nextLine();
						if(lineInput.equals(".")){
							System.out.print("Finished? (y/n): ");
							String yn = scanner.next();
							if(yn.equals("y") || yn.equals("yes")) finished = true;
						}
						else{
							//Write line to file
							lineInput += "\n";
							out.write(lineInput);
						}
					}
					out.close();
					
					System.out.print("Enter name to save file as: ");
					String filename = scanner.next();
					File finalFile = new File(workingDir + "/cache/" + filename + ".txt");
					
					if(!tempFile.renameTo(finalFile)){
						System.err.println("Error renaming! WAT!?");
					}
					else tempFile.delete();
					//Check file doesn't already exist on file server
					//Get lock on file
					//createNewFile() on file server
					
					out.close();
				} catch(IOException e){
					System.err.println("Error writing to temporary file " + e.toString());
					e.printStackTrace();
				}
				
				break;
			case 7:
				//Create Directory
				System.out.println("Enter name of new directory");
				String mkdir = scanner.next();
				try{
					//Getting list of files/folders from directory server
				    String[] fileList = DServerStub.getFileFolderList(currentDir);
				    //Ensure directory does not already exist
				    if(fileList == null || !Arrays.asList(fileList).contains(mkdir + "/")){
				    	DServerStub.createFolder(currentDir, mkdir);
				    }
				    else System.out.println("Directory already exists");				    
			    } catch(RemoteException e){
			    	System.err.println("Exception retrieving file list " + e.toString());
			    	e.printStackTrace();
			    }
				break;
			case 8:
				//Exit
				System.out.println("\nExiting...");
				exit = true;
				break;
			}			
		}
	}
	
	public void runTestClient(){		
		try{
			setup();
			
		    //Getting list of files from directory server
		    String[] fileList = DServerStub.getFileFolderList(currentDir);
		    //Print file list
		    //System.out.println("\nFiles Available:");
		    
		    if(fileList == null){
		    	System.out.println("NONE");
		    }
		    else{
		    	if(DServerStub.renameFile("", "thisText.txt", "TESTCHANGE.txt")){
		    		
		    		System.out.println("DONE!");		    	
		    	
			    	fileList = DServerStub.getFileFolderList(currentDir);
			    	
			    	for(int i=0; i<fileList.length; i++){
			        	System.out.println(fileList[i]);
			    	}
		    	}
//		    
//			    //User chooses file
//			    System.out.print("\nChoose File: ");
//			    String fileChosen = scanner.next();
//			    
//			    //If choice is in the list given, retrieve file and write to workingDir/CLIENTCOPY_filename"
//			    if(Arrays.asList(fileList).contains(fileChosen)){		    	
//			    	//Get full file server filepath of file needed from directory server
//			    	String filepath = DServerStub.getFilePath(currentDir, fileChosen);
//			    	
//			    	//Retrieve file from file server:
//			    	byte[] fileBytes = FServerStub.retrieveFile(filepath);
//			    	
//			    	LServerStub.getLock(filepath, clientName);
//			    	
//			    	if(FServerStub.overwriteFile(fileBytes, filepath, clientName)){
//			    		System.out.println("File " + fileChosen + " overwritten!");
//			    	}
//			    	else{
//			    		System.out.println("Overwrite failed!");
//			    	}
//			    	
//			    	//Deserialise byte[] into file on client
//			    	String localPath = (workingDir + "/CLIENTCOPY_" + fileChosen);
//			    	Utils.getUtils().deSerialiseFile(fileBytes, localPath);
//				    
//				    
//			    }
//			    else{
//			    	System.out.println("File specified is not available.");
//			    }
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
