import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;
import org.junit.jupiter.api.Test;
public class MessageTest {
	MessageType t = MessageType.LOGIN_REQ;
	//user/amount/date
	Date date = new Date();
	String d = "Marc" + "2000" + date;
	float f = 500;
	//make a message
	Message m = new Message (t, d, f);
	@Test
	void values_get_assigned() {
		//check if values are not null as well as check expected result
		assertNotNull (m.getData());
		assertNotNull (m.getID());
		assertNotNull (m.getFunds());
		assertNotNull (m.getType());
		assertNotNull(m.getID());
	}
	
	@Test
	public void test_getters() {
		assertEquals(m.getFunds(), f);
		assertEquals(m.getType(), t);
		assertEquals(m.getID(), "1");
	}
	
	@Test
	public void test_data_returned() {
		//is correct data returned?
		String expectedData = d + f;
		assertNotNull (m.getData());
		assertEquals(m.getData(), expectedData);
		
	}
	
}


