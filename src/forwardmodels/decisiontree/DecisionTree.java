package forwardmodels.decisiontree;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import forwardmodels.decisiontree.conditions.Condition;
import forwardmodels.decisiontree.conditions.EqualCondition;
import games.gridgame.Pattern;


/**
 *
 * @author mostafa (v1.0 https://github.com/mostafacs/DecisionTree)
 * @author Alexander Dockhorn (v1.1)
 */
public class DecisionTree {

	private int classesCount;
	/**
	 * Used to determine if current attribute already used or not
	 */
	private BitSet columns;
	/**
	 * Classes set of the training data
	 */
	private Set<String> classes;
	private int rowsCount;
	private int columnsCount;
	private Node rootNode;
	private File psvFile;
	private ArrayList<String> data;
	private boolean[] classValues;

	public DecisionTree() {

		this.columns = new BitSet(columnsCount);
		classes = new HashSet<String>();
		rootNode = new Node();
	}

	/**
	 * @param row
	 * @return
	 * Classify new patterns encoded as String
	 */
	public String classify(String row) {

		String[] attrs = row.split(",");

		return classify(attrs, rootNode);
	}

	/**
	 * @param pattern
	 * @return
	 * Classify new patterns encoded as SimplePattern object
	 */
	public String classify(Pattern pattern) {

		String[] attrs = new String[pattern.getIp().size()];
		for (int i = 0; i < pattern.getIp().size(); i++)
			attrs[i] = "" + pattern.getIp().get(i);

		return classify(attrs, rootNode);
	}


	/**
	 * 
	 * @param attrs
	 * @param node
	 * @return
	 * Private Method to Find a Classification of new pattern
	 */
	private String classify(String[] attrs, Node node) {

		if (node.isLeaf()) {
			return node.getLabel();
		}
		String currentValue = attrs[node.getIndex()];
		for (Condition condition : node.getForks()) {
			if (condition.test(currentValue)) {
				return classify(attrs, condition.getNextNode());
			}
		}

		return "Cannot Find Class -- Please Learn Tree with more examples";

	}

