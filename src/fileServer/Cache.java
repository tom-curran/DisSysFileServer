package fileServer;

import java.io.File;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Date;

public class Cache {
	private int cacheSize = 3;
	
	private File files[] = new File[cacheSize];	
	private Date dates[] = new Date[cacheSize];
	
	public Cache(){
		for(int i=0; i < cacheSize; i++){
			files[i] = null;
			dates[i] = null;
		}
	}
	
	public void addToCache(File in, Date inDate){
		boolean inCache = false;
		int oldest = 0;
		
		for(int i=0; i < cacheSize; i++){
			if(files[i] != null && files[i].getName().equals(in.getName())){
				inCache = true;
				dates[i] = inDate;
				break;
			}
			if(dates[oldest] != null && dates[i] != null && dates[i].before(dates[oldest])) oldest = i;
		}
		if(!inCache){
			//Find oldest, evict, add new file to cache
			if(files[oldest] != null) files[oldest].delete();
			files[oldest] = in;
			dates[oldest] = inDate;
		}
	}
	
	public boolean checkValidInCache(File check){
		boolean inCache = false;
		int cachePos = 0;
		
		for(int i=0; i < cacheSize; i++){
			if(files[i] != null && files[i].getName().equals(check.getName())){
				inCache = true;
				cachePos = i;
				break;
			}
		}
		if(!inCache) return false;
		
		//Contact directory server to check timestamp
		try{
			Registry registry = LocateRegistry.getRegistry(null);
			DServerRMI DServerStub = (DServerRMI) registry.lookup("DirectoryServer");
			
			return DServerStub.checkTimestamp(files[cachePos].getName(), dates[cachePos]);
		}catch(Exception e){
			return false;
		}
	}
}
