//import for class
import java.io.Serializable;
//date
import java.util.Date;

public class Log implements Serializable {
	protected Date date;
	protected String user;
	protected String account;
	protected String action;
	float amount;
	
	
	//constructor to set variables
	public Log(String user,String action, float amount, Date date, String account){
		this.user = user;
		this.action = action;
		this.amount = amount;
		this.date = date;
		this.account = account;	
	}
	
	//overload constructor for non-fund actions
	public Log(String user,String action, Date date, String account){
		this.user = user;
		this.action = action;
		this.amount = -1;
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
	
	public String getAction() {
		return action;
	}

	public Date getDate() {
		return date;
	}
	
	public String getAccount() {
		return account;
	}
	
}
