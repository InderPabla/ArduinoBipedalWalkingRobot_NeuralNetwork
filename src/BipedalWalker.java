import java.awt.Dimension;
import java.io.IOException;
import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;

public class BipedalWalker {
	
	private RemoteBluetoothConnector connector= null;
	private Thread connectorThread;
	private boolean debugMode = false;
	Dimension screen;
	Dimension drawScreen;
	
	public BipedalWalker(String name,String address,String localServerAddress,String nodeJSCommand){
		screen = new Dimension(600,750);
		drawScreen = new Dimension(600,700);
		connector = new RemoteBluetoothConnector(name,address,localServerAddress,nodeJSCommand,debugMode,drawScreen);
		
		
		JFrame jFrame = new JFrame();
        jFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jFrame.setSize(screen.width, screen.height);
        jFrame.getContentPane().add(connector);
        jFrame.setVisible(true);

        
        jFrame.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		        System.out.println("---CLOSING CONNECTIONS---");
	
		        try {
		        	if(connector.inputStream!=null)
		        		connector.inputStream.close();
				} catch (IOException e) {}
		        
		        try {
		        	if(connector.outputStream!=null)
		        		connector.outputStream.close();
				} catch (IOException e) {}
		        
		        try {
		        	if(connector.connection!=null)
		        		connector.connection.close();
				} catch (IOException e) {}
		        
		        System.exit(0);
		    }
		});
        
		if(connector.deviceFound() == true){	
			connectorThread = new Thread(connector);
			connectorThread.start();
		}
	}
	
	public static void main(String[] arguments){
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
		        if ("Nimbus".equals(info.getName())) {
		            UIManager.setLookAndFeel(info.getClassName());
		            break;
		        }
		    }
	    } 
	    catch (Exception e) {
	       System.out.println("Could not apply numbus look and feel!");
	    }

		
		BipedalWalker burglarAlarm = new BipedalWalker("HC-06",null,"http://127.0.0.1:8081","node C:\\Users\\Pabla\\Desktop\\ArduinoServo\\Burglar_Alarm_Server.js");		
	}
}
