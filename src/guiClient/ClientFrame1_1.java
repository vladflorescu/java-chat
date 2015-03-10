package guiClient;		
						//daca selectez din main, pierd auto-scrollul

import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.border.EmptyBorder;
import javax.swing.JTextArea;
import javax.swing.JButton;
import javax.swing.border.LineBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultCaret;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import javax.swing.JScrollPane;

import vlad.utilities.*;

public class ClientFrame1_1 extends JFrame implements Runnable{
	

	private static final long serialVersionUID = 101;
	private static final String LINK_ATTRIBUTE = "linkAtt";
	private static final int BUFFER_SIZE = 65536;
	private String client;
	private Socket socket;
	private boolean isConnected = false;
	private String specialString = new String("™¼σ¼™");
	private DataInputStream dis = null;
	private DataOutputStream dos = null;

	private class LoginWindow extends JFrame implements Runnable{
		
		private static final long serialVersionUID = 102;
		private Thread thisThread = Thread.currentThread();
		
		public LoginWindow() throws Exception{
			
			setTitle("Vlad Florescu Messenger v1.1");
			System.setProperty("apple.awt.fileDialogForDirectories", "false");
			
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			setBounds(500, 250, 400, 135);
			startWindowContentPane = new JPanel();
			startWindowContentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
			setContentPane(startWindowContentPane);
			startWindowContentPane.setLayout(null);
			
			startWindowUsernameLabel = new JLabel("Insert your username");
			startWindowUsernameLabel.setBounds(20, 15, 250, 30);
			startWindowContentPane.add(startWindowUsernameLabel);
			
			startWindowUsernameTextField = new JTextField();
			startWindowUsernameTextField.setBounds(20, 40, 250, 30);
			startWindowUsernameTextField.addKeyListener(new StartingFrameActionEvent());
			startWindowContentPane.add(startWindowUsernameTextField);
			
			startWindowConnectButton = new JButton("Connect");
			startWindowConnectButton.setBounds(280, 40, 90, 30);
			startWindowConnectButton.addActionListener(new StartingFrameActionEvent());
			startWindowContentPane.add(startWindowConnectButton);
			
			setVisible(true);
			setResizable(false);
			startWindowUsernameTextField.requestFocus();
		}
		
		class StartingFrameActionEvent implements ActionListener, KeyListener{
			
			public void actionPerformed(ActionEvent e){
				
				try {
					
					if(startWindowUsernameTextField.getText().equals("")){
					
						JOptionPane.showMessageDialog(null, "Please insert an username.", "Empty username field", JOptionPane.ERROR_MESSAGE);
						return;
					}
					
					
					socket = new Socket("vladflorescuserver.ddns.net",36705);
					client = startWindowUsernameTextField.getText();//.toLowerCase();
					isConnected=true;
					
					dispose();
					thisThread.interrupt();
					
				} catch (Exception ex) {
					
					JOptionPane.showMessageDialog(null, "Can't connect to server. Try again!", "Connection Error", JOptionPane.ERROR_MESSAGE);
				}
			}

			@Override
			public void keyPressed(KeyEvent e) {
								
				if(e.getKeyCode() == KeyEvent.VK_ENTER){
					
					startWindowConnectButton.doClick();
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				
			}

			@Override
			public void keyTyped(KeyEvent e) {
			
				if(startWindowUsernameTextField.getText().length()>17 || e.getKeyChar() == KeyEvent.VK_SPACE){
					e.consume();
				}
								
			}	
		}
		
		public void run(){
			
			Scanner sc = new Scanner(System.in);
			while(sc.nextLine().equals("exit")==false){		//you can close the startWindow from the console
				
			}
			sc.close();
			dispose();
				
		}
		
		private JPanel startWindowContentPane;
		private JTextField startWindowUsernameTextField;
		private JLabel startWindowUsernameLabel;
		private JButton startWindowConnectButton;
	}

//End of StartWindow class
//-------------------------------------------------------------------------------------------------------------------------------------		
	
