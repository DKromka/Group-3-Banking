import static org.junit.jupiter.api.Assertions.*;
import java.util.Date;

import org.junit.Before;
import org.junit.jupiter.api.Test;
public class LogTest {

	String name = "Jhon";
		String type = "Deposit";
		float amount = 200;
		Date date = new Date();
		String Acc = "1";
		//create a log
		Log info = new Log (name, type, amount, date, Acc);

	
	@Test
	public void test_check_Values_Not_Null() {
		assertNotNull(info.getUser());
		assertNotNull(info.getAction());
		assertNotNull(info.getAmount());
		assertNotNull(info.getDate());
		assertNotNull(info.getAccount());
	}
	
	public void test_Get_user_information() {
		assertEquals(name, info.getUser());
		assertEquals(Acc, info.getAccount());
	}
	
	public void testGetAction() {
		assertEquals(type,info.getAction());
	}
	
	public void test_Get_Amount() {
		assertEquals("200",info.getAmount());
		assertNotEquals(amount,info.getAmount());	
	}
	
	public void test_Get_Date() {
		assertEquals(date.toString(),info.getDate());
		assertNotEquals(date,info.getDate());
	}
}