	/**
	 * Train the tree from the psv File
	 * 
	 * @param psvFile
	 */
	public void train(File psvFile) {
		try {
			this.psvFile = psvFile;
			findClasses(psvFile);
			BitSet rows = new BitSet(rowsCount);
			for (int i = 0; i < rowsCount; i++) {
				rows.set(i);
			}
			buildTree(rootNode, rows);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Class count: " + this.classesCount);
		System.out.println("Columns count: " + this.columnsCount);
		System.out.println("Rows count: " + this.rowsCount);
	}


	/**
	 * Train the tree from an ArrayList of Strings
	 *
	 * @param data
	 */
	public void train(ArrayList<String> data) {
		try {
			this.data = data;
			findClasses(data);
			BitSet rows = new BitSet(rowsCount);
			for (int i = 0; i < rowsCount; i++) {
				rows.set(i);
			}
			buildTree(rootNode, rows);
		} catch (IOException e) {
			e.printStackTrace();
		}
		//System.out.println("Class count: " + this.classesCount);
		//System.out.println("Columns count: " + this.columnsCount);
		//System.out.println("Rows count: " + this.rowsCount);
	}


	/**
	 * 
	 * @param currentNode
	 * @param rows
	 * @throws IOException
	 * 
	 * Building the tree based on decision tree algorithm
	 * 1-Finding Best Attribute to split data 
	 * 2-if entropy value equal to zero mark this node as leaf
	 * 3-repeat until all data is separated or all attributes is processed
	 */
	private void buildTree(Node currentNode, BitSet rows) throws IOException {

		AttributeInfo bestAttribute = findBestSplit(psvFile, rows);
		Map<String, ValueInfo> infoValues;
		if (bestAttribute == null) {
			currentNode.setLeaf(true);
			System.out.println(rows.nextSetBit(0));
			System.out.println(classValues[rows.nextSetBit(0)-1]);
			if (classValues[rows.nextSetBit(0)])
				currentNode.setLabel("1");
			else
				currentNode.setLabel("0");
			//currentNode.setLabel(classValues[rows.nextSetBit(0)-1]?"1":"0");
		} else {
			currentNode.setLabel(bestAttribute.getName());
			currentNode.setIndex(bestAttribute.getIndex());

			/**
			 * Mark This Attribute as processed so not use it again
			 */
			//columns.set(bestAttribute.getIndex());

			infoValues = bestAttribute.getValues();
			Iterator<Entry<String, ValueInfo>> valuesItr = infoValues.entrySet()
					.iterator();
			while (valuesItr.hasNext()) {
				Entry<String, ValueInfo> entry = (Entry<String, ValueInfo>) valuesItr
						.next();

				ValueInfo currentValue = entry.getValue();
				String valueName = entry.getKey();
				Map<String, Integer> classes = currentValue.getAttributeClasses();

				if (currentValue.getEntropy() == 0.0) {
					String classLabel = findClassLabel(classes);
					Node leafNode = new Node();
					leafNode.setLabel(classLabel);
					leafNode.setLeaf(true);
					EqualCondition equalCondition = new EqualCondition(leafNode,
							valueName);
					currentNode.addCondition(equalCondition);

				} else {
					Node newNode = new Node();
					EqualCondition equalCondition = new EqualCondition(newNode,
							valueName);
					currentNode.addCondition(equalCondition);

					BitSet tempcolums = (BitSet) columns.clone();
					buildTree(newNode, currentValue.getRows());
					columns = tempcolums;
				}
			}
		}
	}

	/**
	 * 
	 * @param classes
	 * @return
	 * Assign the Class for current value
	 * By Get the class with maximum rows 
	 */
	private String findClassLabel(Map<String, Integer> classes) {

		int max = -1;
		String classLabel = "";
		Iterator<Entry<String, Integer>> classIterator = classes.entrySet()
				.iterator();

		while (classIterator.hasNext()) {
			Entry<String, Integer> classEntry = (Entry<String, Integer>) classIterator
					.next();
			if (classEntry.getValue() > max) {
				max = classEntry.getValue();
				classLabel = classEntry.getKey();
			}
		}

		return classLabel;
	}

	/**
	 * 
	 * @param csvFile
	 * @throws IOException
	 * Find Classes by process last column on the data
	 */
	private void findClasses(File csvFile) throws IOException {

		FileReader fileReader = new FileReader(csvFile);
		BufferedReader breader = new BufferedReader(fileReader);
		int counter = 0;		//counting the number of rows (including header)
		int classCounter = 0;	//counting the number of classes
		String line;
		classValues = new boolean[5120];	//stores the class value of each row (only works binary)
		while ((line = breader.readLine()) != null) {
			if (counter != 0) {
				String[] cols = line.split(",");
				//columnsCount = cols.length;
				if (cols.length > 2) {
					String targetValue = cols[cols.length - 1];
					if (targetValue.equals("0")){
						classValues[counter-1] = false;
					} else {
						classValues[counter-1] = true;
					}
					if (!classes.contains(targetValue)) {
						classes.add(targetValue);
						classCounter++;
					}
				}
			} else {
				String[] cols = line.split(",");
				columnsCount = cols.length;
			}
			counter++;
		}
		rowsCount = counter-1;
		classesCount = classCounter;
		breader.close();
		fileReader.close();
	}

	/**
	 *
	 * @param data
	 * @throws IOException
	 * Find Classes by process last column on the data
	 */
	private void findClasses(ArrayList<String> data) throws IOException {

		int counter = 0;		//counting the number of rows (including header)
		int classCounter = 0;	//counting the number of classes
		String line;
		classValues = new boolean[5120];	//stores the class value of each row (only works binary)
		while (counter < data.size()) {
			line = data.get(counter);
			if (counter != 0) {
				String[] cols = line.split(",");
				//columnsCount = cols.length;
				if (cols.length > 2) {
					String targetValue = cols[cols.length - 1];
					if (targetValue.equals("0")){
						classValues[counter-1] = false;
					} else {
						classValues[counter-1] = true;
					}
					if (!classes.contains(targetValue)) {
						classes.add(targetValue);
						classCounter++;
					}
				}
			} else {
				String[] cols = line.split(",");
				columnsCount = cols.length;
			}
			counter++;
		}
		rowsCount = counter-1;
		classesCount = classCounter;
	}

	/**
	 * 
	 * @param csvFile
	 * @param rows
	 * @return
	 * @throws IOException
	 * 
	 * Find Best Attribute to split
	 */
	private AttributeInfo findBestSplit(File csvFile, BitSet rows)
			throws IOException {

		AttributeInfo bestAttribute = null;
		double bestEntropy = Double.MAX_VALUE;
		for (int h = 0; h < columnsCount -1; h++) {
			if (columns.get(h))
				continue;

			AttributeInfo data = singleAttributeInfo(csvFile, h, rows);
			Map<String, ValueInfo> attributes = data.getValues();

			double entropy = calculateSubTreeEntropy(attributes,
					data.getRowCount());
			if (entropy < bestEntropy) {
				bestAttribute = data;
				bestEntropy = entropy;
				bestAttribute.setIndex(h);
			}

		}
		if (bestAttribute == null){
		} else {
			//don't use the same attribute twice in the same subtree
			columns.set(bestAttribute.getIndex());
		}
		return bestAttribute;

	}

	/**
	 * 
	 * @param file
	 * @param index
	 * @param rows
	 * @return
	 * @throws IOException
	 * Get Single Attribute Information 
	 */
	private AttributeInfo singleAttributeInfo(File file, int index, BitSet rows)
			throws IOException {

		AttributeInfo attributeInfo = new AttributeInfo();
		attributeInfo.setIndex(index);
		Map<String, ValueInfo> attributes = new HashMap<String, ValueInfo>();
		String line;
		int counter = 0;
		int linecounter = 0;

		if (this.psvFile != null) {
			FileReader fileReader = new FileReader(file);
			BufferedReader breader = new BufferedReader(fileReader);
			while ((line = breader.readLine()) != null) {
				if (linecounter == 0) {
					String[] cols = line.split(",");
					attributeInfo.setName(cols[index]);
					linecounter++;
				} else {
					if (!rows.get(linecounter-1))
					{
						linecounter++;
						continue;
					}

					String[] cols = line.split(",");

					String className = cols[cols.length - 1];
					String value = cols[index];
					if (!attributes.containsKey(value)) {
						attributes.put(value, new ValueInfo(classes,
								new BitSet(rowsCount)));
					}

					ValueInfo info = attributes.get(value);
					info.increaseClass(className);
					info.setRowAt(linecounter-1);
					info.increaseRowCount();
					linecounter++;
					counter++;
				}
			}
			breader.close();
			fileReader.close();
		} else {

			while (linecounter < data.size()) {
				line = this.data.get(linecounter);
				if (linecounter == 0) {
					String[] cols = line.split(",");
					attributeInfo.setName(cols[index]);
					linecounter++;
				} else {
					if (!rows.get(linecounter-1))
					{
						linecounter++;
						continue;
					}

					String[] cols = line.split(",");

					String className = cols[cols.length - 1];
					String value = cols[index];
					if (!attributes.containsKey(value)) {
						attributes.put(value, new ValueInfo(classes,
								new BitSet(rowsCount)));
					}

					ValueInfo info = attributes.get(value);
					info.increaseClass(className);
					info.setRowAt(linecounter-1);
					info.increaseRowCount();
					linecounter++;
					counter++;
				}
			}
		}

		attributeInfo.setRowCount(counter);
		attributeInfo.setValues(attributes);
		return attributeInfo;
	}

	/**
	 * 
	 * @param subTree
	 * @param count
	 * @return
	 * Calculate Subtree Entropy to find best split ( List of  Values)
	 */
	private double calculateSubTreeEntropy(Map<String, ValueInfo> subTree,
			int count) {
		double totalEntropy = 0;

		Iterator<Entry<String, ValueInfo>> iterator = subTree.entrySet()
				.iterator();
		while (iterator.hasNext()) {
			Entry<String, ValueInfo> entry = (Entry<String, ValueInfo>) iterator
					.next();
			ValueInfo info = entry.getValue();

			double entropy = calculateEntropy(new ArrayList<Integer>(info
					.getAttributeClasses().values()), info.getRowsCount());
			info.setEntropy(entropy);
			totalEntropy += ((double) info.getRowsCount() / count) * entropy;

		}

		return totalEntropy;
	}

	/**
	 * 
	 * @param classRecords
	 * @param total
	 * @return
	 * Calculate Entropy for single value
	 */
	public double calculateEntropy(List<Integer> classRecords, int total) {

		double entropy = 0;

		for (int i = 0; i < classRecords.size(); i++) {
			double probability = (double) classRecords.get(i) / total;

			entropy -= probability * logb(probability, 2);

		}
		return entropy;
	}

	/**
	 * 
	 * @param classRecords
	 * @param total
	 * @return
	 * Overload for above method
	 */
	public double calculateEntropy(Integer[] classRecords, int total) {

		double entropy = 0;
		// System.out.println("II TOTAL = "+total);
		for (int i = 0; i < classRecords.length; i++) {
			double probability = (double) classRecords[i] / total;

			entropy -= probability * logb(probability, 2);

		}
		return entropy;
	}

	/**
	 * 
	 * @param classRecords
	 * @param total
	 * @return
	 * Calculate Gini another measure of unpredictability of information content
	 */
	public double calculateGini(Integer[] classRecords, int total) {
		double gini = 0;

		for (int i = 0; i < classesCount; i++) {
			double probability = (double) classRecords[i] / total;
			gini += Math.pow(probability, 2);
		}
		gini = 1 - gini;
		return gini;
	}

	public static double logb(double a, double b) {
		if (a == 0)
			return 0;
		return Math.log(a) / Math.log(b);
	}

	public Node getRootNode() {
		return rootNode;
	}

}
