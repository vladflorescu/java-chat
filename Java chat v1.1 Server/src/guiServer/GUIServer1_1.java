package guiServer;			//check if you closed everything here and in the Client !

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import vlad.utilities.*;

public class GUIServer1_1 {
	
	private static final int BUFFER_SIZE = 65536;
	
	private static ServerSocket serverSocket;
	private static ArrayList<DataOutputStream> clientsDataOutputStreams;
	private static String specialString = new String("™¼σ¼™");
	private static Set<Client> clientsHash
			= Collections.newSetFromMap(new ConcurrentHashMap<Client, Boolean>());	//I'm using an concurrent HashSet

//---------------------------------------------------------------------------------------------------------------------------------------------
	private static class Client{
		
		public String name;
		private ArrayList<File> availableFiles ;
		
		public Client(String name){
			
			this.name = name;
			availableFiles = new ArrayList<File>();
		}
		
		public void addToAvailableFiles(File file){
			
			availableFiles.add(file);
		}
	}
	
	private static class ClientHandler implements Runnable{
		
		private Socket clientSocket;
		private Client client;
		private DataInputStream  dis;
		private DataOutputStream dos;
		boolean successfulConnection;
		
		public ClientHandler(Socket socket){
			clientSocket= socket;
		}
		
		public void run(){
						
			String message;
			dis = null;
			dos = null;
			
			try{
				
				String[] received;
				dis = new DataInputStream(clientSocket.getInputStream());
				
				while((message = dis.readUTF())!= null){
					
					received = message.split(specialString);
					
					String command = received[received.length - 1];
					
					if(command.equals("connect")){
						
						
						String newConnectedNameLC = received[0].toLowerCase();
						
						if(newConnectedNameLC.equals("me")){					//clients can't choose their name "me"
							
							dos = new DataOutputStream(clientSocket.getOutputStream());
							dos.writeBoolean(false);
							return;
						}
						
						for(Client connectedClient : clientsHash){			//if that name is already used (even with different casing)
							
							if(newConnectedNameLC.equals(connectedClient.name.toLowerCase())){
								
								dos = new DataOutputStream(clientSocket.getOutputStream());
								dos.writeBoolean(false);
								return;
							}
						}
						
						successfulConnection = true;
						System.out.println("An user connected");
						client = new Client(received[0]);
						sendMessageWithClientName(received[0], "has connected.");  
						dos= new DataOutputStream(clientSocket.getOutputStream()); // adding the client's DataOutputStream to the array
						dos.writeBoolean(true); //telling the client his name is valid
						clientsDataOutputStreams.add(dos);
						
						clientsHash.add(client);
						updateClientsLists();
						
						
					}
					else if(command.equals("chat")){
						
						System.out.println("An user sent a message");
						sendMessageWithClientName(received[0], received[1]);
					}
					
					else if(command.equals("disconnect")){
						
						System.out.println("An user has disconnected");
						disconnectAndSendMessage(clientSocket, dis, dos);
						return;
					}
					else if(command.equals("sendFile")){		//"sendFile" message is sent using a fileTransfer pseudo-client
												
						dos = new DataOutputStream(clientSocket.getOutputStream());
						long remainingSize = Long.parseLong(received[1]);
						String fileOwner = new String(received[0]);
						
						File clientDirectory = new File("downloads/"+ fileOwner);
						if(!clientDirectory.isDirectory()){
							try{
								if(clientDirectory.mkdirs() == false){
									throw new Exception("Client's uploads directory couldn't be created");
								}
							}catch(Exception e){
								System.err.println(e.getMessage());
								e.printStackTrace();
								ObjectCloser.closeInputStream(dis);
								ObjectCloser.closeOutputStream(dos);
								ObjectCloser.closeSocket(clientSocket);
								Thread.currentThread().interrupt();
							}
						}
						
						MyFile file = new MyFile("downloads/"+ fileOwner+ "/"+ received[2]);
						FileOutputStream fos = new FileOutputStream(file);	//to close
												
						dos.writeInt(0);	//sending a response to the fileTransfer client, that the server is ready for receiving the file
						
						byte[] bytes = new byte[BUFFER_SIZE];
						long readBytes;
						long startTime = System.currentTimeMillis();
						System.out.println("An user started to upload a file");
						
						while(remainingSize > 0){			
							
							try{ 
								readBytes = dis.read(bytes);	
								fos.write(bytes, 0, (int)readBytes);
								remainingSize -= readBytes;
								
							} catch(IOException e){
								
								ObjectCloser.closeOutputStream(fos);
								//deleting the file if the upload was unsuccessful
								File parentDirectory = file.getParentFile();
								file.delete();
								
								if(parentDirectory != null){
									
									String[] list = null;
									try{
										list = parentDirectory.list();
										
									}catch(NullPointerException npe){
										
										System.err.println(e.getMessage());
									}
									
									
									if(list!=null)
									
										if(parentDirectory.list().length == 0){
							        	parentDirectory.delete();
							        }
								
								}
								//
								ObjectCloser.closeInputStream(dis);
								ObjectCloser.closeOutputStream(dos);
								ObjectCloser.closeSocket(clientSocket);
								Thread.currentThread().interrupt();
							}
						}
						
						System.out.println("A file was uploaded. Time elapsed: "+ (System.currentTimeMillis() - startTime)/1000 + " seconds. ");
						ObjectCloser.closeOutputStream(fos);
						
						sendLinkToTheConnectedClients(fileOwner, file); //trimitem link cu fisierul userilor
						linkFileToTheConnectedClients(file);
						
						ObjectCloser.closeInputStream(dis);
						ObjectCloser.closeOutputStream(dos);
						ObjectCloser.closeSocket(clientSocket);
						Thread.currentThread().interrupt();
					}
					
					else if(command.equals("requestFile")){
						
						dos = new DataOutputStream(clientSocket.getOutputStream());
						String filePath = new String("downloads/"+received[0]);
						System.out.println(filePath);
						
						File file = new File(filePath);
						FileInputStream fis = new FileInputStream(file);
						
						byte buffer[] = new byte[BUFFER_SIZE];
						int bytesRead;
						dos.writeLong(file.length());
						
						long startTime = System.currentTimeMillis();
						System.out.println("An user started to download a file");
						
						try{
							while((bytesRead = fis.read(buffer)) > 0){
								
								dos.write(buffer, 0, bytesRead);
							}
							
						} catch( IOException e){
							
							ObjectCloser.closeInputStream(fis);
							ObjectCloser.closeInputStream(dis);
							ObjectCloser.closeOutputStream(dos);
							ObjectCloser.closeSocket(clientSocket);
							Thread.currentThread().interrupt();
						}
						
						System.out.println("A file was downloaded. Time elapsed: "+ (System.currentTimeMillis() - startTime)/1000 + " seconds. ");
						
						ObjectCloser.closeInputStream(fis);
						ObjectCloser.closeInputStream(dis);
						ObjectCloser.closeOutputStream(dos);
						ObjectCloser.closeSocket(clientSocket);
						Thread.currentThread().interrupt();
					}
				}
				
				dis.close();
				dos.close();
				
			}catch(IOException e){
				
				if(dos!=null){
					if(successfulConnection==true){
						clientsDataOutputStreams.remove(dos);
						System.out.println("An user has disconnected");
						disconnectAndSendMessage(clientSocket, dis, dos);
					}
											// I think I should close the streams
				}
				else{
					System.out.println("Couldn't acces one of the i/o streams");
				}
			
			}
		}
//---------------------------------------------------------------------------------------------------------------------------------------------
		@SuppressWarnings("unused")
		private void tellEverybody(String message){
			
			for(DataOutputStream d : clientsDataOutputStreams)
				try {
					d.writeUTF(message+specialString+"print");
				} catch (IOException e) {

					e.printStackTrace();
				}
		}
		
