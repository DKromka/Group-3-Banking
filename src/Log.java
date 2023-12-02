//import for class
import java.io.Serializable;
//date
import java.util.Date;

public class Log implements Serializable {
	protected Date date;
	protected String user;
	protected String account;
	protected MessageType action;
	float amount;
	
	
	//constructor to set variables
	public Log(String user,MessageType action, float amount, Date date, String account){
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
	
	public float getAmount() {	
		return amount;
	}
	
	public MessageType getAction() {
		return action;
	}

	public Date getDate() {
		return date;
	}
	
	public String getAccount() {
		return account;
	}
	
}
