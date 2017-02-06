import java.util.ArrayList;
import java.util.Random;

public class NeuralNetwork {

	private final float BIAS = 0.25f;

    private int[] layers = null;
    //private float[][] neurons = null;
    ArrayList<float[]> neuronsList;
    //private float[][][] weights = null;
    ArrayList<ArrayList<float[]>> weightsList;
    
    private int ID;    
	
   
    private Random rand;

    int highestNeuronInLayer = 0;
    
	public NeuralNetwork(int[] lay, int ID) {
		this.ID = ID;
		this.rand = new Random();
		
        //deep copy layers array
        this.layers = new int[lay.length];
        for (int i = 0; i < layers.length; i++)
        {
            this.layers[i] = lay[i];      
            if(lay[i]>highestNeuronInLayer)
            	highestNeuronInLayer= lay[i];
        }
        
        initilizeNeurons();
        initilizeWeights();
        
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
            
            //float [][] out = null;
            //layerWeightsList.toArray(out);
            weightsList.add(layerWeightsList);
        }

        //weightsList.toArray(weights); //convert list to array*/
    }
    
  //neural network feedword by matrix operation
    public float[] feedforward(float[] inputs)
    {
        
    	/*System.out.print("Before: ");
    	for(int i = 0;i<neuronsList.get(0).length;i++){
    		System.out.print(neuronsList.get(0)[i]+" ");
    	}
    	System.out.print("\n");*/
    	
    	//add inputs to the neurons matrix
        for (int i = 0; i < inputs.length; i++)
        {
            neuronsList.get(0)[i] = inputs[i];
        }
        
        /*System.out.print("After: ");
    	for(int i = 0;i<neuronsList.get(0).length;i++){
    		System.out.print(neuronsList.get(0)[i]+" ");
    	}
    	System.out.print("\n");*/
        
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

}
