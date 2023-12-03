import java.awt.HeadlessException;
import java.io.IOException;
import java.net.UnknownHostException;

public class TellerGUIDriver {
	public static void main(String[] args) throws NumberFormatException, UnknownHostException, IOException, HeadlessException, ClassNotFoundException {
		TellerGUI the = new TellerGUI();
		the.run();
	}
}
