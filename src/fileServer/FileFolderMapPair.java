package fileServer;

import java.util.HashMap;

public class FileFolderMapPair {

	public HashMap<String, String> files = new HashMap<String, String>();
	public HashMap<String, FileFolderMapPair> folders = new HashMap<String, FileFolderMapPair>();
}