	/**
	 * Creation of the frame.
	 */
	public ClientFrame1_1() throws Exception{
		
		while(true){ // while the name isn't valid
			
			Thread startWindowThread = new Thread(new LoginWindow());
			startWindowThread.start();
			
			try{
				startWindowThread.join();		//wait the startWindow
			} catch(InterruptedException e){
				
				//We need to interrupt the first Window's thread
			}
			
			if(isConnected==false)
				System.exit(0);
			
			dis = new DataInputStream(socket.getInputStream());
			dos = new DataOutputStream(socket.getOutputStream());
			dos.writeUTF(client+specialString+" "+specialString+"connect");
			
			boolean response = dis.readBoolean();		//the server tells us if the name is available  
			if(response==true){
				break;
			}
			else{
				JOptionPane.showMessageDialog(null, "This username is already in use. Choose another one !", "SignUp Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		//Here starts the construction of the main frame
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 600, 380);
		
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new GridBagLayout());		//I'm going to use a GridBagLayout
														//I let first row of the Layout empty for using it eventually for sth else (a menu ?)
		
		clientsTextArea = new JTextArea();
		clientsTextArea.setEditable(false);
		clientsTextArea.setMinimumSize( new Dimension(114, 200));
		clientsTextArea.setPreferredSize( new Dimension(114, 200));
		clientsTextArea.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
		clientsTextArea.setFont(new Font("Times New Roman", Font.BOLD, 11));
		
		GridBagConstraints ctaGbc = 
				new GridBagConstraintsBuilder().gridx(2).gridy(1).fill(GridBagConstraints.VERTICAL).insets(new Insets(10, 0, 10, 0)).build();
		contentPane.add(clientsTextArea, ctaGbc); 
		
		
		buttonsPanel = new JPanel(new GridBagLayout());
		
		sendButton = new JButton("Send");
		sendButton.setMinimumSize( new Dimension(114, 33));
		sendButton.setPreferredSize( new Dimension(114, 33));
		sendButton.addActionListener( new ButtonActionListener());

		shareFileButton = new JButton("Share file...");
		shareFileButton.setMinimumSize( new Dimension(114, 33));
		shareFileButton.setPreferredSize( new Dimension(114, 33));
		shareFileButton.addActionListener( new ButtonActionListener());
//		shareFileButton.setEnabled(false);		//to modify

		buttonsPanel.add(sendButton, new GridBagConstraints(0, 0, 3, 1, 0, 0, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 10, 0), 0, 0));
		buttonsPanel.add(shareFileButton, new GridBagConstraints(0, 2, 3, 1, 0, 1, GridBagConstraints.FIRST_LINE_START, GridBagConstraints.NONE, new Insets(0, 0, 0, 0), 0, 0));
		
		GridBagConstraints bpGbc = 
				new GridBagConstraintsBuilder().gridx(2).gridy(2).insets(new Insets(10, 0, 0, 0)).fill(GridBagConstraints.VERTICAL).weighty(0.25).build();
		contentPane.add(buttonsPanel, bpGbc);
		
		
		inputTextArea = new JTextArea();
		inputTextArea.setMinimumSize(new Dimension(112, 60));
		inputTextArea.setPreferredSize(new Dimension(112, 60));
		inputTextArea.setFont(new Font("Courier New", Font.PLAIN, 12));
		inputTextArea.setLineWrap(true);
		inputTextArea.setBorder(BorderFactory.createLineBorder(new Color(0,0,0)));
		inputTextArea.addKeyListener(new InputKeyActionListener())	;
		
		GridBagConstraints itaGbc = 
				new GridBagConstraintsBuilder().gridx(0).gridy(2).gridwidth(2).weighty(0.25).fill(GridBagConstraints.BOTH).insets(new Insets(10,10,5,10)).build();
		contentPane.add(inputTextArea, itaGbc);
		
		
		chatTextPane = new JTextPane();				
		chatTextPane.setEditable(false);
		chatTextPane.setBorder(new LineBorder(new Color(0,0,0)));
		chatTextPane.addMouseListener(new MouseClickListener());
		
		styledDocumentOfChatPane = chatTextPane.getStyledDocument();
		
		scrollPane = new JScrollPane();
		scrollPane.setViewportView(chatTextPane);	//this activates wrap only after a word is finished
		scrollPane.setMinimumSize(new Dimension(250, 180));
		scrollPane.setPreferredSize(new Dimension(250, 180));

		
		GridBagConstraints spGbc =
				new GridBagConstraintsBuilder().gridx(0).gridy(1).gridwidth(2).weightx(1).weighty(0.75).fill(GridBagConstraints.BOTH).insets(new Insets(10,10,10,10)).build();
		contentPane.add(scrollPane, spGbc);
		///
		DefaultCaret caret = (DefaultCaret)chatTextPane.getCaret();		
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);					//here we activate the auto-scroll for chatPane
		
