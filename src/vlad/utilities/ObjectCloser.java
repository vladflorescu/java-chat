package vlad.utilities;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class ObjectCloser {

	static public void closeInputStream(InputStream is){
	
		try {
			is.close();
		} catch (IOException e) {
			
			e.printStackTrace();
			System.err.println("An input stream couldn't be closed.");
		}
	}
	
	static public void closeOutputStream(OutputStream os){
		
		try{
			os.close();
		} catch (IOException e){
			
			e.printStackTrace();
			System.err.println("An output stream couldn't be closed.");
		}
	}
	
	static public void closeSocket(Socket s){
		
		try {
			s.close();
		} catch (IOException e) {
			
			e.printStackTrace();
			System.err.println("A socket couldn't be closed");
		}
	}
	
}
