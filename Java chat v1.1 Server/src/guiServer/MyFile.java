package guiServer;
import java.io.File;
import java.util.Map;
import java.util.HashMap;


public class MyFile extends File{
	
	static private long nextAvailableId = 0;
	static public Map<Long, Long> numberOfLinkedClients = new HashMap<Long, Long>(); 
	
	private final long id;
	
	public MyFile(String pathname){
		
		super(pathname);
		id = nextAvailableId++;
	}
	
	public long getID(){
		return id;
	}

}