		setVisible(true);
		this.setMinimumSize(this.getMinimumSize());
		this.setTitle(client+" - Vlad Florescu Messenger v1.1");
		inputTextArea.requestFocus();
		

	}
	
	private class FileTransferRunnable implements Runnable{
		
		private Socket transferSocket;
		private DataInputStream transferDis;
		private DataOutputStream transferDos;
		private String fileOwnerName;
		private File file;
		private FileInputStream fis;
		
		public FileTransferRunnable(String fileOwnerName, String filePath){
			
			this.fileOwnerName = fileOwnerName;
			this.file = new File(filePath);
		}
		
		public void run(){
			
			transferSocket = null;
			
			try {
				transferSocket = new Socket("vladflorescuserver.ddns.net", 36705);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				ObjectCloser.closeSocket(transferSocket);
				Thread.currentThread().interrupt();
			}
			
			try {
				transferDis = new DataInputStream( transferSocket.getInputStream());
				transferDos = new DataOutputStream( transferSocket.getOutputStream());
				
			} catch (IOException e) {
				
					e.printStackTrace();
					if(transferDis != null){		//	I'm checking only if the DataInputStream was opened, because if the DataOutputStream
													//had been opened this error wouldn't have occurred. 
					
						ObjectCloser.closeInputStream(transferDis);
					}
				
				ObjectCloser.closeSocket(transferSocket);
				Thread.currentThread().interrupt();
			}
			
			try {
				
				transferDos.writeUTF(fileOwnerName+ specialString+ file.length()+ specialString+ file.getName()+ specialString+ "sendFile" );
				transferDis.readInt();

			} catch (IOException e) {
				
				e.printStackTrace();
				ObjectCloser.closeInputStream(transferDis);
				ObjectCloser.closeOutputStream(transferDos);
				ObjectCloser.closeSocket(transferSocket);
				Thread.currentThread().interrupt();
			}
			
			byte[] buffer = new byte[BUFFER_SIZE];
			
			fis = null;
			
			try {
				fis = new FileInputStream(file);
				int bytesRead;
				
				while((bytesRead = fis.read(buffer)) > 0){
					
					transferDos.write(buffer, 0, bytesRead);
				}
				
				
			} catch (FileNotFoundException e) {

				handleExceptionCloseEverythingInterruptThread(e);
				
			} catch (IOException e) {
				
				handleExceptionCloseEverythingInterruptThread(e);

			}
			
			ObjectCloser.closeInputStream(fis);
			ObjectCloser.closeInputStream(transferDis);
			ObjectCloser.closeOutputStream(transferDos);
			ObjectCloser.closeSocket(transferSocket);
		}
		
		private void handleExceptionCloseEverythingInterruptThread(Exception e){
			
			e.printStackTrace();
			ObjectCloser.closeInputStream(fis);
			ObjectCloser.closeInputStream(transferDis);
			ObjectCloser.closeOutputStream(transferDos);
			ObjectCloser.closeSocket(transferSocket);
			Thread.currentThread().interrupt();
		}
		
	}

	private class FileRequestRunnable implements Runnable{
	
