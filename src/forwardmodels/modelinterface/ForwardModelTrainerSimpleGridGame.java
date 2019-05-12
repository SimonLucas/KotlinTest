package forwardmodels.modelinterface;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.HashSet;

import forwardmodels.decisiontree.DecisionTree;
import games.simplegridgame.SimplePattern;
import games.gridgame.InputType;

/**
 * @author Alexander Dockhorn
 */

public class ForwardModelTrainerSimpleGridGame {

	private InputType inputType;
	private DecisionTree tree;
	private HashSet<SimplePattern> uniquepatterns = new HashSet<>();
	private int knownPatterns = 0;
	private ArrayList<String> trainingData;

	public ForwardModelTrainerSimpleGridGame(InputType inputType){
		this.inputType = inputType;

		this.trainingData = new ArrayList<String>();

		switch (inputType){
			case Simple: trainingData.add("tl,l,bl,t,c,b,tr,r,br,class"); break;
			case PlayerInt: trainingData.add("tl,l,bl,t,c,b,tr,r,br,pint,class"); break;
			case PlayerOneHot: trainingData.add("tl,l,bl,t,c,b,tr,r,ptl,pl,pbl,pt,pc,pb,ptr,pr,pbl,class"); break;
            case Sokoban: throw new InvalidParameterException("use ForwardModelTrainerSokoban instead");
            default: break;
		}
	}

	private static ArrayList<String> validation_data;


	public int nrOfKnownPatterns(){
		return uniquepatterns.size();
	}

	public HashSet<SimplePattern> getTrainingData(){
		return uniquepatterns;
	}

	private static ArrayList<String> getValidationData(InputType inputType) {
		if (validation_data == null || validation_data.size()!=5211){
			validation_data = createValidationDataset(inputType);
		}
		return validation_data;
	}

	private static final int[][] standard_game_model = {
			{0, 0, 0, 1, 0, 0, 0, 0, 0},
			{0, 0, 1, 1, 0, 0, 0, 0, 0}
	};

	private static String applyGameModel(String[] values, int[][] gamemodel, InputType type){
		//apply player action (flip bit of another gridGame cell or do nothing)
		if (type != InputType.Simple) {
			if (Integer.parseInt(values[9]) != 9)
				values[Integer.parseInt(values[9])] = "" + (1- Integer.parseInt(values[Integer.parseInt(values[9])]));
		}

		int sum = 0;
		for (int i = 0; i < 9; i++){
			if (i==4) continue;
			sum += Integer.parseInt(values[i]);
		}

		return ""+ gamemodel[Integer.parseInt(values[4])][sum];
	}

	private static ArrayList<String> createValidationDataset(InputType inputType) {
		return createValidationDataset(standard_game_model, inputType);
	}


	private static ArrayList<String> createValidationDataset(int[][] gamemodel, InputType inputType){
		//define attributes
		ArrayList<String> list = new ArrayList<>(5121);

		switch (inputType){
			case PlayerInt: list.add("tl,l,bl,t,c,b,tr,r,br,pint,class"); break;
			case PlayerOneHot: list.add("tl,l,bl,t,c,b,tr,r,ptl,pl,pbl,pt,pc,pb,ptr,pr,pbl,class"); break;
			case Simple: list.add("tl,l,bl,t,c,b,tr,r,class"); break;
			default: break;
		}

		//fill validation_data set
		int size = 9;
		int numRows = (int)Math.pow(2, size);
		boolean[][] bools = new boolean[numRows][size];

		//create binary vector of all possible gridGame patterns
		for(int i = 0; i<bools.length; i++)
		{
			StringBuilder tempinstance = new StringBuilder();

			for(int j = 0; j < bools[i].length; j++)
			{
				int val = bools.length * j + i;
				int ret = (1 & (val >>> j));
				bools[i][j] = ret != 0;
				tempinstance.append(bools[i][j]?"1,":"0,");
			}
			if (inputType == InputType.PlayerInt){
				for(int j = 0; j < 10; j++){
					StringBuilder newinstance = new StringBuilder(tempinstance);
					newinstance.append(j);
					newinstance.append(",");
					newinstance.append(applyGameModel(newinstance.toString().split(","), gamemodel, inputType));
					list.add(newinstance.toString());
				}
			}
			if (inputType == InputType.Simple){
				tempinstance.append(applyGameModel(tempinstance.toString().split(","), gamemodel, inputType));
				list.add(tempinstance.toString());
			}

		}
		return list;
	}

