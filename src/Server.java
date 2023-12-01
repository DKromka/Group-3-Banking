import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;

public class Server {
	//Can you put input/outputStreams to send message objects
	//back and forth server to client... 
	static private HashMap <String,User> Users;
	static private HashMap <String,BankAccount> Accounts;
	static private HashMap <String,log> Logs;
	static private float minimumFunds = 25;
	
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
		private User currClient;
		private boolean loggedIN, Teller;
		private BankAccount currAccount;
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
					
					String inputStr;
					
					while(loggedIN) {
							
						msg = (Message)in.readObject();	//get object from network
							
						if(currClient.isTeller()) {
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
		
		void verifyLogin() {
			String[] input;
			String username, password;
			while(!loggedIN) {
				try {
					msg = (Message)in.readObject();
					if(msg.type == MessageType.LOGIN_REQ) {
												
						input = msg.getData().split("\n",2); //parse data string from message object
						
						System.out.println(input[0]);
						//store username and password temporarily
						username = ""; //input[0];
						password = ""; //input[1];
						
						currClient = Users.get(input[0]);
						
						if(currClient != null) {
							if(currClient.verify(password)) {
								loggedIN = true;
								msg = new Message(MessageType.SUCCESS,"Login Successful",0);
							}
							else {
								msg = new Message(MessageType.FAIL,"Login Failed",0);
							}
						}
						else {
							msg = new Message(MessageType.FAIL,"User Does NOT Exist",0);
						}
					}
					else {
						loggedIN = false;
						msg = new Message(MessageType.FAIL,"Login Failed",0);
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
		void handleLogout() throws IOException {
			msg = new Message(MessageType.LOGOUT,"User Logged Out",0);
			out.writeObject(msg);
			loggedIN = false;
		}
		
		void handleDeposit() throws IOException {
			
			currAccount = Accounts.get(msg.getData()); // grabs account from hash
			
			if(currAccount.hasUser(currClient.getName()) && currAccount != null) { //check if user has permission to access and if it exists
				if(currAccount.deposit(msg.getFunds())) {
					msg = new Message(MessageType.SUCCESS,"Funds have been Deopsited",0); //deposit successful
				}
				else {
					msg = new Message(MessageType.FAIL,"Invalid Funds",0); //deposit is unsuccessful
				}
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Acount",0);
			}
			
			out.writeObject(msg);
		}
		
		void handleWithdraw() throws IOException {
			
			currAccount = Accounts.get(msg.getData()); //grabs account from hash
			
			float withdrawFunds;
			if(currAccount.hasUser(currClient.getName()) && currAccount != null) { //check if user has permission to access and if it exists
				if(currAccount.withdraw(msg.getFunds())) {
					msg = new Message(MessageType.SUCCESS,"Funds Withdrawn",0); //withdraw is successful
				}
				else{
					msg = new Message(MessageType.FAIL,"Insufficient Funds",0); //withdraw is unsuccessful
				}
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Acount",0);
			}
			
			out.writeObject(msg);
		}
		
		void handleAccountInfoReq() throws IOException{
			currAccount = Accounts.get(msg.getData()); //grabs account from hash
			
			if(currAccount.hasUser(currClient.getName()) && currAccount != null) { //check if user has permission to access and if it exists
				msg = new Message(MessageType.ACCOUNT_INFO,currAccount.getName() + "\n" + currAccount.getStatus(),currAccount.getBalance());
			}
			else {
				msg = new Message(MessageType.FAIL,"Invalid Acount",0);
			}
			out.writeObject(msg);
		}
		
		void handleAddUser(){
			
		}
		
		void handleRemoveUser() {
			
		}
	}
	
	private void LoadUsers(){
		//load user data from file.
	}
}
