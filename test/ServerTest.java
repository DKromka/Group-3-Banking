import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class ServerTest {

    private BankAccount testAccount;
    private User testUser;

    @Before
    public void setUp() {
        // Initialize necessary objects for testing
        testUser = new User("TestUser", "TestPassword", false);
        testAccount = new BankAccount("TestAccount", 1000.0f, 100.0f, AccountStatus.GOOD);
        testAccount.addUser(testUser.getName());
    }

    @Test
    public void testDeposit() {
        try {
            float initialBalance = testAccount.getBalance();
            float depositAmount = 200.0f;

            // Simulate a deposit
            testAccount.deposit(depositAmount);

            // Check if the balance has been updated correctly
            assertEquals(initialBalance + depositAmount, testAccount.getBalance(), 0.01);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }

    @Test
    public void testWithdrawal() {
        try {
            float initialBalance = testAccount.getBalance();
            float withdrawalAmount = 50.0f;

            // Simulate a withdrawal
            boolean success = testAccount.withdraw(withdrawalAmount);

            // Check if the withdrawal was successful and the balance has been updated correctly
            assertTrue(success);
            assertEquals(initialBalance - withdrawalAmount, testAccount.getBalance(), 0.01);
        } catch (Exception e) {
            fail("Exception thrown: " + e.getMessage());
        }
    }
}
