import java.util.Vector;

public class BankAccount {
	
	private float balance;
	private float minBalance;
	private Vector<String> users; // Yes this means contains is O(n) :/
	private String name;
	private AccountStatus status;
	
	public BankAccount(String name, float initBalance, float minBalance) {
		this.name = name;
		balance = initBalance;
		this.minBalance = minBalance;
		status = AccountStatus.GOOD;
		users = new Vector<String>();
	}
	
	public BankAccount(String name, float initBalance, float minBalance, AccountStatus status) {
		this.name = name;
		balance = initBalance;
		this.minBalance = minBalance;
		this.status = status;
		users = new Vector<String>();
	}
	
	public float getBalance() {
		return balance;
	}
	
	public String getName() {
		return name;
	}
	
	public AccountStatus getStatus() {
		return status;
	}
	
	public void setStatus(AccountStatus s) {
		status = s;
	}
	
	public boolean deposit(float amount) {
		if (amount <= 0) {return false;}
		balance += amount;
		return true;
	}
	
	public boolean withdraw(float amount) {
		if (amount <= 0 || balance - amount < minBalance) {return false;}
		balance -= amount;
		return true;
	}
	
	public boolean addUser(String user) {
		if (users.contains(user)) {return false;}
		users.add(user);
		return true;
	}
	
	public boolean removeUser(String user) {
		return users.remove(user); // Vectors conveniently already have this boolean-return functionality
	}
	
	public boolean hasUser(String user) {
		return users.contains(user);
	}
	
	public String toString() {
		String result = name + "|" + Float.toString(balance) + "|" + Float.toString(minBalance) + "|";
		switch (status) {
		case GOOD:
			result += "GOOD\n";
			break;
		case FREEZE:
			result += "FREEZE\n";
			break;
		default:
			result += "INACTIVE\n";
			break;
		}
		for (String user : users) {
			result += user + "|";
		}
		return result.substring(0, result.length() - 1);
	}
}
