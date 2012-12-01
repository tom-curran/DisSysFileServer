package fileServer;

public class LockServer implements LServerRMI{
	
	public String test(){
		return "x";
	}
	
	public boolean getLock(String filepath){
		return true;
	}

}
