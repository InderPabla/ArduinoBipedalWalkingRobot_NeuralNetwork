import java.util.ArrayList;
import java.util.Random;

public class NeuralNetwork implements Comparable<NeuralNetwork>{

	private final float BIAS = 0.25f;

    private int[] layers = null;
    ArrayList<float[]> neuronsList;
    ArrayList<ArrayList<float[]>> weightsList;
    
    private int ID;    

    private Random rand;

    int highestNeuronInLayer = 0;
    private float fitness;
    private boolean tested;
    
	public NeuralNetwork(int[] lay, int ID) {
		this.ID = ID;
		this.rand = new Random();
		this.fitness = 0;
		tested = false;
        //deep copy layers array
        this.layers = new int[lay.length];
        for (int i = 0; i < layers.length; i++) {
            this.layers[i] = lay[i];      
            if(lay[i]>highestNeuronInLayer)
            	highestNeuronInLayer= lay[i];
        }
        
        initilizeNeurons();
        initilizeWeights();
        
	}
	
	public NeuralNetwork(NeuralNetwork copy) {
		this.rand = new Random();
		this.fitness = copy.fitness;
		this.tested = copy.tested;
		this.layers = new int[copy.layers.length];
		
		for (int i = 0; i < layers.length; i++) {
            this.layers[i] = copy.layers[i];      
        }
		
		this.highestNeuronInLayer = copy.highestNeuronInLayer;
		
		initilizeNeurons();
		initilizeWeights();
		
		for(int i =0;i<weightsList.size();i++){
			for(int j =0;j<weightsList.get(i).size();j++){
				for(int k =0;k<weightsList.get(i).get(j).length;k++){
					weightsList.get(i).get(j)[k] = copy.weightsList.get(i).get(j)[k];
				}
			}
		}
		
	}
	
	public NeuralNetwork(int ID, float fit, boolean test, int[] lay,  ArrayList<ArrayList<float[]>> copyWeights) {
		this.rand = new Random();
		this.ID = ID;
		this.fitness = fit;
		this.tested = test;
		
		this.layers = new int[lay.length];
		for (int i = 0; i < layers.length; i++) {
            this.layers[i] = lay[i];   
            if(lay[i]>highestNeuronInLayer)
            	highestNeuronInLayer= lay[i];
        }
		
		initilizeNeurons();
		initilizeWeights();
		
		for(int i =0;i<weightsList.size();i++){
			for(int j =0;j<weightsList.get(i).size();j++){
				for(int k =0;k<weightsList.get(i).get(j).length;k++){
					weightsList.get(i).get(j)[k] = copyWeights.get(i).get(j)[k];
				}
			}
		}
	}
	
	//create a static neuron matrix
    private void initilizeNeurons()
    {
        //Neuron Initilization
        neuronsList = new ArrayList<float[]>();

        for (int i = 0; i < layers.length; i++) //run through all layers
        {
            neuronsList.add(new float[layers[i]]); //add layer to neuron list
        }
        
    }

    //create a static weights matrix
    private void initilizeWeights()
    {
        //Weights Initilization
        weightsList = new ArrayList<ArrayList<float[]>>();

        for (int i = 1; i < neuronsList.size(); i++)
        {
        	ArrayList<float[]> layerWeightsList = new ArrayList<float[]>(); //layer weights list

            int neuronsInPreviousLayer = layers[i - 1];

            for (int j = 0; j < neuronsList.get(i).length; j++)
            {
                float[] neuronWeights = new float[neuronsInPreviousLayer]; //neruons weights

                //set the weights randomly between 1 and -1
                for (int k = 0; k < neuronsInPreviousLayer; k++)
                {
                    neuronWeights[k] = randomFloat(-0.5f,0.5f);
                }

                layerWeightsList.add(neuronWeights);
            }

            weightsList.add(layerWeightsList);
        }
    }
    
  //neural network feedword by matrix operation
    public float[] feedforward(float[] inputs)
    {
    	
    	//add inputs to the neurons matrix
        for (int i = 0; i < inputs.length; i++)
        {
            neuronsList.get(0)[i] = inputs[i];
        }
        
        int weightLayerIndex = 0;

        // run through all neurons starting from the second layer
        for (int i = 1; i < neuronsList.size(); i++) //layers
        {
            for (int j = 0; j < neuronsList.get(i).length; j++) //nerons
            {
                float value = BIAS ;

                for (int k = 0; k < neuronsList.get(i - 1).length; k++)
                {
                    value += weightsList.get(weightLayerIndex).get(j)[k] *  neuronsList.get(i - 1)[k];
                }

                neuronsList.get(i)[j] = (float)Math.tanh(value); //(float) sigmoid(value);
            }
            weightLayerIndex++;
        }

        return neuronsList.get(layers.length - 1); //return output field
    }
    
