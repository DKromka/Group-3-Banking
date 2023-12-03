import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import org.junit.jupiter.api.Test;
public class LogTest {
	String name = "Jhon";
	String type = "deposit";
	float amount = 200;
	Date date = new Date();
	String Acc = "1";
	//create a log
	Log info = new Log (name, type, amount, date, Acc);
	
	@Test
	public void checkValuesNotNull() {
		assertNotNull(info.getAccount());
		assertNotNull(info.getAccount());
		assertNotNull(info.getUser());
	}
	
	public void Get_user_information() {
		assertEquals(name, info.getUser());
		assertEquals(Acc, info.getAccount());
	}
	
	public void info_to_be_logged () {
		assertEquals(type, info.getAction());
		assertEquals(date, info.getDate());
		
	}
}


