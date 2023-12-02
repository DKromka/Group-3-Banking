import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Vector;

public class Server {
	//Can you put input/outputStreams to send message objects
	//back and forth server to client... 
	static private HashMap <String,User> Users;
	static private HashMap <String,BankAccount> Accounts;
	static private HashMap <String,Vector<log>> Logs;
	
	Server(){
		Users = new HashMap<String,User>();
		Accounts = new HashMap<String,BankAccount>();
		LoadUsers();
	}
	
	public static void main(String[] args) {
		ServerSocket server= null;
		try {
			//Create a ServerSock on socket:4591
	        server = new ServerSocket(4591);
	        server.setReuseAddress(true);
	        
			while(true) {
		        System.out.println("ServerSocket awaiting connections...");

		        // .accept blocks until an inbound connection on this port is attempted
		        Socket client = server.accept();
		        System.out.println("Connection from " + client.getInetAddress().getHostAddress());
		        
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
									handleAccountInfoReq();
									break;
								case LOGS_REQ:
									handleLogRequest();
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
									handleAccountInfoReq();
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
			loggedIN = false;
		}
		
		private void handleDeposit() throws IOException {
			
			BankAccount account = Accounts.get(msg.getData()); //grabs account from hash
			
			float funds = msg.getFunds();
			
			if(account != null) { //if it exists
				
				if(account.hasUser(currUser.getName())) { //check if user has permission to access account
					
					if(account.deposit(funds)) {
						msg = new Message(MessageType.SUCCESS,"Funds Deposited"); //withdraw is successful
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
		}
		
		private void handleWithdraw() throws IOException {
			
			BankAccount account = Accounts.get(msg.getData()); //grabs account from hash
			
			float funds = msg.getFunds();
			
			if(account != null) { //if it exists
				
				if(account.hasUser(currUser.getName())) { //check if user has permission to access account
					
					if(account.withdraw(funds)) {
						msg = new Message(MessageType.SUCCESS,"Funds Withdrawn"); //withdraw is successful
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
		}
		
		private void handleAccountInfoReq() throws IOException{
			
			BankAccount account = Accounts.get(msg.getData()); //grabs account from hash
			
			if(account != null) { //check if it exists
				if(account.hasUser(currUser.getName())) { //checks if user has permission to access account
					msg = new Message(MessageType.ACCOUNT_INFO,account.getName() + "\n" + account.getStatus(),account.getBalance());
				}
				else {
					msg = new Message(MessageType.FAIL,"Acccess Denied");
				}
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Acount");
			}
			out.writeObject(msg);
		}
		
		private void handleAddUser()throws IOException{
			
			input = msg.getData().split("\n",2);
			
			String accountName = input[0];
			String user = input[1];
			
			BankAccount account = Accounts.get(accountName);
			
			if(account != null) {
				if(currAccount.addUser(user)) {
					msg = new Message(MessageType.SUCCESS,"User " + user + " added to account " + accountName);
				}
				else {
					msg = new Message(MessageType.FAIL,"User already attached");
				}
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Acount");
			}
			out.writeObject(msg);
		}
		
		private void handleRemoveUser() throws IOException{
			
			input = msg.getData().split("\n",2);
			
			String accountName = input[0];
			String user = input[1];
			
			BankAccount account = Accounts.get(accountName);
			 
			if(account != null) {
				if(account.removeUser(user)) {
					msg = new Message(MessageType.SUCCESS,"User " + user + " removed from account " + accountName);
				}
				else {
					msg = new Message(MessageType.FAIL,"User not attached");
				}
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Acount");
			}
			
			out.writeObject(msg);
		}
		
		private void handleTransfer(){
			
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
			
			if(fromAccount == null || toAccount == null) {
				msg = (fromAccount == null) ?
						(new Message(MessageType.FAIL,"Invalid account: " + account1)) :
						(new Message(MessageType.FAIL,"Invalid account: " + account2));
			}
			else {
				if(fromAccount.withdraw(funds)) {
					toAccount.deposit(funds);
					msg = new Message(MessageType.SUCCESS,"Transfer Successful");
				}
				else {
					msg = new Message(MessageType.FAIL,"Insufficient Funds");
				}
			}
		}
		
		private void handleLogRequest() {
			String user = null;
			String action = null;
			String date = null;
			float amount = -1;
			String logData;
			
			String input = msg.getData();
			
			Vector<log> userLogs = Logs.get(input);
			
			if(Logs.containsKey(input)) {
				for (log x : userLogs) {
					logData = x.getUser() + "\n"+ x.getAction() + "\n" + x.getAmount() + "\n" + x.getDate();
					msg = new Message(MessageType.LOG_INFO,logData);
					out.writeObject(msg);
		        }
				msg = new Message(MessageType.DONE,"");
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Account");
			}
			out.writeObject(msg);
		}
	}
	
	private void LoadUsers(){
		//load user data from file.
	}
}
