
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;
import java.util.logging.Handler;


public class Client extends User{
	




	//create socket for 2 way communication outputStream/inputStream
	private Socket socket;
	private ObjectOutputStream outstream;
	private ObjectInputStream instream;
	private Vector<String> accounts;
	private static String name;
	private static String status;
	private static String funds;
	private static String password;
	private static boolean teller;
	
	ClientHandler currHandler;
	

	
	
	
	//main
		public static void main(String[] args) throws IOException, ClassNotFoundException {
			//Create a ServerSock on socket:4591

			info();
			//make a client
			Client client = null;
			client = new Client("localhost", 4591);

			//send login request to server
			Message message;
			MessageType mt = MessageType.LOGIN_REQ;
		    message = new Message(mt, "", 0);
		    client.outstream.writeObject(message);
            client.outstream.flush();
		    
		    //receive message server message
		    message = (Message) client.instream.readObject();
		    //save type of message that server sent
		    MessageType messageBack = message.getType();
		    //if type = success prompt the user with message
		    
		    if (messageBack.equals("SUCCESS")) {
		    	//client connected to server
				System.out.println("Connected to server!");
				
		        Scanner scanner = new Scanner(System.in);

				
				  //infinite loop while user not logged off
		        while (true) {
		            System.out.print("Enter Action: \n"
		            		+"1. Withdraw\n"
		            		+ "2. Deposit\n"
		            		+ "3. Check balance\n"
		            		+ "4. Log out");

		            String userInput = scanner.nextLine();
		            
		            
		            //if user input 1 = withdraw request
		            if (userInput.equalsIgnoreCase("1")) {
		            	//send message to server based on action:
			            mt = MessageType.WITHDRAW;
			            int i = message.getID();
			            float f = message.getFunds();
			            String userID = Integer.toString(i);
			            //message object contains: type, id of user, funds
			            message = new Message(mt, userID , f);
			            client.outstream.writeObject(message);
			            client.outstream.flush();
			            
			            //if user input 2 = deposit request
		            }else if (userInput.equalsIgnoreCase("2")) {
			            mt = MessageType.DEPOSIT;
			            int i = message.getID();
			            float f = message.getFunds();
			            String userID = Integer.toString(i);
			            //message object contains: type, id of user, funds
			            message = new Message(mt, userID, f);
			            client.outstream.writeObject(message);
			            client.outstream.flush();
			            
			            //if user input 3 = get account info
		            }else if (userInput.equalsIgnoreCase("3")) {
			            mt = MessageType.ACCOUNT_INFO;
			            int i = message.getID();
			            String userID = Integer.toString(i);
			            float f = message.getFunds();
			            //message object contains: type, id of user, funds
						message = new Message(mt, userID , f);
			            client.outstream.writeObject(message);
			            client.outstream.flush();
			            
			            
			            //if user input 4 = logout
		            }else if (userInput.equalsIgnoreCase("4")) {
			            mt = MessageType.LOGOUT;
			            int i = message.getID();
			            String userID = Integer.toString(i);
			            float f = message.getFunds();
			            //message object contains: type, id of user, funds
			            message = new Message(mt, userID, 0);
			            client.outstream.writeObject(message);
			            client.outstream.flush();
			            //read message back from server
			            message = (Message) client.instream.readObject();
			            
	 		            //if successfully logged off from server, remove client
		                  if (message.getType().equals("SUCCESS")) {
		                        System.out.println("you have been logged off.\n\n Bye!");
		                        client.socket.close();
		                        break;
		                   }
		            }
		        }
		    }	    
		}

 
    



	
	public boolean addAccount(String id) {
		//if account is already in, return false, else true
		if (accounts.contains(id)) {
			return false;
		}
			accounts.add(id); 
			return true;
	}
		

	public boolean removeAccount(String id) {
		//if removing existing Id, return true
		if (accounts.contains(id)) {
			return true;
		}
			accounts.add(id); 
			return false;
	}
		
	
	

	//getAccount method for vectors 
		Vector<String> getAccounts(String accountID) throws IOException, ClassNotFoundException{
			//create message object to get accountID info 
	        MessageType mt = MessageType.ACCOUNT_INFO;
		    Message message = new Message(mt, "",0);
			//send message to server
            outstream.writeObject(message);
            outstream.flush();
		    


			//read the info and separate account info 
			instream.readObject();
			
			//make vector to put object of info in
	        BufferedReader reader = new BufferedReader(new InputStreamReader(instream));
			Vector <String> accountInfo = new Vector<>();
			String info;
			
			
			//while loop to read all info from instream object
			while ((info = reader.readLine()) != null){
				//comma separated
				String[] tokens = info.split(",");
				//tokens based on Bank account info encoding we discussed on discord
				if(tokens.length == 3) {
					String accountName = tokens [0];
					String status = tokens[1];
					String balance = tokens[2];

					
					//put info of object in vector
					accountInfo.add("User's Name: " + accountName);
					accountInfo.add("Status: " + status);
					accountInfo.add("Balance: " + balance);
					
				}else {
					System.out.println("please format string as:\n"
							+ "account name, status, balance");
				}
			}
			
			//return account info. you can print it. 
			return accountInfo;
			
		}
		
		
	//Get Client Handler - class still needs to be implemented
	public ClientHandler getHandler() {
		return currHandler;
		//stub

	}
	
	//Set Client Handler
	public  void setHandler() {
		//stub
		
	}
	
	
	
	//connect to server
	public Client(String server, int numPort) throws UnknownHostException, IOException {
		//create user
		super(name, password, teller);
		//connect to server
		socket = new Socket(server, numPort);
	    outstream= new ObjectOutputStream(socket.getOutputStream());
	    instream = new ObjectInputStream(socket.getInputStream());
	}



	//print a message
	private static void info() {
		System.out.println("This is the Client Class");
		System.out.println(":)\n");
		
	}

}