		Socket downloadSocket ;
		DataInputStream  downloadDis;
		DataOutputStream downloadDos;
		FileOutputStream fos;
		FileLink fileLink;
		File outputFile;
		
		public FileRequestRunnable(FileLink fileLink, String filePath){
			
			this.fileLink = fileLink;
			outputFile = new File(filePath);
		}
		
		public void run(){
			
			downloadSocket = null;
			 	
			try {
				downloadSocket = new Socket("vladflorescuserver.ddns.net", 36705);
				
			} catch (IOException e) {
				
				e.printStackTrace();
				ObjectCloser.closeSocket(downloadSocket);
				Thread.currentThread().interrupt();
			}
			
			try {
				downloadDis = new DataInputStream( downloadSocket.getInputStream());
				downloadDos = new DataOutputStream( downloadSocket.getOutputStream());
				
			} catch (IOException e) {
				
					e.printStackTrace();
					if(downloadDis != null){		//	I'm checking only if the DataInputStream was opened, because if the DataOutputStream
													//had been opened, this error wouldn't have occurred. 
					
						ObjectCloser.closeInputStream(downloadDis);
					}
				
				ObjectCloser.closeSocket(downloadSocket);
				Thread.currentThread().interrupt();
			}
			
			long remainingSize = -1;
			
			try {
				downloadDos.writeUTF((fileLink.fileOwner+'/'+fileLink.fileName)+ specialString+ "requestFile");
				remainingSize = downloadDis.readLong();
				
			} catch (IOException e) {
				
				e.printStackTrace();
				ObjectCloser.closeInputStream(downloadDis);
				ObjectCloser.closeOutputStream(downloadDos);
				ObjectCloser.closeSocket(downloadSocket);
				Thread.currentThread().interrupt();
			}
			
			fos = null;
			
			try {
				fos = new FileOutputStream(outputFile);
				
			} catch (FileNotFoundException e) {
				
				handleExceptionCloseEverythingInterruptThread(e);
			}
			
			byte buffer[] = new byte[BUFFER_SIZE];
			long readBytes;
			
			while(remainingSize > 0){
				
				try{
					readBytes = downloadDis.read(buffer);	
					fos.write(buffer, 0, (int)readBytes);
					remainingSize -= readBytes;
					
				} catch(IOException e){
					
					e.printStackTrace();
					ObjectCloser.closeOutputStream(fos);
					outputFile.delete();	//deleting the file if the download was unsuccessful
					ObjectCloser.closeInputStream(downloadDis);
					ObjectCloser.closeOutputStream(downloadDos);
					ObjectCloser.closeSocket(downloadSocket);
					Thread.currentThread().interrupt();
				}
			}
			
			ObjectCloser.closeOutputStream(fos);
			ObjectCloser.closeInputStream(downloadDis);
			ObjectCloser.closeOutputStream(downloadDos);
			ObjectCloser.closeSocket(downloadSocket);
		}
		
		private void handleExceptionCloseEverythingInterruptThread(Exception e){
			
			e.printStackTrace();
			ObjectCloser.closeOutputStream(fos);
			ObjectCloser.closeInputStream(downloadDis);
			ObjectCloser.closeOutputStream(downloadDos);
			ObjectCloser.closeSocket(downloadSocket);
			Thread.currentThread().interrupt();
		}
		
	}
	
	
	private class ButtonActionListener implements ActionListener{
		
		@Override
		public void actionPerformed(ActionEvent ev){
			
			if(ev.getSource() == sendButton){
				
				if(inputTextArea.getText().equals("")){
					
				}
				else{
					System.out.println(inputTextArea.getText());
					String message = client+specialString+inputTextArea.getText()+specialString+"chat";
					sendMessageToServer(message);
					inputTextArea.setText("");
				}
				
				inputTextArea.requestFocus();
			}
			else if(ev.getSource() == shareFileButton){
				
				FileDialog fd = new FileDialog(ClientFrame1_1.this, "Choose a file", FileDialog.LOAD);	//getting the full path of the desired file
				fd.setDirectory(" ");
				fd.setVisible(true);
				if(fd.getFile()!=null){		//if client selected a file
					
					String filePath = fd.getDirectory() + fd.getFile();
				
					Thread fileTransferThread = new Thread(new FileTransferRunnable(client, filePath));
					fileTransferThread.start();
				}
			}
		}
	}
	
