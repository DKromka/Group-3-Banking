package testing;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)

@SuiteClasses({
	User.class,
	TellerHandler.class,
	Teller.class,
	Server.class,
	Message.class,
	log.class,
	Client.class,
	ClientHandler.class,
	BankAccount.class,
	ClientGUI.class,
	TellerGUI.class
})

public class AllTests {
	
}
