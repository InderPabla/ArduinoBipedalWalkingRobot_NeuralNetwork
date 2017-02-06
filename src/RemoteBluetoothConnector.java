import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.swing.JFrame;

public class RemoteBluetoothConnector  extends JFrame implements Runnable, MouseMotionListener, MouseListener {
	
	private String deviceName = null;
	private String deviceAddress = null;
	private String localServerAddress = null;
	private String nodeJSCommand = null;	
	
	private UUID[] uuidSet = new UUID[1]; //serial port
	private LocalDevice localDevice = null;
	private DiscoveryAgent agent = null;
	
	private RemoteDevice remoteDevice = null;
	private String remoteDeviceAddress = null;

    private boolean connected  = false;
    private boolean found = false; 
    
    public boolean detected = false;
    public boolean send = true;
    
    
    int delay = 1; //25 ms delay in print
	Thread runThread;  //active thread
	Dimension screen = new Dimension(600,600); //screen dimensions
	float mx,my; //mouse x and y positions
	
	long lastTime; //last time of system
	long fps; //frames per second
	
	Image image; //image to be printed out
	Graphics2D g2; 
	
	//buttons and colors
	Rectangle resetButton; 
	Color resetColor;
	Rectangle joltButton; 
	Color joltColor;
	boolean resetDown = false;	
	boolean joltDown = false;	
    
  	StreamConnection connection = null;
	InputStream inputStream = null;
	OutputStream outputStream = null;
	NeuralNetwork network;
    float angles[] = {0,0,0,0};
    float netOut[] = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
    float maxRange = 45;
  
	private boolean debugMode = true;
	private boolean constructorMade = false;
	private String names[] = new String[]{"Joint 1: Top Right","Joint 2: Top Left", "Joint 3: Bottom Left", "Joint 4: Bottom Right"};
	
	public RemoteBluetoothConnector(String name, String address, String serverAddress, String command, boolean debugMode){
		image = new BufferedImage(screen.width,screen.height,BufferedImage.TYPE_INT_ARGB); 
		g2 = (Graphics2D) image.getGraphics(); //graphic set to show image
		
		this.debugMode = debugMode;
		this.deviceName = name;
		this.deviceAddress = address;
		this.localServerAddress = serverAddress;
		this.nodeJSCommand = command;
		
		uuidSet[0] = new UUID(0x1101); //serial port
		
		findRemoteDevice();
		
		if(remoteDevice == null){
			System.out.println("Unable to find device");
			found = false;
		}
		else{
			System.out.println("Found Device: "+deviceName);
    		System.out.println("Device Address: "+deviceAddress);		
    		found = true; 
		}
		
		
		setSize(screen); //set screen size
		setBackground(Color.black); //screen background to black
		setTitle("Robot Control");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().setLayout(null);
		setVisible(true);
		
		
		//image = createImage(screen.width, screen.height); //image creation
		

		
		addMouseMotionListener(this); //adding mouse listener
		
		addMouseListener(this);
		
		resetButton = new Rectangle(10,40,50,50); 
		joltButton = new Rectangle(70,40,50,50); 
		
		network = new NeuralNetwork(new int[]{16,20,20,16},0);
		constructorMade = true;
		
	}
	
	private void findRemoteDevice(){	
		try {
			localDevice = LocalDevice.getLocalDevice();
		} catch (BluetoothStateException error) {
			error.printStackTrace();
			return;
		}
	    
		agent = localDevice.getDiscoveryAgent();
	    RemoteDevice[] deviceArray = agent.retrieveDevices(DiscoveryAgent.CACHED);
		
	    if(deviceArray.length == 0)
	    	return;
	    
	    for(int index = 0; index < deviceArray.length; index++){
	    	String name = null;
	    	String address = null;
	    	
	    	try {
				name = deviceArray[index].getFriendlyName(false);
			} catch (IOException error) {
				name = null;
			}
	    	
	    	address = deviceArray[index].getBluetoothAddress();
	    	
	    	if(deviceName != null && deviceAddress !=null){
	    		if(name.equals(deviceName) && address.equals(deviceAddress)){
		    		remoteDevice = deviceArray[index];
		    		deviceName = name;
		    		deviceAddress = address;
		    		break;
		    	}
	    	}
	    	else if(deviceName != null || deviceAddress !=null){
	    		if(name.equals(deviceName) || address.equals(deviceAddress)){
		    		remoteDevice = deviceArray[index];
		    		deviceName = name;
		    		deviceAddress = address;
		    		break;
		    	}
	    	}
	    	else
	    		remoteDevice = null;
	    }		
	}

	
	public boolean deviceFound(){
		return found;
	}
	
