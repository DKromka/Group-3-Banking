import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)

@SuiteClasses({
	UserTest.class,
	ServerTest.class,
	MessageTest.class,
	LogTest.class,
	BankAccountTest.class,
	ClientGUITest.class,
	TellerGUITest.class
})

public class AllTests {
	
}
