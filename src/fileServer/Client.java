package fileServer;

//import java.rmi.RMISecurityManager;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
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
    private DServerRMI DServerStub;
    private LServerRMI LServerStub;
    
    private Cache myCache = new Cache();
    
    //Constructor(s)
	public Client(){}
	public Client(String workspace){workingDir = workspace;}
	
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
		
		//Set code base so RMI can see the classes (classpath)
		System.setProperty("java.rmi.server.codebase", DServerRMI.class.getProtectionDomain().getCodeSource().getLocation().toString());
		
		//Run test client
		Client client = new Client("C:/Users/Tom/Desktop/Servers/Client");		
		//client.runTestClient();
		client.mainMenu();	
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
						String crumbs[] = currentDir.split("/");
						if(crumbs.length == 0 && crumbs[0].equals("")) System.out.println("Already at root");
						else{
							currentDir = crumbs[0];
							for(int i=1; i < crumbs.length-1; i++){
								currentDir += ("/" + crumbs[i]);
							}
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
				System.out.print("File to edit: ");
				String gedit = scanner.next();
				
				//Check cache
				File cachedFile = new File(workingDir + "/cache/" + gedit);
				if(cachedFile.exists() && myCache.checkValidInCache(cachedFile)){
					//Can read from cache, need to lock
					System.out.println("Reading from cache");
					try{
						String serverFilepath = DServerStub.getFilePath(currentDir, gedit);
						if(serverFilepath == null) System.out.println("File not available");
						else{
							//Get lock on file
							if(!LServerStub.getLock(serverFilepath, clientName)){
								System.out.println("File not available");
							}
							else{
								//EDIT FILE
								ArrayList<String> lines = new ArrayList<String>();
								FileInputStream fis = new FileInputStream(workingDir + "/cache/" + gedit);
								BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
								String line;
								while((line = br.readLine()) != null){
									System.out.println(line);
									System.out.print("1-Keep Line, 2-Edit Line, 3-Delete Line");
									int chk=0;
									while(chk != 1 && chk != 2 && chk != 3) chk = scanner.nextInt();
									switch(chk){
									case 1:
										lines.add(line);
										break;
									case 2:
										System.out.print("Type new line: ");
										String newL = scanner.next();
										lines.add(newL);
										break;
									case 3:
										break;
									}
								}
								br.close();
								fis.close();
								
								File tempFile = new File(workingDir + "/cache/" + gedit); 
								FileWriter fstream = new FileWriter(tempFile);
								BufferedWriter out = new BufferedWriter(fstream);
								
								for(int i=0; i<lines.size(); i++){
									//Write line to file
									out.write(lines.get(i) + "\n");
								}
								out.close();
								fstream.close();
								
								//WRITE TO FILE SERVER
								FServerStub.overwriteFile(Utils.getUtils().serialiseFile(workingDir + "/cache/" + gedit), serverFilepath, clientName);
								
								//Update cache and last edit time on Directory server
								Date now = new Date();
								DServerStub.updateTimestamp(gedit, now);
								myCache.addToCache(new File(workingDir + "/cache/" + gedit), now);
							}
						}
					}catch(Exception e){
						System.err.println("Could not contact servers");
					}
					
				}
				else{
					System.out.println("Retrieving from server");
					try{
						String serverFilepath = DServerStub.getFilePath(currentDir, gedit);
						if(serverFilepath == null) System.out.println("File not available");
						else{
							//Get lock on file
							if(!LServerStub.getLock(serverFilepath, clientName)){
								System.out.println("File not available");
							}
							else{
								//Copy file to local directory
								byte copiedFile[] = FServerStub.retrieveFile(serverFilepath);
								Utils.getUtils().deSerialiseFile(copiedFile, (workingDir + "/cache/" + gedit));	//Copied to cache, must write again when editing is finished								
								
								//EDIT FILE
								ArrayList<String> lines = new ArrayList<String>();
								FileInputStream fis = new FileInputStream(workingDir + "/cache/" + gedit);
								BufferedReader br = new BufferedReader(new InputStreamReader(fis, Charset.forName("UTF-8")));
								String line = "";
								while((line = br.readLine()) != null){
									System.out.println(line);
									System.out.print("1-Keep Line, 2-Edit Line, 3-Delete Line");
									String chk="";
									while(!chk.equals("1") && !chk.equals("2") && !chk.equals("3")) chk = scanner.next();
									if(chk.equals("1")) lines.add(line);
									else if(chk.equals("2")){
										System.out.print("Type new line: ");
										String newL = scanner.next();
										lines.add(newL);
									}
								}
								br.close();
								fis.close();
								
								File tempFile = new File(workingDir + "/cache/" + gedit); 
								FileWriter fstream = new FileWriter(tempFile);
								BufferedWriter out = new BufferedWriter(fstream);
								
								for(int i=0; i<lines.size(); i++){
									//Write line to file
									out.write(lines.get(i) + "\n");
								}
								out.close();
								fstream.close();						
								
								//WRITE TO FILE SERVER
								FServerStub.overwriteFile(Utils.getUtils().serialiseFile(workingDir + "/cache/" + gedit), serverFilepath, clientName);
								
								//Update cache and last edit time on Directory server
								Date now = new Date();
								DServerStub.updateTimestamp(gedit, now);
								myCache.addToCache(new File(workingDir + "/cache/" + gedit), now);
							}
						}
					}
					catch(Exception e){
						System.err.println("Exception editing file " + e.toString());
				    	e.printStackTrace();
					}
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
					
					String filename;
					
					do{
						System.out.print("Enter name to save file as: ");
						filename = scanner.next();
						filename += ".txt";
						//Check file doesn't already exist on file server
					}while(DServerStub.getFilePath(currentDir, filename) != null);
						
					File finalFile = new File(workingDir + "/cache/" + filename);
					
					if(!tempFile.renameTo(finalFile)){
						System.err.println("Error renaming!");
					}
					else tempFile.delete();
					
					byte serFile[] = Utils.getUtils().serialiseFile(workingDir + "/cache/" + filename);
					FServerStub.writeNewFile(serFile, filename, currentDir);
					
					Date now = new Date();
					DServerStub.updateTimestamp(filename, now);
					myCache.addToCache(new File(workingDir + "/cache/" + filename), now);
					
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
				try{
					LServerStub.removeName(clientName);
				}catch(Exception e){					
				}
				exit = true;
				break;
			}			
		}
	}
}
