//import for class
import java.io.Serializable;

//implement serializable
public class Message implements Serializable {
	//variables
	static protected int ID;
	protected MessageType type;
	protected String data;
	protected float funds;
	
	//constructor to set variables
	public Message(MessageType type, String data, float funds){
		ID++;
		this.type = type;
		this.data = data;
		this.funds = funds;
		
	}
	
	public Message(MessageType type, String data){
		ID++;
		this.type = type;
		this.data = data;
		this.funds = 0;
		
	}
	
	//getters- get ID/Type/data/funds  (public)
	public int getID() {
		return this.ID;
	}
	public MessageType getType(){
		return this.type;
	}
	
	public String getData(){
		return this.data;
	}
	
	public float getFunds(){
		return this.funds;
	}
}
