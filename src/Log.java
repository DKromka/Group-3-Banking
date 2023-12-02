//import for class
import java.io.Serializable;
//date
import java.util.Date;

public class Log implements Serializable {
	protected String date;
	protected String user;
	protected String account;
	protected String action;
	protected String amount;
	
	
	//constructor to set variables
	public Log(String user,String action, float amount, Date date, String account){
		this.user = user;
		this.action = action;
		this.amount = String.valueOf(amount);
		this.date = date.toString();
		this.account = account;	
	}
	
	//overload constructor for non-fund actions
	public Log(String user,String action, Date date, String account){
		this.user = user;
		this.action = action;
		this.amount = String.valueOf(-1);
		this.date = date.toString();
		this.account = account;	
	}
	
	public Log(String user,String action, String amount, String date, String account){
		this.user = user;
		this.action = action;
		this.amount = amount;
		this.date = date;
		this.account = account;	
	}
	//getters - public
	
	public String getUser() {	
		return user;
	}
	
	public String getAmount() {	
		return amount;
	}
	
	public String getAction() {
		return action;
	}

	public String getDate() {
		return date;
	}
	
	public String getAccount() {
		return account;
	}
	
	public String toString() {
		return user + "|" + action + "|" + amount + "|" + date;
	}
	
}
