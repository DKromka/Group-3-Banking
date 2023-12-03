import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;
import org.junit.jupiter.api.Test;
class BankAccountTest {
	float balance = 100;
	float minBalance = 0;
	Vector<String> users = new Vector<String>();
	String name = "Alex";
	AccountStatus status = AccountStatus.GOOD;
	//new bank account
	BankAccount ba = new BankAccount(name, balance, minBalance);
	
	@Test
	public void testing_get_name () {
		String expectedResult = name;
		//not null
		assertNotNull (ba.getName());
		//compare expected result with actual result.
		assertEquals (expectedResult,ba.getName());
	}
	
	@Test
	public void testing_get_balance() {
		float er = balance;
		assertEquals(balance, ba.getBalance(), er);
	}
	
	@Test
	public void testing_get_Status() {
		AccountStatus expectedResult = status;
		assertEquals(expectedResult, ba.getStatus());
	}
	
	@Test
	public void deposit_more_money_test() {
		float depositAmount = 15;
		//== true if money was deposited
		assertTrue(ba.deposit(depositAmount));
	}
	
	
	@Test
	public void add_user_test() {
		//add users to vector
		String u1 = "Alex";
		String u2 = "Jazmine";
		
		assertTrue(ba.addUser(u1));
		assertTrue(ba.addUser(u2));
	
	}
	
	
	//new bank account
	BankAccount ba2 = new BankAccount("Jazmine", 5000, 100);
	String newUser1 = "Jazmine";
	
	@Test
	void cant_widthdraw_money_testcase(){
		float w = 6000;
		assertFalse(ba2.withdraw(w));
	}
}