		private void sendMessageWithClientName(String clientName, String message){
			
			for(DataOutputStream d : clientsDataOutputStreams)
				try {
					d.writeUTF(clientName + specialString + message + specialString + "printWithClientName");
				} catch (IOException e) {

					e.printStackTrace();
				}
		}
		
		private static void updateClientsLists(){
			
			String clients = new String("");
			for(Client c:clientsHash){
				
				clients = new String(c.name+" "+clients);
			}
			
			if(!clients.equals("")){
				clients = new String(clients.substring(0, clients.length()-1));		//deletes the last space, that we don't need
				System.out.println("Clients connected: " + clients);
			}
			else{
				System.out.println("No more clients connected");
			}
			
			for(DataOutputStream d : clientsDataOutputStreams){
				try{
					
					d.writeUTF(clients+specialString+"updateClientsList");
				}
				catch(IOException e){
					
					e.printStackTrace();
				}
			}
			
		}
		
		private void disconnectAndSendMessage(Socket socket, DataInputStream distream, DataOutputStream dostream){
			
			try{
				
				Client clientToRemove = client;
				if(clientToRemove == null){
					throw new Exception("Invalid name at disconnection.");
				}
				
				clientsHash.remove(client);			//removing Client from hash
			}
			catch(Exception e){
				
				e.getMessage();
				e.printStackTrace();
			}
			
			clientsDataOutputStreams.remove(dostream);
			
			for(File currentFile : client.availableFiles ){
				
				long newValue = MyFile.numberOfLinkedClients.get( ((MyFile)(currentFile)).getID() ) - 1;
				MyFile.numberOfLinkedClients.put(
						((MyFile)currentFile).getID(), newValue);	//decrementing number of connected Clients of every file
							
				if(newValue == 0){		//  if there are no more users linked to this file
										//server will delete the file
	
					File parentDirectory = currentFile.getParentFile();
					currentFile.delete();
					
					String[] list = null;
					try{
						list = parentDirectory.list();
						
					}catch(NullPointerException npe){
						
						System.err.println(npe.getMessage());
					}
					
					
					if(parentDirectory != null){
						
						if(list.length == 0){
				        	parentDirectory.delete();
				        }
					
					}
					
				}
				
			}
	
			for(DataOutputStream d : clientsDataOutputStreams)		//announcing the other clients
				try {
					d.writeUTF(client.name + specialString + "hasDisconnected");
				} catch (IOException e) {

					e.printStackTrace();
				}
					
			ObjectCloser.closeInputStream(distream);
			ObjectCloser.closeOutputStream(dostream);
			ObjectCloser.closeSocket(socket);
			
			updateClientsLists();
		}
		
		private void linkFileToTheConnectedClients(MyFile file){
			
			long numberOfConnectedClients = 0;
			
			for(Client currentClient : clientsHash){
				
				currentClient.addToAvailableFiles(file);
				numberOfConnectedClients++;
			}
				
			MyFile.numberOfLinkedClients.put(file.getID(), numberOfConnectedClients);
			
		}
		
		private void sendLinkToTheConnectedClients(String fileOwner, File file){
			
			for(DataOutputStream d : clientsDataOutputStreams)
				try {
					d.writeUTF(fileOwner + specialString + file.getName() + specialString + "linkFile");
				} catch (IOException e) {

					e.printStackTrace();
				}
		}
		
	}
//---------------------------------------------------------------------------------------------------------------------------------------------
/**
 *  Main function
 */
	
	public static void main(String[] args) throws Exception{
		
    	System.out.println(InetAddress.getLocalHost().getHostAddress());
		clientsDataOutputStreams = new ArrayList<DataOutputStream>();
		serverSocket = new ServerSocket(36705);
		
		while(true){
			Socket socket = serverSocket.accept();
			(new Thread(new ClientHandler(socket))).start();
		}
			
	}
	
}