	public boolean deviceConnected(){
		return connected;
	}

	
	/**
     * paint (main update functions which is called)
	 * @param  g  gets drawn
     */
	public void paint(Graphics g) {
		if(constructorMade == true){
			//draw screen
			g2.setColor(Color.gray);
			g2.fillRect(0,0, screen.width, screen.height);
	
			drawNeuralNetwork();
			
			g2.setColor(Color.blue);
			Stroke oldStroke = g2.getStroke();
			g2.setStroke(new BasicStroke(3f));
			/*for(int i = 0; i<4;i++){
				
				float angle = angles[i];
				if(angle<0)
					angle = 360+angle;
				
				float startX = (i*125)+125;
				float startY = 550;
				float endX1   = (float) (startX + 25 * Math.sin(Math.toRadians(angle)));
				float endY1   = (float) (startY + 25 * Math.cos(Math.toRadians(angle)));
				
				float endX2   = (float) (startX - 25 * Math.sin(Math.toRadians(angle)));
				float endY2   = (float) (startY - 25 * Math.cos(Math.toRadians(angle)));
				
				g2.setColor(Color.blue);
				g2.drawOval((int)startX-25, (int)startY-25, 50, 50);
				g2.setColor(Color.green);
				g2.drawLine((int)endX1, (int)endY1, (int)endX2, (int)endY2);
				g2.setColor(Color.yellow);
				g2.drawString(names[i], startX-50, startY-30);
				
			}*/
			
			for(int i = 0; i<2;i++){
				
				float angle = angles[i];
				if(angle<0)
					angle = 360+angle;
				angle = (float) Math.toRadians(angle);
				
				float startX = (i*70)+455;
				float startY = 200;
				
				float centerX = startX+20;
				float centerY = startY+20;
				float endX1   = (float) (centerX + 25 * Math.sin(angle));
				float endY1   = (float) (centerY + 25 * Math.cos(angle));
				
				float endX2   = (float) (centerX - 25 * Math.sin(angle));
				float endY2   = (float) (centerY - 25 * Math.cos(angle));
				
				g2.setColor(Color.blue);
				g2.drawLine((int)endX1, (int)endY1, (int)endX2, (int)endY2);
				g2.setColor(Color.green);
				g2.drawOval((int)startX, (int)startY, 40, 40);
				
			}
			
			for(int i = 3; i>=2;i--){
				
				float angle = angles[i];
				if(angle<0)
					angle = 360+angle;
				angle = (float) Math.toRadians(angle);
				
				float startX = ((3-i)*70)+455;
				float startY = 300;
				
				float centerX = startX+20;
				float centerY = startY+20;
				float endX1   = (float) (centerX + 25 * Math.sin(angle));
				float endY1   = (float) (centerY + 25 * Math.cos(angle));
				
				float endX2   = (float) (centerX - 25 * Math.sin(angle));
				float endY2   = (float) (centerY - 25 * Math.cos(angle));
				
				
				g2.setColor(Color.blue);
				g2.drawLine((int)endX1, (int)endY1, (int)endX2, (int)endY2);
				g2.setColor(Color.green);
				g2.drawOval((int)startX, (int)startY, 40, 40);
				
			}
			
			g2.setStroke(oldStroke);
			
			if(resetDown == true){
				resetColor = Color.green;
				
				if(send == true && connected == true){
					network = new NeuralNetwork(new int[]{16,20,20,16},0);
					angles = new float[]{0,0,0,0};
					
					writeOut("5");
					writeOut("5");
					writeOut("5");
					writeOut("5");
					//wirteByte((byte)5); wirteByte((byte)5); wirteByte((byte)5); wirteByte((byte)5);
					send = false;
				}
			}
			else{
				resetColor = Color.red;
			}
			
			if(joltDown == true){
				joltColor = Color.green;
				
				if(send == true && connected == true){
					
					writeOut("6");
					writeOut("6");
					writeOut("6");
					writeOut("6");
					send = false;
				}
			}
			else{
				joltColor = Color.red;
			}
			
			g2.setColor(resetColor);
			g2.fillRect(resetButton.x, resetButton.y, resetButton.width, resetButton.height);
			g2.setColor(joltColor);
			g2.fillRect(joltButton.x, joltButton.y, joltButton.width, joltButton.height);
			
			//draw frame rate
			g2.setColor(Color.yellow);
			//g2.drawString("Framerate:" + (1000/(System.currentTimeMillis() - lastTime)), 15, 15);
			lastTime = System.currentTimeMillis();	
			
			//draw image
			g.drawImage(image,0,0,this);
	
			//System.out.println(send);
			
			if(send == true && connected == true){
				float radAng[] = new float[16];
				for(int i = 0; i<angles.length;i++){
					
					radAng[i] = (float) Math.toRadians(angles[i])/ (float)Math.toRadians(maxRange);
					if(angles[i] >maxRange-1){
						radAng[i+4] = 1;
					}
					else if(angles[i] <(-maxRange)+1){
						radAng[i+4] = -1;
					}
				}
				
				for(int i = 8; i<16;i++){
					radAng[i] = netOut[i];
					
				}
				
				netOut = network.feedforward(radAng);
				for(int i = 0;i<8;i+=2){
					if(Math.abs(netOut[i])>Math.abs(netOut[i+1])){
						if(netOut[i]>0){
							angles[i/2]+= 15f;
							writeOut("1");
							//wirteByte((byte)1);
						}
						else {
							angles[i/2]+= 5f;
							//wirteByte((byte)2);
							writeOut("2");
							
						}
					}
					else{
						if(netOut[i+1]>0){
							angles[i/2]-= 15f;
							//wirteByte((byte)3);
							writeOut("3");
							
						}
						else {
							angles[i/2]-= 5f;
							//wirteByte((byte)4);
							writeOut("4");
						}
					}
					
					if(angles[i/2]>maxRange)
						angles[i/2] = maxRange;
					else if (angles[i/2]<-maxRange)
						angles[i/2] = -maxRange;
				}
		
				send  = false;
			}
			
			
		}
	}//paint method	
	
