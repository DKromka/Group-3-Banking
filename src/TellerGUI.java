import java.awt.HeadlessException;
	import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

public class TellerGUI implements ActionListener {
	
	private String user;
	private String currAccount;
	private Vector<String[]> currLogs;
	private ObjectOutputStream outObj;
	private ObjectInputStream inObj;
	private JFrame frame;
	private JTable logTable;
	private JScrollPane tablePane;
	private JPanel buttonPane;
	private MyTableModel currModel;
	
	private class MyTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;
		public MyTableModel() {
			int i = 0;
			String[] curr = null;
			for (var it = currLogs.iterator(); it.hasNext(); i++) {
				curr = it.next();
				for (int j = 0; j < 4; j++) {
					setValueAt(curr[j], i, j);
				}
			}
		}
		public String getColumnName(int col) {
			switch (col) {
			case 0:
				return "User";
			case 1:
				return "Action";
			case 2:
				return "Amount";
			default:
				return "Date";
			}
		}
		public void refresh() {
			fireTableDataChanged();
		}
		public int getColumnCount() {return 4;}
		public int getRowCount() {return currLogs.size();}
		public Object getValueAt(int row, int col) {return currLogs.get(row)[col];}
		private void printDebugData() {System.out.println("ltg");}
	}
	
	public void run() throws NumberFormatException, UnknownHostException, IOException, HeadlessException, ClassNotFoundException {
			Socket socket;
			inObj = null;
			outObj = null;
			Scanner input = new Scanner(System.in);
			
			try {
				System.out.println("IP?");
				String ip = input.nextLine();
				System.out.println("Port?");
				
				socket = new Socket(ip, Integer.parseInt(input.nextLine()));
				outObj = new ObjectOutputStream(socket.getOutputStream());
				inObj = new ObjectInputStream(socket.getInputStream());
			}
			catch (Exception e) {
				System.out.println("lol. lmao"); // obviously a temporary hack
			}
			
			frame = new JFrame();
			
			String pass;
			
			do {
				user = JOptionPane.showInputDialog(frame, "Input username");
				pass = JOptionPane.showInputDialog(frame, "Input password");
			}
			while (!login(user, pass));
			
			currLogs = new Vector<String[]>();
			
			currModel = new MyTableModel();
			logTable = new JTable(currModel);
			logTable.setOpaque(true);
			tablePane = new JScrollPane(logTable);
			buttonPane = new JPanel();	
			
			JButton button1 = new JButton("Deposit"); button1.setActionCommand("deposit"); button1.addActionListener(this);
			JButton button2 = new JButton("Withdraw"); button2.setActionCommand("withdraw"); button2.addActionListener(this);
			JButton button3 = new JButton("Refresh"); button3.setActionCommand("refresh"); button3.addActionListener(this);
			JButton button4 = new JButton("Return"); button4.setActionCommand("Die"); button4.addActionListener(this);
			JButton button5 = new JButton("Transfer"); button5.setActionCommand("transfer"); button5.addActionListener(this);
			JButton button6 = new JButton("Change Status"); button6.setActionCommand("status"); button6.addActionListener(this);
			JButton button7 = new JButton("Add User"); button7.setActionCommand("add"); button7.addActionListener(this);
			JButton button8 = new JButton("Remove User"); button8.setActionCommand("remove"); button8.addActionListener(this);
			
			buttonPane.add(button1); buttonPane.add(button2); buttonPane.add(button3); buttonPane.add(button4); buttonPane.add(button5); buttonPane.add(button6);
			
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tablePane, buttonPane);
			
			frame.getContentPane().add(splitPane);
			frame.pack();
			
			displayMainUI();
		}

	private void displayMainUI() throws IOException, HeadlessException, ClassNotFoundException {
		frame.setVisible(false);
		int ans = JOptionPane.showOptionDialog(null, "What do?", "Teller", JOptionPane.YES_NO_OPTION, JOptionPane.PLAIN_MESSAGE, null, new String[] {"Select Account", "Make Account", "Log Out"}, "Select Account");;
		switch (ans) {
		case 0:
			currAccount = JOptionPane.showInputDialog(frame, "Which account?");
			if (!getLogs()) {
				JOptionPane.showMessageDialog(null, "Invalid account name", "Error", JOptionPane.ERROR_MESSAGE);
				break;
			}
			displayAccountUI();
			break;
		case 1:
			String newAcc = JOptionPane.showInputDialog(frame, "What to name the account?");
			if (!createAccount(newAcc)) {
				JOptionPane.showMessageDialog(null, "Create operation failed", "Error", JOptionPane.ERROR_MESSAGE);
			}
			break;
		case 2:
			logout();
		}	
	}
	
	private void displayAccountUI() {
		frame.setVisible(true);
	}
	
	private boolean login(String user, String pass) throws IOException, ClassNotFoundException {

		Message message = new Message(MessageType.CONNECT_TELLER, "");

		outObj.writeObject(message);
		outObj.flush();

		message = (Message) inObj.readObject();

		if (message.getType().equals(MessageType.SUCCESS)) {

			message = new Message(MessageType.LOGIN_REQ, user + "\n" + pass);
		
			outObj.writeObject(message);
			outObj.flush();
			
			message = (Message) inObj.readObject();
			
			if (message.getType().equals(MessageType.SUCCESS)) {
				
				return true;
				
			} else {
				
				return false;
			}

		} else {

			return false;

		}
		
	}
	
	
	
	private boolean getLogs() throws IOException, ClassNotFoundException {
		
		currLogs.clear();
		
		Message message = new Message(MessageType.LOGS_REQ, currAccount);
		
		outObj.writeObject(message);
		outObj.flush();
		
		String user;
		String action;
		String amount;
		String date;
		String currData;
		
		message = (Message) inObj.readObject();
		
		if (message.getType() == MessageType.FAIL) {
			return false;
		}
		
		while (message.getType() != MessageType.DONE) {
			
			currData = message.getData();
			
			user = currData.substring(0, currData.indexOf('\n'));
			currData = currData.substring(currData.indexOf('\n') + 1);
			action = currData.substring(0, currData.indexOf('\n'));
			currData = currData.substring(currData.indexOf('\n') + 1);
			amount = currData.substring(0, currData.indexOf('\n'));
			date = currData.substring(currData.indexOf('\n') + 1);
			
			currLogs.add(new String[] {user, action, amount, date});
			
			message = (Message) inObj.readObject();
		}
		return true;
	}
	
	private boolean deposit(float amount) throws IOException, ClassNotFoundException {
		
		Message message = new Message(MessageType.DEPOSIT, currAccount, amount);
				
		outObj.writeObject(message);
		outObj.flush();
		
		message = (Message) inObj.readObject();
		
		if (message.getType().equals(MessageType.SUCCESS)) {
			
			System.out.println("dope");
			return true;
			
			
		} else {
			
			System.out.println(message.getData());
			return false;
		}
				
	}
	
	private boolean withdraw(float amount) throws IOException, ClassNotFoundException {
		
		Message message = new Message(MessageType.WITHDRAW, currAccount, amount);
				
		outObj.writeObject(message);
		outObj.flush();
		
		message = (Message) inObj.readObject();
		
		if (message.getType().equals(MessageType.SUCCESS)) {
			
			return true;
			
		} else {
			
			return false;
		}
				
	}
	
	private boolean createAccount(String newAcc) throws IOException, ClassNotFoundException {
		
		Message message = new Message(MessageType.MAKE_ACCOUNT, newAcc, 25); // 25 is minBalance
				
		outObj.writeObject(message);
		outObj.flush();
		
		message = (Message) inObj.readObject();
		
		if (message.getType().equals(MessageType.SUCCESS)) {
			
			return true;
			
		} else {
			
			return false;
		}
				
	}
	
	private boolean removeUser(String user) throws IOException, ClassNotFoundException {
		
		Message message = new Message(MessageType.REMOVE_USER, currAccount + "\n" + user);
				
		outObj.writeObject(message);
		outObj.flush();
		
		message = (Message) inObj.readObject();
		
		if (message.getType().equals(MessageType.SUCCESS)) {
			
			return true;
			
		} else {
			
			return false;
		}
				
	}
	
	private boolean addUser(String user) throws IOException, ClassNotFoundException {
		
		Message message = new Message(MessageType.ADD_USER, currAccount + "\n" + user);
				
		outObj.writeObject(message);
		outObj.flush();
		
		message = (Message) inObj.readObject();
		
		if (message.getType().equals(MessageType.SUCCESS)) {
			
			return true;
			
		} else {
			
			return false;
		}
				
	}
	
	private void changeStatus(String status) throws IOException, ClassNotFoundException {
		
		Message message = new Message(MessageType.STATUS_CHANGE, currAccount + "\n" + status);
				
		outObj.writeObject(message);
		outObj.flush();
		
	}
	
	
	
	private void logout() throws IOException {

		Message message = new Message(MessageType.LOGOUT, "");
				
		outObj.writeObject(message);
		outObj.flush();

		System.exit(0);

	}
	
	public boolean transfer(String acc, float amount) throws IOException, ClassNotFoundException {
		
		Message message = new Message(MessageType.TRANSFER, currAccount + "\n" + acc, amount);
		
		outObj.writeObject(message);
		outObj.flush();
		
		message = (Message) inObj.readObject();
		
		System.out.println("read");
		
		return message.getType() != MessageType.FAIL;
	}
	
	public void actionPerformed(ActionEvent event) {
		switch (event.getActionCommand()) {
		case "deposit":
				float depositAmount;
			try {
				depositAmount = Float.parseFloat(JOptionPane.showInputDialog("How much to deposit to " + currAccount));
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				deposit(depositAmount);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			try {
				refresh();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			return;
		case "withdraw":
			float withdrawAmount;
			try {
				withdrawAmount = Float.parseFloat(JOptionPane.showInputDialog("How much to withdraw from " + currAccount));
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				withdraw(withdrawAmount);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			try {
				refresh();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			return;
		case "transfer":
			float transferAmount;
			String acc2;
			try {
				acc2 = JOptionPane.showInputDialog("Which account to transfer to?");
				transferAmount = Float.parseFloat(JOptionPane.showInputDialog("How much to withdraw from " + currAccount + " and deposit to " + acc2 + "?"));
			}
			catch (Exception e) {
				JOptionPane.showMessageDialog(null, "Invalid amount", "Error", JOptionPane.ERROR_MESSAGE);
				return;
			}
			try {
				transfer(acc2, transferAmount);
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			try {
				refresh();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			return;
		case "refresh":
			try {
				refresh();
			} catch (ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			return;
		case "Die":
			try {
				displayMainUI();
			} catch (HeadlessException | ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			return;
		case "status":
			try {
				changeStatus((String) JOptionPane.showInputDialog(frame, "Which status?", currAccount, JOptionPane.PLAIN_MESSAGE, null, new String[] {"Good","Freeze","Inactive"}, "Good"));
			} catch (HeadlessException | ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			return;
		case "add":
			try {
				if (!addUser(JOptionPane.showInputDialog("Which user to add?"))) {
					JOptionPane.showMessageDialog(null, "That user is already on this account", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (HeadlessException | ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			return;
		case "remove":
			try {
				if (!removeUser(JOptionPane.showInputDialog("Which user to remove?"))) {
					JOptionPane.showMessageDialog(null, "That user is not on this account", "Error", JOptionPane.ERROR_MESSAGE);
				}
			} catch (HeadlessException | ClassNotFoundException | IOException e) {
				e.printStackTrace();
			}
			return;
		}
	}
	
	public void refresh() throws ClassNotFoundException, IOException {
		for (String[] logList : currLogs) {
			for (String log : logList) {
				System.out.println(log);
			}
		}
		System.out.println("DIVIDER");
		getLogs();
		for (String[] logList : currLogs) {
			for (String log : logList) {
				System.out.println(log);
			}
		}
		currModel.refresh();
	}
}
