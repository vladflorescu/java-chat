package guiClient;

public class GUIClient1_1{
	
	public static void main (String[] args) {
		
		try {
			
			(new Thread(new ClientFrame1_1())).start();
			
		} catch (Exception e) {

			e.printStackTrace();
		}
		
	}
	
}