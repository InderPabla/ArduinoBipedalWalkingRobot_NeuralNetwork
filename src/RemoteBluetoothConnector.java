import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import javax.bluetooth.BluetoothConnectionException;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.UUID;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class RemoteBluetoothConnector  extends JPanel implements Runnable, MouseMotionListener, MouseListener {
	
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
    
    
    int delay = 25; //25 ms delay in print
	Thread runThread;  //active thread
	Dimension screen; /*= new Dimension(600,600); *///screen dimensions
	float mx,my; //mouse x and y positions
	
	long lastTime; //last time of system
	long fps; //frames per second
	
	BufferedImage bufferedImage; //image to be printed out
	Graphics2D g2; 
	
  	StreamConnection connection = null;
	InputStream inputStream = null;
	OutputStream outputStream = null;
	//NeuralNetwork network = null;
	NeuralNetwork[] networkPopulation = null;
    float angles[] = {0,0,0,0};
    //float netOut[];
    float maxRange = 45;
  
	private boolean debugMode = true;
	private boolean constructorMade = false;
	boolean connectorStarted = false;
	boolean generatePriority = false;
	boolean resetPriority = false;
	
	JButton generate;
	JButton save;
	JButton load;
	JButton previous;
	JButton next;
	JButton test;
	JButton stopTest;
	JButton finished;
	JTextField fitness;
	JButton applyFitness;
	
	int recievedOkCount = 0;
	int sentDataCount = 0;
	
	int testTime = 10;
	boolean isTesting = false;
	int testIndex= 0;
	int populationSize = 10;
	int generation = 1;
	float[] chosen = new float[16];
	
	int[] layer = new int[]{16,20,20,24};
	
	public RemoteBluetoothConnector(String name, String address, String serverAddress, String command, boolean debugMode, Dimension screen){
		this.screen = screen;
		this.debugMode = debugMode;
		this.deviceName = name;
		this.deviceAddress = address;
		this.localServerAddress = serverAddress;
		this.nodeJSCommand = command;
		
		networkPopulation = new NeuralNetwork[populationSize];
		bufferedImage = new BufferedImage(screen.width,screen.height-100,BufferedImage.TYPE_INT_ARGB); 
		g2 = (Graphics2D) bufferedImage.getGraphics(); //graphic set to show image
		
		
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

		setBackground(SystemColor.activeCaption);
		setLayout(null);
		addMouseMotionListener(this); //adding mouse listener
		addMouseListener(this);
		
		
		generate = new JButton("Generate");
		generate.setFont(new Font(generate.getFont().getName(),Font.BOLD,generate.getFont().getSize()+1));
		generate.setBounds(10,screen.height-90,130,40);
		generate.setVisible(true);
		generate.setEnabled(false);
		this.add(generate);
		
		generate.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
		       generateButtonAction();
		    }
		});
		
		save = new JButton("Save");
		save.setFont(new Font(save.getFont().getName(),Font.BOLD,save.getFont().getSize()+1));
		save.setBounds(10,screen.height-40,60,40);
		save.setVisible(true);
		this.add(save);
		save.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
		       System.out.println("Save");
		       File saveFile = new File("generation_neural_network.net");
		       if(saveFile.exists()){
		    	   saveFile.delete();
		       }
		       
		       try {
		    	   saveFile.createNewFile();
		    	   PrintWriter writer = new PrintWriter(saveFile);

		    	   for(int i = 0; i<populationSize;i++){
			    	   	NeuralNetwork net = networkPopulation[i];
			    	   	writer.print(net.getFitness()+" "+(net.isTested()==true?"1 ":"0 "));
			    	   	ArrayList<ArrayList<float[]>> weightsList = net.getWeightsList();
			    	   	
			    	   	for(int a =0;a<weightsList.size();a++){
			    	   		for(int j =0;j<weightsList.get(a).size();j++){
			    	   			for(int k =0;k<weightsList.get(a).get(j).length;k++){
			    	   				writer.print(weightsList.get(a).get(j)[k]+" ");
			    	   			}
			    	   		}
			   			}
			       }
		    	   
		    	   writer.close(); 
		       } 
		       catch (IOException e) {
					e.printStackTrace();
		       }
		       
		       
		    }
		});
		
		load = new JButton("Load");
		load.setFont(new Font(load.getFont().getName(),Font.BOLD,load.getFont().getSize()+1));
		load.setBounds(80,screen.height-40,60,40);
		load.setVisible(true);
		this.add(load);
		load.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				testIndex = 0;
				File loadFile = new File("generation_neural_network.net");
				ArrayList<ArrayList<float[]>> zeroWeightsList = NeuralNetwork.createZeroWeightList(layer);
				if(loadFile.exists()){
		    	   try {
					Scanner scan = new Scanner(loadFile);
					for(int i = 0; i<populationSize;i++){
						float fit = scan.nextFloat();
						float state = scan.nextFloat();
						
						for(int a =0;a<zeroWeightsList.size();a++){
			    	   		for(int j =0;j<zeroWeightsList.get(a).size();j++){
			    	   			for(int k =0;k<zeroWeightsList.get(a).get(j).length;k++){
			    	   				zeroWeightsList.get(a).get(j)[k] = scan.nextFloat();
			    	   			}
			    	   		}
			   			}
						
						NeuralNetwork net = new NeuralNetwork(i,fit,state==0?false:true,layer,zeroWeightsList);
						networkPopulation[i] = net;
					}
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    	   
		       	}
		       	else{
		       		JOptionPane.showMessageDialog(null, "Save file \"generation_neural_network.net\" does not exist.", "Load", JOptionPane.PLAIN_MESSAGE);
		       	}
		    }
		});
		
		next = new JButton("Next");
		next.setFont(new Font(next.getFont().getName(),Font.BOLD,next.getFont().getSize()+1));
		next.setBounds(160,screen.height-90,100,40);
		next.setVisible(true);
		this.add(next);
		next.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
		       System.out.println("next");
		       if(testIndex == populationSize-1)
		    	   testIndex = 0;
		       else
		    	   testIndex++;
		    }
		});
		
		previous = new JButton("Previous");
		previous.setFont(new Font(previous.getFont().getName(),Font.BOLD,previous.getFont().getSize()+1));
		previous.setBounds(160,screen.height-40,100,40);
		previous.setVisible(true);
		this.add(previous);
		previous.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
		       System.out.println("previous");
		       if(testIndex == 0)
		    	   testIndex = populationSize-1;
		       else
		    	   testIndex--;
		    }
		});
		
		test = new JButton("Test");
		test.setFont(new Font(test.getFont().getName(),Font.BOLD,test.getFont().getSize()+1));
		test.setBounds(280,screen.height-90,100,40);
		test.setVisible(true);
		this.add(test);
		test.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
		       System.out.println("tesddt");
		       resetAction();
		       isTesting = true;
		    }
		});
		
		stopTest = new JButton("Stop Test");
		stopTest.setFont(new Font(stopTest.getFont().getName(),Font.BOLD,stopTest.getFont().getSize()+1));
		stopTest.setBounds(280,screen.height-40,100,40);
		stopTest.setVisible(true);
		this.add(stopTest);
		stopTest.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
			   resetAction();
		       isTesting = false;
		       //
		    }
		});
		
		applyFitness = new JButton("<html>Apply<br />Fitness</html>");
		applyFitness.setFont(new Font(applyFitness.getFont().getName(),Font.BOLD,applyFitness.getFont().getSize()));
		applyFitness.setBounds(390,screen.height-90,100,40);
		applyFitness.setVisible(true);
		this.add(applyFitness);
		applyFitness.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
		       System.out.println("Apply Fitness");
		       String fitStr = fitness.getText();
		       try{
		    	   float fit = Float.parseFloat(fitStr);
		    	   networkPopulation[testIndex].setFitness(fit);
		    	   networkPopulation[testIndex].setTested(true);
		       }
		       catch(NumberFormatException err){}
		       
		    }
		});
		
		fitness = new JTextField(50);
		fitness.setFont(new Font(fitness.getFont().getName(),Font.BOLD,fitness.getFont().getSize()+10));
		fitness.setBounds(390,screen.height-40,100,40);
		fitness.setVisible(true);
		this.add(fitness);
		
		finished = new JButton("Finished");
		finished.setFont(new Font(finished.getFont().getName(),Font.BOLD,finished.getFont().getSize()));
		finished.setBounds(500,screen.height-90, 80, 90);
		finished.setVisible(true);
		this.add(finished);
		finished.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
		       System.out.println("Finished");
		       boolean allTested = true;
		       String notTestesNetworks = "Some networks still need to be tested.\nThese include";
		       for(int i = 0; i<populationSize;i++){
		    	   if(networkPopulation[i].isTested() == false){
		    		   allTested = false;
		    		   notTestesNetworks+=" "+(i+1);
		    	   }
		       }
		       notTestesNetworks+=".";
		       
		       if(allTested == false){
		    	   JOptionPane.showMessageDialog(null, notTestesNetworks, "Test", JOptionPane.PLAIN_MESSAGE);
		       }
		       else{
		    	   populateNewGeneration();
		       }
		    }
		});
		
		
		this.setVisible(true);
		
		
		//image = createImage(screen.width, screen.height); //image creation
		
		
		/*resetButton = new Rectangle(10,40,50,50); 
		joltButton = new Rectangle(70,40,50,50); */
		
		//network = new NeuralNetwork(new int[]{16,20,20,16},0);

		/**/

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

	public void populateNewGeneration(){
		generation++;
		Arrays.sort(networkPopulation);
		
		/*for(int i = 0; i<networkPopulation.length;i++)
			System.out.println(networkPopulation[i].getID());*/
		
		for(int i = populationSize/2,j=0; i<populationSize;i++,j++){
			networkPopulation[i].setID(i);
			networkPopulation[i].resetNeuronValues(); 
			networkPopulation[i].setFitness(0.1f);
			networkPopulation[i].setTested(false);
			NeuralNetwork copy = new NeuralNetwork(networkPopulation[i]);
			copy.mutate();
			copy.setFitness(0);
			copy.setID(j);
			copy.resetNeuronValues();
			copy.setTested(false);
			networkPopulation[j] = copy;
		}
		
		testIndex = 0;
		
	}
	
	
	/**
     * paint (main update functions which is called)
	 * @param  g  gets drawn
     */
	/*public void paint(Graphics g) {*/
	public void paintComponent(Graphics g) {
        super.paintComponent(g);
		if(constructorMade == true){
			drawBackground();
			drawJoints();
			drawNeuralNetwork();
			drawButtons();
			g.drawImage(bufferedImage,0,0,this);
			
			buttonPressedActions();

			//System.out.println(send +" "+connected+" "+generatePriority);
			if(isTesting == true && send == true && connected == true && generatePriority == false){
				fireNetwork();
				send = false;
			}
			else if(generatePriority == true){
				generateButtonAction();
			}
			else if (resetPriority == true){
				resetAction();
			}
			
		}
		
		
	}//paint method	
	
	public void resetAction() {
		resetPriority = false;
		
	    if(send == true ){
	    	send = false;
	    	writeOut("5");writeOut("5");writeOut("5");writeOut("5");
	    	angles = new float[]{0,0,0,0};
	    	if(networkPopulation[testIndex]!=null)
	    		networkPopulation[testIndex].resetNeuronValues();
	    }
	    else{
	    	resetPriority = true;
	    }
	}

	public void generateButtonAction() {
		//network = new NeuralNetwork(new int[]{16,20,20,16},0);
		generatePriority = false;
		
	    if(send == true ){
	    	send = false;
	    	
	    	writeOut("5");writeOut("5");writeOut("5");writeOut("5");
	    	
	    	//netOut = new float[20];
	    	for(int i=0;i<populationSize;i++)
	    		networkPopulation[i] = new NeuralNetwork(layer,i);
	    			
	    }
	    else{
	    	generatePriority = true;
	    }
	}
	

	public void fireNetwork(){

		if(/*network!=null*/networkPopulation[testIndex]!=null){
			
			float input[] = new float[16];
			for(int i = 0; i<angles.length;i++){
				
				input[i] = (float) Math.toRadians(angles[i])/ (float)Math.toRadians(maxRange);
				if(angles[i] >maxRange-1){
					input[i+4] = 1;
				}
				else if(angles[i] <(-maxRange)+1){
					input[i+4] = -1;
				}
			}
			
			float netOut[] = networkPopulation[testIndex].getOutput();
			for(int i = 8,j=16; i<input.length;i++,j++){
				input[i] = netOut[j];
				
			}

			netOut = networkPopulation[testIndex].feedforward(input);
			for(int j = 0; j<chosen.length;j++){
				chosen[j] = 0;
			}
			
			for(int i = 0;i<16;i+=4){
				int index = maxIndex(new float[]{netOut[i],netOut[i+1],netOut[i+2],netOut[i+3]});
				
				if(index == 0){
					chosen[i] = 1;
					angles[i/4]+= 15f;
					writeOut("1");
				}
				else if(index == 1){
					chosen[i+1] = 1;
					angles[i/4]+= 5f;
					writeOut("2");
				}
				else if(index == 2){
					chosen[i+2] = 1;
					angles[i/4]-= 15f;
					writeOut("3");		
				}
				else {
					chosen[i+3] = 1;
					angles[i/4]-= 5f;
					writeOut("4");
				}
				
				if(angles[i/4]>maxRange)
					angles[i/4] = maxRange;
				else if (angles[i/4]<-maxRange)
					angles[i/4] = -maxRange;
			}
			
			//send  = false;
			
			/*float radAng[] = new float[16];
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
					}
					else {
						angles[i/2]+= 5f;
						writeOut("2");
						
					}
				}
				else{
					if(netOut[i+1]>0){
						angles[i/2]-= 15f;
						writeOut("3");
						
					}
					else {
						angles[i/2]-= 5f;
						writeOut("4");
					}
				}
				
				if(angles[i/2]>maxRange)
					angles[i/2] = maxRange;
				else if (angles[i/2]<-maxRange)
					angles[i/2] = -maxRange;
			}*/
	
			
		}
	}
	
	public int maxIndex(float[] array){
		int maxIndex = 0;
	    for (int i = 1; i < array.length; i++) {
	        float newnumber = array[i];
	        if (newnumber > array[maxIndex]) {
	            maxIndex = i;
	        }
	    }
	    
	    return maxIndex;
	}
	
	public void buttonPressedActions(){
		/*if(resetDown == true){
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
		}*/
	}
	
	public void drawButtons(){
		/*g2.setColor(resetColor);
		g2.fillRect(resetButton.x, resetButton.y, resetButton.width, resetButton.height);
		g2.setColor(joltColor);
		g2.fillRect(joltButton.x, joltButton.y, joltButton.width, joltButton.height);*/
	}
	
	public void drawBackground(){
		g2.setColor(Color.gray);
		g2.fillRect(0,0, screen.width, screen.height-100);
		
		
		Font oldFont = g2.getFont();
		Font newFont = new Font(oldFont.getFontName(), Font.BOLD, oldFont.getSize()+1);
		g2.setFont(newFont);
		g2.setColor(Color.yellow);
		g2.drawString("Recv Ok   Count: "+recievedOkCount, 5, 15);
		g2.drawString("Sent Data Count: "+sentDataCount/4, 5, 30);
		
		if(connected == true){
			if(networkPopulation[testIndex]!=null){
				g2.drawString("State: ",5,50);
				g2.drawString("Fitness: ",5,65);
				
				if(networkPopulation[testIndex].isTested())
					g2.setColor(Color.green);
				else
					g2.setColor(Color.red);
				g2.drawString(networkPopulation[testIndex].isTested() == true?"Tested":"Not Tested",50,52);
				
				
				g2.drawString(networkPopulation[testIndex].getFitness()+"",60,67);
			}
		}
		
		
		newFont = new Font(oldFont.getFontName(), Font.BOLD, oldFont.getSize()+5);
		g2.setFont(newFont);
		g2.setColor(Color.green);
		g2.drawString("Network: "+(testIndex+1)+"/"+populationSize,260,15);
		
		g2.drawString("Generation: "+generation,430,15);
		
		g2.setFont(oldFont);

	}
	
	public void drawJoints(){
		g2.setColor(Color.blue);
		Stroke oldStroke = g2.getStroke();
		g2.setStroke(new BasicStroke(3f));

		
		for(int i = 0; i<2;i++){
			
			float angle = angles[i];
			if(angle<0)
				angle = 360+angle;
			angle = (float) Math.toRadians(angle);
			
			float startX = (i*70)+455;
			float startY = 200;
			//System.out.println(startX+" "+startY);
			float centerX = startX+20;
			float centerY = startY+20;
			float endX1   = (float) (centerX + 19 * Math.sin(angle));
			float endY1   = (float) (centerY + 19 * Math.cos(angle));
			
			float endX2   = (float) (centerX - 19 * Math.sin(angle));
			float endY2   = (float) (centerY - 19 * Math.cos(angle));
			
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
			//System.out.println(startX+" "+startY);
			float centerX = startX+20;
			float centerY = startY+20;
			float endX1   = (float) (centerX + 19 * Math.sin(angle));
			float endY1   = (float) (centerY + 19 * Math.cos(angle));
			
			float endX2   = (float) (centerX - 19 * Math.sin(angle));
			float endY2   = (float) (centerY - 19 * Math.cos(angle));
			
			
			g2.setColor(Color.blue);
			g2.drawLine((int)endX1, (int)endY1, (int)endX2, (int)endY2);
			g2.setColor(Color.green);
			g2.drawOval((int)startX, (int)startY, 40, 40);
			
		}

		g2.drawLine(455+20+20, 200+20, 525, 200+20);
		g2.drawLine(455+20+20, 300+20, 525, 300+20);
		
		g2.drawLine(455+20, 200+20+20, 455+20, 300);
		g2.drawLine(525+20, 200+20+20, 525+20, 300);
		
		Font oldFont = g2.getFont();
		Font newFont = new Font(oldFont.getFontName(), Font.BOLD, oldFont.getSize()+1);

		g2.setColor(Color.white);
		g2.setFont(newFont);
		
		g2.drawString("Joint 1", 455+1, 200-10);
		g2.drawString("Joint 2", 525+1, 200-10);
		g2.drawString("Joint 3", 455+1, 300+10+50);
		g2.drawString("Joint 4", 525+1, 300+10+50);
		
		g2.setFont(oldFont);
		g2.setStroke(oldStroke);
	}
	
	public void drawNeuralNetwork(){
		
		if(networkPopulation[testIndex]!=null){
			NeuralNetwork network = networkPopulation[testIndex];
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
				if(i==0){
					for(int j = 0; j<network.neuronsList.get(i).length;j++){
						
						Rectangle rec = networkRecs.get(i).get(j);
						if(j>=8){
							g2.setColor(new Color(128,0,128));
						}
						else{
							g2.setColor(Color.black);
						}
						
						g2.fillRect(rec.x-(rec.width/2), rec.y-(rec.height/2), rec.width, rec.height);
						g2.setColor(Color.white);
						g2.drawString(f.format(network.neuronsList.get(i)[j]),rec.x-(rec.width/2), rec.y-(rec.height/2)+11);
					}
				}
				else if(i == network.neuronsList.size()-1){
					for(int j = 0; j<network.neuronsList.get(i).length;j++){
						
						Rectangle rec = networkRecs.get(i).get(j);
						if(j<chosen.length && chosen[j] ==1){
							g2.setColor(Color.red);
						}
						else if (j>=chosen.length){
							g2.setColor(new Color(128,0,128));
						}
						else{
							g2.setColor(Color.black);
						}
						
						g2.fillRect(rec.x-(rec.width/2), rec.y-(rec.height/2), rec.width, rec.height);
						g2.setColor(Color.white);
						g2.drawString(f.format(network.neuronsList.get(i)[j]),rec.x-(rec.width/2), rec.y-(rec.height/2)+11);
					}
				}
				else{
					for(int j = 0; j<network.neuronsList.get(i).length;j++){
						Rectangle rec = networkRecs.get(i).get(j);
						g2.setColor(Color.black);
						g2.fillRect(rec.x-(rec.width/2), rec.y-(rec.height/2), rec.width, rec.height);
						g2.setColor(Color.white);
						g2.drawString(f.format(network.neuronsList.get(i)[j]),rec.x-(rec.width/2), rec.y-(rec.height/2)+11);
					}
				}
			}
		}
	}
	
	/*public void resetJoints(){
		System.out.println("oki3!");
		if(connected==true){

			writeOut("5");
		    writeOut("5");
		    writeOut("5");
		    writeOut("5");
			send = false;
		    System.out.println("Reseting joint");
		}
	}*/
	
	
	Runnable connectorRun = new Runnable(){
		    public void run() {
		    	connectorStarted = true;
		    	try {
		    		
		    		connection = (StreamConnection) Connector.open("btspp://" + deviceAddress + ":1;authenticate=false;encrypt=false;master=true;");
					inputStream = connection.openInputStream();
					outputStream = connection.openOutputStream();
					
					generatePriority = true;
					connected = true;
					generateButtonAction();
					generate.setEnabled(true);
					
					/*writeOut("5");writeOut("5");writeOut("5");writeOut("5");
					network = new NeuralNetwork(new int[]{12,20,20,20},0);
			    	angles = new float[]{0,0,0,0};
					send = false;*/
					
					
					//generateButtonAction();
		    	} catch (IOException err) {
		    		System.out.println("Bluetooth Connection time out! To connect start connection again!");
				}
				
				
		    }
	};
	
	@Override
	public void run() {
		
		try {
			if(debugMode == false && connectorStarted==false){
				Thread connectorThread = new Thread(connectorRun);
				connectorThread.start();
			}
			
	        while(true){			
				try {
					repaint(); 
					Thread.sleep(1); //sleep 25 ms
				} 
				catch (InterruptedException e) {
					e.printStackTrace();
					break;
				}	
	        	
				if(debugMode == false && connected == true){
		        	if(send==false && inputStream.available()>0){
		        		
		        		int data;
		        		while ( ( data = inputStream.read()) > -1 )
		        			if ( data == '\n' ) 
		                        break;
		        		//System.out.println("Recieved");    
		        		send = true;
		        		recievedOkCount++;
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
	        
	        //inputStream.close();
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
		/*if(resetButton.intersects(new Rectangle((int)mx, (int)my,1,1)) && resetDown == false){
			resetDown = true;
		}
		
		if(joltButton.intersects(new Rectangle((int)mx, (int)my,1,1)) && joltDown == false){
			joltDown = true;
		}*/
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub
		/*resetDown = false;
		joltDown = false;*/
	}
	
	/**
     * writeOut write out to COM7 port of Arduino
	 * @param  s  string to write out
     */
	public void writeOut(String s){
		try {
			sentDataCount++;
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
