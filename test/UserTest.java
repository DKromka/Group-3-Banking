import static org.junit.jupiter.api.Assertions.*;
import java.util.Vector;
import org.junit.jupiter.api.Test;
class UserTest {
	
	String name = "Naomin";
	String pw = "12345";
	boolean teller = false;
	private Vector<String> accounts;
	//create user
	User u = new User(name, pw, teller);
	
	//new users to add
	String newU = "Jack";
	String newU2 = "Roy";
	@Test
	public void addUserTest() {
		assertTrue(u.addAccount(newU));
	}
	
	@Test
	public void DeleteUserTest() {
		u.addAccount(newU);
		assertTrue(u.removeAccount(newU));
		
	}
	
	@Test
	public void test_get_functions() {
		assertEquals(u.getName(), name);
		}
	
	@Test
	public void return_teller_boolean() {
		//expected result = false
		assertFalse(u.isTeller());
	}
	
	@Test
	public void Check_info_returned () {
		String expectedResult = "Naomin|12345|0";
		assertEquals(u.toString(), expectedResult);
	}
}