	private class InputKeyActionListener implements KeyListener{

		@Override
		public void keyPressed(KeyEvent e) {
			
			if(e.getKeyChar() == KeyEvent.VK_ENTER){
				
				e.consume();
				sendButton.doClick();
			}
		}

		@Override
		public void keyReleased(KeyEvent e) {
			
			if(e.getKeyChar() == KeyEvent.VK_ENTER)
				inputTextArea.setText("");
		}

		@Override
		public void keyTyped(KeyEvent e) {

			if(inputTextArea.getText().length()>160){
				
				e.consume();
				inputTextArea.setText(inputTextArea.getText().substring(0, 160));
			}
		}
		
	}
	
	private class MouseClickListener extends MouseAdapter{			
		
		public void mouseClicked( MouseEvent ev){
			
			Element elem = styledDocumentOfChatPane.getCharacterElement( chatTextPane.viewToModel( ev.getPoint() ) );
			FileLink fle = (FileLink) elem.getAttributes().getAttribute(LINK_ATTRIBUTE);

			if(fle!=null){
				
				String fileExtension = (fle.fileName).substring((fle.fileName).lastIndexOf('.'));
				
				FileDialog fd = new FileDialog(ClientFrame1_1.this, "Choose your location", FileDialog.SAVE);
				fd.setDirectory(" ");
				fd.setFile(fle.fileName + fileExtension);
			
				fd.setVisible(true);
				if(fd.getFile()!=null){
					
					(new Thread(new FileRequestRunnable(fle, fd.getDirectory()+fd.getFile()))).start();
				}
			}
		}
		
		
	}
	
	private class FileLink{
		
		public final String fileOwner, fileName;
		
		public FileLink(String fileOwner, String fileName){
			
			this.fileOwner = fileOwner;
			this.fileName  = fileName;
		}
			
	}

//------------------------------------------------------------------------------------------------------------------------------------------
/**
 * 
 * Methods
 */
	private void sendMessageToServer(String message){
		
		try {
			dos.writeUTF(message);
		} catch (IOException e) {
			System.out.println("Message couldn't be sent to the server");
		}
	}
	
