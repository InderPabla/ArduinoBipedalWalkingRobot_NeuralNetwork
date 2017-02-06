
public class BurglarAlarm {
	
	private RemoteBluetoothConnector connector= null;
	private Thread connectorThread;
	private boolean debugMode = false;
	public BurglarAlarm(String name,String address,String localServerAddress,String nodeJSCommand){
		connector = new RemoteBluetoothConnector(name,address,localServerAddress,nodeJSCommand,debugMode);
		if(connector.deviceFound() == true){	
			connectorThread = new Thread(connector);
			connectorThread.start();
		}
	}
	
	public static void main(String[] arguments){
		BurglarAlarm burglarAlarm = new BurglarAlarm("HC-06",null,"http://127.0.0.1:8081","node C:\\Users\\Pabla\\Desktop\\ArduinoServo\\Burglar_Alarm_Server.js");
		
	}
}
