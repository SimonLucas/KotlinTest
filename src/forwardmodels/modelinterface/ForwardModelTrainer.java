package forwardmodels.modelinterface;

import java.util.ArrayList;
import java.util.HashSet;

import forwardmodels.decisiontree.DecisionTree;
import games.gridgame.GridGameKt;
import games.gridgame.Pattern;

/**
 * 
 * @author mostafa (v1.0 https://github.com/mostafacs/DecisionTree)
 * @author Alexander Dockhorn (v1.1 various bug fixes)
 */

public class ForwardModelTrainer {

	private static ArrayList<String> data;

	HashSet<Pattern> dataset = new HashSet<Pattern>();

	public static ArrayList<String> getDataset() {
		if (data == null || data.size()!=5211){
			data = createDataset();
		}
		return data;
	}

	private static final int[][] standardgamemodel = {
			{0, 0, 0, 1, 0, 0, 0, 0, 0},
			{0, 0, 1, 1, 0, 0, 0, 0, 0}
	};

	private static String applyGameModel(String[] values, int[][] gamemodel){
		//apply player action (flip bit of another gridGame cell or do nothing)
		if (Integer.parseInt(values[9]) != 9)
			values[Integer.parseInt(values[9])] = "" + (1- Integer.parseInt(values[Integer.parseInt(values[9])]));

		int sum = 0;
		for (int i = 0; i < 9; i++){
			if (i==4) continue;
			sum += Integer.parseInt(values[i]);
		}

		return ""+ gamemodel[Integer.parseInt(values[4])][sum];
	}

	private static ArrayList<String> createDataset() {
		return createDataset(standardgamemodel);
	}


	private static ArrayList<String> createDataset(int[][] gamemodel){
		//define attributes
		ArrayList<String> list = new ArrayList<>(5121);
		switch (GridGameKt.getIncludeNeighbourInputs()){
			case PlayerInt: list.add("tl,l,bl,t,c,b,tr,r,br,pint,class"); break;
			case PlayerOneHot: list.add("tl,l,bl,t,c,b,tr,r,ptl,pl,pbl,pt,pc,pb,ptr,pr,pbl,class"); break;
			default: break;
		}

		//fill data set
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

			for(int j = 0; j < 10; j++){
				StringBuilder newinstance = new StringBuilder(tempinstance);
				newinstance.append(""+j);
				newinstance.append(","+applyGameModel(newinstance.toString().split(","), gamemodel));
				list.add(newinstance.toString());
			}
		}
		return list;
	}

	private static float measureAccuracy(DecisionTree tree, ArrayList<String> testData){
		int[] counter = new int[2];
		String line;
		for (int idx = 1; idx < testData.size(); idx++){
			line = testData.get(idx);
			String[] cols = line.split(",");
			String classLabel = cols[cols.length-1];

			if(tree.classify(line).equals(classLabel))
				counter[0]++;
			else
				counter[1]++;
		}

		return (float)counter[0] / (counter[0] + counter[1]);
	}

	private static float measureAccuracy(DecisionTree tree){
		ArrayList<String> testData = getDataset();
		return measureAccuracy(tree, testData);
	}

	public  DecisionTree trainDecisionTree(ArrayList<Pattern> data){
		DecisionTree tree = new DecisionTree();
		ArrayList<String> trainingData = new ArrayList<String>();
		switch (GridGameKt.getIncludeNeighbourInputs()){
			case PlayerInt: trainingData.add("tl,l,bl,t,c,b,tr,r,br,pint,class"); break;
			case PlayerOneHot: trainingData.add("tl,l,bl,t,c,b,tr,r,ptl,pl,pbl,pt,pc,pb,ptr,pr,pbl,class"); break;
			default: break;
		}

		this.dataset.addAll(data);
		int size = dataset.size();

		for (Pattern pattern : this.dataset){
			StringBuilder instance = new StringBuilder();
			for (int i = 0; i < pattern.getIp().size(); i++)
				instance.append("" + pattern.getIp().get(i) + ",");

			instance.append("" + pattern.getOp());

			trainingData.add(instance.toString());
		}
		tree.train(trainingData);
		//System.out.println("Seen patterns: "+ size + "; Accuracy: " + measureAccuracy(tree));
		return tree;
	}

	public static void main(String[] args) {
		// Generates a dataset containing all the patterns of GridGame (default: standard rules)
		//ArrayList<String> dataset = getDataset(gamemodel);
		ArrayList<String> dataset = getDataset();

		DecisionTree tree = new DecisionTree();
		
		// Train your Decision Tree
		//tree.train(new File("dataset.csv"));
		tree.train(dataset);

		// Get the prediction of a single line
		System.out.println(dataset.get(2) + " -> " + tree.classify(dataset.get(2)));

		// Print RootNode display xml structure from your decision tree learning
		//System.out.println(tree.getRootNode());

		// Measure Accuracy. In case no testData is provided, the testset for GridGame standard rules will be generated.
		//System.out.println("Accuracy: " + measureAccuracy(tree, testData));
		System.out.println("Accuracy: " + measureAccuracy(tree));
	}
}