	public void drawNeuralNetwork(){
		
		float highest = network.getHighestNeuronInLayer();
		float highestSpaceTaken = ((highest-1)*20);
		ArrayList<ArrayList<Rectangle>> networkRecs = new ArrayList<ArrayList<Rectangle>>();
		
		for(int i = 0; i<network.neuronsList.size();i++){
			ArrayList<Rectangle> recs = new ArrayList<Rectangle>();
			for(int j = 0; j<network.neuronsList.get(i).length;j++){
				float offsetY = ((network.neuronsList.get(i).length-1)*20);
				offsetY = (highestSpaceTaken-offsetY)/2;
				
				
				Rectangle rect = new Rectangle((125*i)+35, (int)((20*j)+offsetY+100) , 25, 15);
				recs.add(rect);

				if(i>0){
					ArrayList<Rectangle> recsPrev = networkRecs.get(i-1);
					for(int k = 0; k<recsPrev.size();k++){
						Rectangle rectPrev = recsPrev.get(k);
						
						if(network.weightsList.get(i-1).get(j)[k]>0)
							g2.setColor(Color.green);
						else 
							g2.setColor(Color.red);
						
						g2.drawLine(rectPrev.x, rectPrev.y, rect.x, rect.y);
						
					}
					
				}
				
				
			}
			
			networkRecs.add(recs);
		}
		
		DecimalFormat f = new DecimalFormat("#.##");
		for(int i = 0; i<network.neuronsList.size();i++){
			for(int j = 0; j<network.neuronsList.get(i).length;j++){
				Rectangle rec = networkRecs.get(i).get(j);
				g2.setColor(Color.black);
				g2.fillRect(rec.x-(rec.width/2), rec.y-(rec.height/2), rec.width, rec.height);
				g2.setColor(Color.white);
				g2.drawString(f.format(network.neuronsList.get(i)[j]),rec.x-(rec.width/2), rec.y-(rec.height/2)+11);
			}
		}
	}
	
	
	@Override
	public void run() {

		try {
			if(debugMode == false){
				connection = (StreamConnection) Connector.open("btspp://" + deviceAddress + ":1;authenticate=false;encrypt=false;master=true;");
				inputStream = connection.openInputStream();
				outputStream = connection.openOutputStream();
				connected = true;
			}
	        while(true){
	        	
	        	repaint(); //repaint every 25 ms
				
				try {
					Thread.sleep(1); //sleep 25 ms
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}	
	        	
				if(debugMode == false && connected == true){
		        	if(inputStream.available()>0){
		        		int data;
		        		while ( ( data = inputStream.read()) > -1 )
		        			if ( data == '\n' ) 
		                        break;
		                    
		        		send = true;
		        		
		        		/*byte[] buffer = new byte[1024];
		        		String buffer_string;
		        		int data;
		        		int len = 0;
		        		
		                while ( ( data = inputStream.read()) > -1 ){
		                    if ( data == '\n' ) {
		                        break;
		                    }
		                    buffer[len++] = (byte) data;
		                }
		                buffer_string = new String(buffer,0,len);
		                System.out.println(buffer_string);
		                if(buffer_string.contains("ok")){
		                	send = true;
		                }*/
		        		
		        		//System.out.print("yes");
			        	/*while(inputStream.available()>0){
			        		System.out.print((char)inputStream.read());
			        	}
			        	System.out.print("\n");*/
			        	
			        	/*if(!detected){
			        		connection.openOutputStream().write("6666".getBytes());
			        	}
			        	detected = true;  */
			        	
			        	
		        	}
		        }
		        
		        
	        }
	        
	        inputStream.close();
		} catch (IOException e) {
			connected = false;	
		} 
		
		try {
			inputStream.close();
		} catch (IOException e) {}
		connected = false;
        
	}
	
	
	public void connectToNodeJsServer(){
    	try {	
    		System.out.println("Calling Node Js Server");
    		Runtime runtime = Runtime.getRuntime();
    		Process process = runtime.exec(nodeJSCommand);
			URL currentUrl = new URL(localServerAddress);
			HttpURLConnection connection = (HttpURLConnection) currentUrl.openConnection();
			connection.getResponseCode();  
	    } catch (IOException error) {
		     error.printStackTrace();
	    }
    }

	public void mouseDragged(MouseEvent ev) {
		mx=ev.getX();
		my=ev.getY();
	}

	@Override
	public void mouseMoved(MouseEvent ev) {
		// TODO Auto-generated method stub
		mx=ev.getX();
		my=ev.getY();
	}


	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub
		if(resetButton.intersects(new Rectangle((int)mx, (int)my,1,1)) && resetDown == false){
			resetDown = true;
		}
		
		if(joltButton.intersects(new Rectangle((int)mx, (int)my,1,1)) && joltDown == false){
			joltDown = true;
		}
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		resetDown = false;
		joltDown = false;
	}
	
	/**
     * writeOut write out to COM7 port of Arduino
	 * @param  s  string to write out
     */
	public void writeOut(String s){
		try {
			
			outputStream.write(s.getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/*public void wirteByte(byte b){
		try {
			outputStream.write(new byte[]{b});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}*/
}