    public static double sigmoid(double x) {
        return (1/( 1 + Math.pow(Math.E,(-1*x))));
      }
    
    public float randomFloat(float min, float max){
    	return rand.nextFloat() * (max - min) + min;
    }
    
    public int getHighestNeuronInLayer(){
    	return highestNeuronInLayer;
    }
    
    public float[] getOutput(){
    	return neuronsList.get(neuronsList.size()-1);
    }
    
    public void setFitness(float fit){
    	this.fitness = fit;
    }
    
    public float getFitness(){
    	return fitness;
    }
    
    public boolean isTested(){
    	return tested;
    }
    
    public void setTested(boolean tested){
    	this.tested = tested;
    }
    
    public void resetNeuronValues(){
    	for(int i = 0; i<neuronsList.size();i++){
    		for(int j = 0; j<neuronsList.get(i).length;j++){
    			neuronsList.get(i)[j] = 0;
        	}
    	}
    }
    /*public static void main(String[] ar){
    	int[] lay = {10,40,40,10};
    	NeuralNetwork network = new NeuralNetwork(lay,0);
    	for(int i = 0; i<5;i++){
    		float out[] = network.feedforward(new float[]{network.randomFloat(-0.5f,0.5f),network.randomFloat(-0.5f,0.5f),network.randomFloat(-0.5f,0.5f),network.randomFloat(-0.5f,0.5f),network.randomFloat(-0.5f,0.5f),network.randomFloat(-0.5f,0.5f),network.randomFloat(-0.5f,0.5f),network.randomFloat(-0.5f,0.5f),network.randomFloat(-0.5f,0.5f),network.randomFloat(-0.5f,0.5f)});
    		for(int j = 0; j<out.length;j++){
    			System.out.print(out[j]+" ");
    		}
    		System.out.print("\n");
    	}
    }*/

	@Override
	public int compareTo(NeuralNetwork other) {
		if(fitness>other.fitness)
			return 1;
		else if(fitness==other.fitness)
			return 0;
		else
			return -1;
	}
	
	public int getID(){
		return ID;
	}
	
	public void setID(int ID){
		this.ID = ID;
	}
	
	public ArrayList<ArrayList<float[]>> getWeightsList(){
		return weightsList;
	}	
	
	public void mutate(){
		for(int i =0;i<weightsList.size();i++){
			for(int j =0;j<weightsList.get(i).size();j++){
				for(int k =0;k<weightsList.get(i).get(j).length;k++){
					float weight = weightsList.get(i).get(j)[k];
					
					float randomNumber1 = randomFloat(1f, 100f); //random number between 1 and 100
                    if (randomNumber1 <= 1)
                    { //if 1
                      //flip sign of weight
                        weight *= -1f;
                    }
                    else if (randomNumber1 <= 2)
                    { //if 2
                      //pick random weight between -1 and 1
                        weight = randomFloat(-0.5f, 0.5f);
                    }
                    else if (randomNumber1 <= 3)
                    { //if 3
                      //randomly increase by 0% to 100%
                        float factor = randomFloat(0f, 1f) + 1f;
                        weight *= factor;
                    }
                    else if (randomNumber1 <= 4)
                    { //if 4
                      //randomly decrease by 0% to 100%
                        float factor = randomFloat(0f, 1f);
                        weight *= factor;
                    }

                    //if(weight>3f || weight <-3f)
                        //weight = UnityEngine.Random.Range(-0.5f, 0.5f);

                    weightsList.get(i).get(j)[k] = weight;
				}
			}
		}
	}
	
	public static ArrayList<ArrayList<float[]>> createZeroWeightList(int layers[]){
		ArrayList<ArrayList<float[]>> weightsList = new ArrayList<ArrayList<float[]>>();
		ArrayList<float[]> neuronsList = new ArrayList<float[]>();

        for (int i = 0; i < layers.length; i++) //run through all layers
        {
            neuronsList.add(new float[layers[i]]); //add layer to neuron list
        }
        
        for (int i = 1; i < neuronsList.size(); i++)
        {
        	ArrayList<float[]> layerWeightsList = new ArrayList<float[]>(); //layer weights list

            int neuronsInPreviousLayer = layers[i - 1];

            for (int j = 0; j < neuronsList.get(i).length; j++)
            {
                float[] neuronWeights = new float[neuronsInPreviousLayer]; //neruons weights

                //set the weights randomly between 1 and -1
                for (int k = 0; k < neuronsInPreviousLayer; k++)
                {
                    neuronWeights[k] = 0f;
                }

                layerWeightsList.add(neuronWeights);
            }

            weightsList.add(layerWeightsList);
        }
        
		return weightsList;
	}
}