	private void printWithTwoStyles(SimpleAttributeSet firstStyle, String firstText, SimpleAttributeSet secondStyle, String secondText){
		
		try{
			
			styledDocumentOfChatPane.insertString(styledDocumentOfChatPane.getLength() , firstText, firstStyle); //print name in chatPane
			styledDocumentOfChatPane.insertString(styledDocumentOfChatPane.getLength() , secondText, secondStyle); //print message
		}
		catch(BadLocationException e){
			
			e.printStackTrace();
		
		}
	}		
	
//------------------------------------------------------------------------------------------------------------------------------------------
/**
 * 
 *  run() method
 */
	public void run(){
		
		String serverResponse;
		String[] responseParts = new String[2];
		try {
			
			while((serverResponse=dis.readUTF())!=null){
				
				responseParts = serverResponse.split(specialString);
				
				if(responseParts[responseParts.length - 1].equals("print")){	//not necessarily used
					
					try {
						
						styledDocumentOfChatPane.insertString(styledDocumentOfChatPane.getLength(), responseParts[0]+"\n", null);
						
					} catch (BadLocationException e) {
						e.printStackTrace();
					}
					
				}
				else if(responseParts[responseParts.length - 1].equals("printWithClientName")){
					
					if(responseParts[1].equals("has connected.")){			//if it is the system message that an user has connected
						
						SimpleAttributeSet attributeSet = new SimpleAttributeSet();
						StyleConstants.setForeground(attributeSet, Color.BLUE);
						StyleConstants.setItalic(attributeSet, true);
						StyleConstants.setFontSize(attributeSet, 11);
						
						SimpleAttributeSet nameAttributeSet = new SimpleAttributeSet(attributeSet);
						StyleConstants.setBold(nameAttributeSet, true);
						
						printWithTwoStyles(nameAttributeSet, responseParts[0], attributeSet, " has connected.\n");
						
					}
					else{	
						
						SimpleAttributeSet nameAttributeSet = new SimpleAttributeSet();
						StyleConstants.setBold(nameAttributeSet, true);
						
						if(responseParts[0].equals(client)){	//if the message is sent by the current client
							StyleConstants.setForeground(nameAttributeSet, new Color(0,35,176));
							responseParts[0] = "Me";
						}
						else{
							StyleConstants.setForeground(nameAttributeSet, Color.RED);
						}
						
						printWithTwoStyles(nameAttributeSet, responseParts[0], null, " : " + responseParts[1] + "\n");
					}
					
				}
				
				else if(responseParts[responseParts.length - 1].equals("updateClientsList")){
					
					String[] clientsList = responseParts[0].split(" ");
					Arrays.sort(clientsList);
					
					clientsTextArea.setText("");    // clearing the textArea for reinitializing
					
					for(String s:clientsList){
						clientsTextArea.append(s+'\n');
					}	
				}
				
				else if(responseParts[responseParts.length - 1].equals("hasDisconnected")){
					
					SimpleAttributeSet attributeSet = new SimpleAttributeSet();
					StyleConstants.setForeground(attributeSet, Color.GRAY);
					StyleConstants.setItalic(attributeSet, true);
					StyleConstants.setFontSize(attributeSet, 11);
					StyleConstants.setAlignment(attributeSet, StyleConstants.ALIGN_RIGHT);
					
					SimpleAttributeSet nameAttributeSet = new SimpleAttributeSet(attributeSet);
					StyleConstants.setBold(nameAttributeSet, true);
					
					try{
						
						styledDocumentOfChatPane.insertString(styledDocumentOfChatPane.getLength(), responseParts[0], nameAttributeSet); //print name in chatPane
						styledDocumentOfChatPane.insertString(styledDocumentOfChatPane.getLength(), " has disconnected. \n", attributeSet); //print message
					}
					catch(BadLocationException e){
						
						e.printStackTrace();
					}
				}
				
				else if(responseParts[responseParts.length - 1].equals("linkFile")){
					
					String fileOwner = responseParts[0];
					String fileName = responseParts[1];
					
					SimpleAttributeSet nameAttributeSet = new SimpleAttributeSet();
					StyleConstants.setBold(nameAttributeSet, true);
					
					SimpleAttributeSet linkAttributeSet = new SimpleAttributeSet();
					StyleConstants.setBold(linkAttributeSet, true);
					StyleConstants.setItalic(linkAttributeSet, true);
					StyleConstants.setForeground(linkAttributeSet, Color.BLUE);
					linkAttributeSet.addAttribute(LINK_ATTRIBUTE, new FileLink(fileOwner, fileName));
					
					printWithTwoStyles(nameAttributeSet, fileOwner, null, " shared a file: ");
					try {
						styledDocumentOfChatPane.insertString(styledDocumentOfChatPane.getLength(), fileName+"\n", linkAttributeSet);
						
					} catch (BadLocationException ble) {
						
						ble.printStackTrace();
					}
					
				}
		
			}
			
		} catch (IOException e) {
			
			e.printStackTrace();
		}
		
	}
	
//------------------------------------------------------------------------------------------------------------------------------------------
/**
 * 
 *  Fields
 */
	private JPanel contentPane;
	private JTextArea clientsTextArea;
	private JTextArea inputTextArea;
	private JTextPane chatTextPane;
	private StyledDocument styledDocumentOfChatPane;
	private JButton sendButton;
	private JButton shareFileButton;
	private JPanel buttonsPanel;
	private JScrollPane scrollPane;
	
}
