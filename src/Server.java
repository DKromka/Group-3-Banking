import java.io.IOException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import javax.swing.filechooser.*;

public class Server {
	//Can you put input/outputStreams to send message objects
	//back and forth server to client... No, that functionality goes in the
	//ClientHandler since there's streams per client
	static private HashMap <String,User> Users;
	static private HashMap <String,BankAccount> Accounts;
	static private HashMap <String,Vector<Log>> Logs;
	
	static private String workingDir;
	
	public static void main(String[] args) {
		
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			public void run() {
				try {
					saveData();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}));
		
		Users = new HashMap<String,User>();
		Accounts = new HashMap<String,BankAccount>();
		Logs = new HashMap <String,Vector<Log>>();
		
		JFrame frame = new JFrame();
		JFileChooser fileselect = new JFileChooser();
		fileselect.setFileFilter(new FileNameExtensionFilter("User data file", "txt"));
		fileselect.setAcceptAllFileFilterUsed(false);
		boolean flag = false;
		while (!flag) {
			int accept = fileselect.showDialog(frame, "Select user data file");
			if (accept == fileselect.APPROVE_OPTION) {
				try {
					workingDir = fileselect.getSelectedFile().getParent().toString();
					initData();
					flag = true;
					saveData();
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			 }
		 }
		
		ServerSocket server= null;
		try {
			//Create a ServerSock on socket:4591
	        server = new ServerSocket(4591);
	        server.setReuseAddress(true);
	        
			while(true) {
		        System.out.println("ServerSocket awaiting connections...");

		        // .accept blocks until an inbound connection on this port is attempted
		        Socket client = server.accept();
		        //System.out.println("Connection from " + client.getInetAddress().getHostAddress());
		        
		        ClientHandler clientSock = new ClientHandler(client);
		        new Thread(clientSock).start();
			}
		}
		catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
		finally {
			if (server != null) {
				try {
					server.close();
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	private static class ClientHandler implements Runnable {
		private final Socket clientSocket;
		
		private Message msg;
		
		private OutputStream outputStream;
		private InputStream inputStream;
		private ObjectOutputStream out;
		private ObjectInputStream in;
		
		private User currUser;
		private BankAccount currAccount;
		
		private boolean loggedIN, Teller;
		private String[] input;
		
		private Server parent;
		
		//Constructor
		public ClientHandler(Socket socket)  throws IOException, ClassNotFoundException
		{
			loggedIN = false;
			Teller = false;
			
			clientSocket = socket;
			
	        //get the input stream from the connected socket
	        outputStream = clientSocket.getOutputStream();
	        inputStream = clientSocket.getInputStream();

	        //create a ObjectInputStream so we can read data from it.
	        out = new ObjectOutputStream(outputStream);
	        in = new ObjectInputStream(inputStream);
		}

		public void run()
		{
			try {
				if(!loggedIN) {
					
					msg = (Message)in.readObject();
					
					if(msg.getType().equals( MessageType.CONNECT_CLIENT)) {
						
						System.out.println("Client Connected: " + clientSocket.getInetAddress().getHostAddress());
						msg = new Message(MessageType.SUCCESS,"");
						Teller = false;
					}
					else if(msg.getType().equals( MessageType.CONNECT_TELLER)) {
			
						System.out.println("Teller Connected: " + clientSocket.getInetAddress().getHostAddress());
						msg = new Message(MessageType.SUCCESS,"");
						Teller = true;
					}
					else {
						System.out.println("Unidentified Device: " + clientSocket.getInetAddress().getHostAddress() + "\nTerminating Connection.");
						clientSocket.close();
					}
					
					out.writeObject(msg);
					out.flush();
					
					verifyLogin();
					
					while(loggedIN) {
							
						msg = (Message)in.readObject();	//get object from network
							
						if(currUser.isTeller() && Teller) {
							switch (msg.getType()) {
								case LOGOUT:
									handleLogout();
									break;
								case DEPOSIT:
									handleDeposit();
									break;
								case WITHDRAW:
									handleWithdraw();
									break;
								case ADD_USER:
									handleAddUser();
									break;
								case REMOVE_USER:
									handleRemoveUser();
									break;
								case TRANSFER:
									handleTransfer();
									break;
								case USER_INFO_REQ:
									handleUserInfo();
									break;
								case LOGS_REQ:
									handleLogRequest();
									break;
								case ACCOUNT_INFO:
									handleAccountInfo();
									break;
								// Add more cases for other message types
								default:
									// Handle unknown message types
									loggedIN = false;
									break;
							}
						}
						else {
							switch (msg.getType()) {
								case LOGOUT:
									handleLogout();
									break;
								case DEPOSIT:
									handleDeposit();
									break;
								case WITHDRAW:
									handleWithdraw();
									break;
								case USER_INFO_REQ:
									handleUserInfo();
									break;
								case ACCOUNT_INFO:
									handleAccountInfo();
									break;
								// Add more cases for other message types
								default:
									// Handle unknown message types
									loggedIN = false;
								break;
							}
						}
					}
				}
				clientSocket.close();
			}
			catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
			}
			finally {
				try {
					if (out != null) {
						out.close();
					}
					if (in != null) {
						in.close();
						clientSocket.close();
					}
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		private void verifyLogin() {
			String username, password;
			while(!loggedIN) {
				try {
					msg = (Message)in.readObject();
										
					if(msg.type == MessageType.LOGIN_REQ) {
												
						input = msg.getData().split("\n",2); //parse data string from message object

						username = input[0];
						password = input[1];
						
						currUser = Users.get(username);
						
						if(currUser != null) {
							if(currUser.verify(password)) {
								loggedIN = true;
								msg = new Message(MessageType.SUCCESS,"Login Successful");
							}
							else {
								msg = new Message(MessageType.FAIL,"Login Failed");
							}
						}
						else {
							msg = new Message(MessageType.FAIL,"User Does NOT Exist");
						}
					}
					else {
						loggedIN = false;
						msg = new Message(MessageType.FAIL,"Login Failed");
						break;
					}
					out.writeObject(msg);
					out.flush();
				}
				catch (IOException | ClassNotFoundException e) {
				e.printStackTrace();
				}
			}
		}
		
		//Notify end user of logout then close connection.
		private void handleLogout() throws IOException {
			msg = new Message(MessageType.LOGOUT,"User Logged Out");
			out.writeObject(msg);
			out.flush();
			loggedIN = false;
		}
		
		private void handleDeposit() throws IOException {
			
			BankAccount account = Accounts.get(msg.getData()); //grabs account from hash
			float funds = msg.getFunds();
			
			Date date = new Date();
			Vector<Log> accountLogs = Logs.get(account.getName());
			
			if(account != null) { //if it exists
				
				if(Teller || account.hasUser(currUser.getName())) { //check if user has permission to access account
					
					if(account.deposit(funds)) {
						msg = new Message(MessageType.SUCCESS,"Funds Deposited"); //deposit is successful
						
						Log log = new Log(currUser.getName(),"Deposit",funds,date,account.getName());
						accountLogs.addElement(log);
						Logs.put(account.getName(), accountLogs);
					}
					else{
						msg = new Message(MessageType.FAIL,"Insufficient Funds"); //deposit is unsuccessful
					}
				}
				else {
					msg = new Message(MessageType.FAIL,"Invalid User");
				}
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Acount");
			}
			
			out.writeObject(msg);
			out.flush();
		}
		
		private void handleWithdraw() throws IOException {
			
			BankAccount account = Accounts.get(msg.getData()); //grabs account from hash
			
			Date date = new Date();
			Vector<Log> accountLogs = Logs.get(account.getName());
			
			float funds = msg.getFunds();
			
			if(account != null) { //if it exists
				
				if(Teller || account.hasUser(currUser.getName())) { //check if user has permission to access account
					
					if(account.withdraw(funds)) {
						msg = new Message(MessageType.SUCCESS,"Funds Withdrawn");
						
						Log log = new Log(currUser.getName(),"Withdraw",funds,date,account.getName());
						accountLogs.addElement(log);
						Logs.put(account.getName(), accountLogs);//withdraw is successful
					}
					else{
						msg = new Message(MessageType.FAIL,"Insufficient Funds"); //withdraw is unsuccessful
					}
				}
				else {
					msg = new Message(MessageType.FAIL,"Invalid User");
				}
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Acount");
			}
			
			out.writeObject(msg);
			out.flush();
		}
		
		private void handleUserInfo() throws IOException{
			
			Vector<String> accounts = currUser.getAccounts();
			BankAccount currAccount;
			
			for(String x : accounts) {
				currAccount = Accounts.get(x);
				if(currAccount != null) {
					if(currAccount.hasUser(currUser.getName())) {
						msg = new Message(MessageType.ACCOUNT_INFO,currAccount.getName() + "\n" + currAccount.getStatus(),currAccount.getBalance());
						out.writeObject(msg);
						out.flush();
					}
				}
			}
			
			msg = new Message(MessageType.DONE,"");
			out.writeObject(msg);
			out.flush();
		}
		
		private void handleAddUser()throws IOException{
			
			input = msg.getData().split("\n",2);
			
			String accountName = input[0];
			String user = input[1];
			
			BankAccount account = Accounts.get(accountName);
			
			Date date = new Date();
			Vector<Log> accountLogs = Logs.get(account.getName());
			
			if(account != null) {
				if(currAccount.addUser(user)) {
					msg = new Message(MessageType.SUCCESS,"User " + user + " added to account " + accountName);
					
					Log log = new Log(currUser.getName(),"Added User",-1,date,account.getName());
					accountLogs.addElement(log);
					Logs.put(account.getName(), accountLogs);
				}
				else {
					msg = new Message(MessageType.FAIL,"User already attached");
				}
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Acount");
			}
			out.writeObject(msg);
			out.flush();
		}
		
		private void handleRemoveUser() throws IOException{
			
			input = msg.getData().split("\n",2);
			
			String accountName = input[0];
			String user = input[1];
			
			BankAccount account = Accounts.get(accountName);
			
			Date date = new Date();
			Vector<Log> accountLogs = Logs.get(account.getName());
			 
			if(account != null) {
				if(account.removeUser(user)) {
					msg = new Message(MessageType.SUCCESS,"User " + user + " removed from account " + accountName);
					
					Log log = new Log(currUser.getName(),"Added User",-1,date,account.getName());
					accountLogs.addElement(log);
					Logs.put(account.getName(), accountLogs);
				}
				else {
					msg = new Message(MessageType.FAIL,"User not attached");
				}
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Acount");
			}
			
			out.writeObject(msg);
			out.flush();
		}
		
		private void handleTransfer() throws IOException{
			
			input = msg.getData().split("\n",2);
			/* 
			 * Message data will be captured to prevent any unwanted errors
			 * or overwrites
			 */
			String account1 = input[0]; //name of account
			String account2 = input[1]; //name of destination account
			
			float funds = msg.getFunds(); //funds duh
			
			BankAccount fromAccount = Accounts.get(account1); //account funds withdrawn from
			BankAccount toAccount = Accounts.get(account2); //account funds transfered to
			
			Date date1 = new Date();
			Date date2 = new Date();
			Vector<Log> accountLogs1 = Logs.get(fromAccount.getName());
			Vector<Log> accountLogs2 = Logs.get(toAccount.getName());
			
			if(fromAccount == null || toAccount == null) {
				msg = (fromAccount == null) ?
						(new Message(MessageType.FAIL,"Invalid account: " + account1)) :
						(new Message(MessageType.FAIL,"Invalid account: " + account2));
			}
			else {
				if(fromAccount.withdraw(funds)) {
					toAccount.deposit(funds);
					msg = new Message(MessageType.SUCCESS,"Transfer Successful");
					
					Log log1 = new Log(currUser.getName(),"Transfer (-)",funds,date1,fromAccount.getName());
					accountLogs1.addElement(log1);
					Logs.put(fromAccount.getName(), accountLogs1);
					
					Log log2 = new Log(currUser.getName(),"Transfer (+)",funds,date1,toAccount.getName());
					accountLogs2.addElement(log2);
					Logs.put(toAccount.getName(), accountLogs2);
					
				}
				else {
					msg = new Message(MessageType.FAIL,"Insufficient Funds");
				}
			}
			out.writeObject(msg);
			out.flush();
		}
		
		private void handleLogRequest() throws IOException {

			String logData;
			
			String input = msg.getData();
			
			Vector<Log> userLogs = Logs.get(input);
			
			if(Logs.containsKey(input)) {
				for (Log x : userLogs) {
					logData = x.getUser() + "\n"+ x.getAction() + "\n" + x.getAmount() + "\n" + x.getDate();
					msg = new Message(MessageType.LOG_INFO,logData);
					out.writeObject(msg);
					out.flush();
		        }
				msg = new Message(MessageType.DONE,"");
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Account");
			}
			out.writeObject(msg);
			out.flush();
		}
		
		private void handleAccountInfo() throws IOException{
			String user = msg.getData();
			BankAccount account = Accounts.get(user);
			
			if(account != null) {
				String data = account.getName() + "\n" + account.getStatus();
				msg = new Message(MessageType.ACCOUNT_INFO,data,account.getBalance());
			}
			else {
				msg = new Message(MessageType.FAIL,"Account Not Found");
			}
			out.writeObject(msg);
			out.flush();
		}
	}
	
	private static void initData() throws FileNotFoundException { // it's not actually possible for this to get thrown I think
		FileInputStream inFile;
		String currLine = " ";
		Scanner scan;
		String currAcc;
		String currUser;
		String currAmount;	
		String currDate;
		String currAction;
		String currMinBalance;
		String currBalance;
		AccountStatus currStatus;
		
		File userFile = new File(workingDir + "/users.txt");
		scan = new Scanner(userFile);
		while (scan.hasNext()) {
			currLine = scan.nextLine();
			Users.put(
					//key
					currLine.substring(0, currLine.indexOf('|')),
					//hashed object
					new User(
							currLine.substring(0, currLine.indexOf('|')), //username
							currLine.substring(currLine.indexOf('|') + 1, //password
							currLine.lastIndexOf('|')),
							currLine.charAt(currLine.length() - 1) == '1' //teller flag
							)
					);
			// warning: do not try to read this
		}
		
		File accFile = new File(workingDir + "/accounts.txt");
		scan = new Scanner(accFile);
		while (scan.hasNext()) {
			currLine = scan.nextLine();
			currAcc = currLine.substring(0, currLine.indexOf('|'));
			currLine = currLine.substring(currLine.indexOf('|') + 1);
			currBalance = currLine.substring(0, currLine.indexOf('|'));
			currLine = currLine.substring(currLine.indexOf('|') + 1);
			currMinBalance = currLine.substring(0, currLine.indexOf('|'));
			currLine = currLine.substring(currLine.indexOf('|') + 1);
			Logs.put(currAcc, new Vector<Log>());
			switch (currLine) {
			case "GOOD":
				currStatus = AccountStatus.GOOD;
				break;
			case "FREEZE":
				currStatus = AccountStatus.FREEZE;
				break;
			default:
				currStatus = AccountStatus.INACTIVE;
			}
			Accounts.put(currAcc, new BankAccount(currAcc, Float.parseFloat(currBalance), Float.parseFloat(currMinBalance), currStatus));
			currLine = scan.nextLine();
			while (currLine.contains("|")) { // THIS REALLY SUCKS A TON BUT IT WORKS
				Accounts.get(currAcc).addUser(currLine.substring(0, currLine.indexOf('|')));
				Users.get(currLine.substring(0, currLine.indexOf('|'))).addAccount(currAcc);
				currLine = currLine.substring(currLine.indexOf('|') + 1);
			}
			if (!currLine.equals("")) {
				Accounts.get(currAcc).addUser(currLine);
				Users.get(currLine).addAccount(currAcc);
			}
		}
		
		for (File file : new File(workingDir).listFiles()) {
			if (file.toString().charAt(file.toString().lastIndexOf('/') + 1) == 'l') {
				currAcc = file.toString().substring(file.toString().lastIndexOf('g') + 1, file.toString().lastIndexOf('.'));
				inFile = new FileInputStream(file);
				scan = new Scanner(inFile);
				while (scan.hasNext()) {
					currLine = scan.nextLine();
					currUser = currLine.substring(0, currLine.indexOf('|'));
					currLine = currLine.substring(currLine.indexOf('|') + 1);
					currAction = currLine.substring(0, currLine.indexOf('|'));
					currLine = currLine.substring(currLine.indexOf('|') + 1);
					currAmount = currLine.substring(0, currLine.indexOf('|'));
					currDate = currLine.substring(currLine.indexOf('|') + 1);
					Logs.get(currAcc).add(new Log(currUser, currAction, currAmount, currDate, currAcc));
				}
			}
		}
	}
	
	private static void saveData() throws IOException {
		for (File file : new File(workingDir).listFiles()) {
			file.delete(); // Surely this is a great idea for how to implement this with no flaws whatsoever
		}
		File newFile = new File(workingDir + "/users.txt");
		newFile.createNewFile();
		PrintWriter fileOut = new PrintWriter(newFile);
		for (User user : Users.values()) {
			fileOut.write(user.toString() + "\n");
		}
		fileOut.close();
		newFile = new File(workingDir + "/accounts.txt");
		newFile.createNewFile();
		fileOut = new PrintWriter(newFile);
		for (BankAccount account : Accounts.values()) {
			fileOut.write(account.toString() + "\n");
		}
		fileOut.close();
		for (String acc : Logs.keySet()) {
			newFile = new File(workingDir + "/log" + acc + ".txt");
			newFile.createNewFile();
			fileOut = new PrintWriter(newFile);
			for (Log log : Logs.get(acc)) {
				fileOut.write(log.toString() + "\n");
			}
			fileOut.close();
		}
	}
}