	private float measureAccuracy(DecisionTree tree, ArrayList<String> testData){
		int[] counter = new int[2];
		String line;
		String wrongInstance = "";
		for (int idx = 1; idx < testData.size(); idx++){
			line = testData.get(idx);
			String[] cols = line.split(",");
			String classLabel = cols[cols.length-1];

			if(tree.classify(line).equals(classLabel))
				counter[0]++;
			else {
				counter[1]++;
				wrongInstance = line;
			}
		}
		return (float)counter[0] / (counter[0] + counter[1]);
	}

	public float measureAccuracy(){
		ArrayList<String> testData = getValidationData(inputType);
		return this.measureAccuracy(this.tree, testData);
	}

	public  DecisionTree trainModel(ArrayList<SimplePattern> data){
		DecisionTree tree = new DecisionTree();


		ArrayList<String> validationData = getValidationData(inputType);

		for (SimplePattern pattern : data){
			if (uniquepatterns.add(pattern)){
				StringBuilder instance = new StringBuilder();
				for (int i = 0; i < pattern.getIp().size(); i++) {
					instance.append(pattern.getIp().get(i));
					instance.append(",");
				}
				instance.append(pattern.getOp());
				trainingData.add(instance.toString());
			}
		}
		int size = uniquepatterns.size();
		if (size > knownPatterns){
			tree.train(trainingData);
			this.tree = tree;
		}
		knownPatterns = size;
		/*
		for (String instance : trainingData){
			boolean is_included = false;
			for (String val_instance : validationData){
				if (instance.equals(val_instance))
				{
					is_included = true;
					break;
				}
			}
			if (!is_included) System.out.println("instance: " + instance + " is missing in the validation set");
		}
		*/


		//System.out.println("Seen patterns: "+ size + "; Accuracy: " + measureAccuracy(tree, inputType));
		//System.out.println("" + size + "," + measureAccuracy(tree));
		return tree;
	}

	public DecisionTree trainModelStrings(ArrayList<String> trainingData){
		DecisionTree tree = new DecisionTree();

		ArrayList<String> validationData = getValidationData(inputType);

		int size = trainingData.size();

		for (String instance : trainingData){
			boolean is_included = false;
			for (String val_instance : validationData){
				if (instance.equals(val_instance))
				{
					is_included = true;
					break;
				}
			}
			if (!is_included) System.out.println("instance: " + instance + " is missing in the validation set");
		}

		tree.train(trainingData);
		this.tree = tree;
		//System.out.println("Seen patterns: "+ size + "; Accuracy: " + measureAccuracy(tree, inputType));
		return tree;
	}


	public static void main(String[] args) {
		// Generates a uniquepatterns containing all the patterns of GridGame (default: standard rules)
		//ArrayList<String> uniquepatterns = getValidationData(gamemodel);
		InputType inputType = InputType.Simple;
		ArrayList<String> dataset = getValidationData(inputType);

		ForwardModelTrainerSimpleGridGame fm = new ForwardModelTrainerSimpleGridGame(inputType);

		// Train your Decision Tree
		//tree.train(new File("uniquepatterns.csv"));
		DecisionTree tree = fm.trainModelStrings(dataset);

		// Get the prediction of a single line
		//System.out.println(dataset.get(2) + " -> " + tree.classify(dataset.get(2)));

		// Print RootNode display xml structure from your decision tree learning
		//System.out.println(tree.getRootNode());

		// Measure Accuracy. In case no testData is provided, the testset for GridGame standard rules will be generated.
		//System.out.println("Accuracy: " + measureAccuracy(tree, testData));
		System.out.println("Accuracy: " + fm.measureAccuracy());
	}
}
