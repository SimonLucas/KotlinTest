package forwardmodels.decisiontree;
import games.sokoban.*;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MultiClassDecisionTree {

    private ArrayList<Attribute> attributes;
    private HashSet<Example> instances = new HashSet<>();

    private Instances trainingData;
    private J48 tree;
    private String defaultvalue = "x";
    private int nrOfLearnedInstances = 0;
    private boolean retrain = false;
    public GridIterator gridIterator;

    public int getTimesTrained() {
        return timesTrained;
    }

    private int timesTrained = 0;

    public String getTreeInfoString(){
        return "leaves: " + tree.measureNumLeaves() + "; nodes: " + tree.measureTreeSize();
    }

    public MultiClassDecisionTree(GridIterator gridIterator){
        String[] options = new String[4];
        options[0] = "-M";
        options[1] = "1";
        options[2] = "-O";
        options[3] = "-U";            // unpruned tree

        tree = new J48();         // new instance of tree
        try {
            tree.setOptions(options);
        } catch (Exception e){
            e.printStackTrace();
        }

        this.gridIterator = gridIterator;

        List<String> cell_values = new ArrayList<String>(9);
        cell_values.add(".");
        cell_values.add("*");
        cell_values.add("o");
        cell_values.add("A");
        cell_values.add("w");
        cell_values.add("+");
        cell_values.add("u");   // represents A and o on the same position
        cell_values.add("x");   // add symbol end of the grid

        List<String> action_values = new ArrayList<String>(5);
        action_values.add("0");
        action_values.add("1");
        action_values.add("2");
        action_values.add("3");
        action_values.add("4");

        attributes = new ArrayList<Attribute>(gridIterator.getMaxElements()+2);
        for (String s : gridIterator.getHeader()){
            attributes.add(new Attribute(s, cell_values));
        }
        attributes.add(new Attribute("action", action_values));
        attributes.add(new Attribute("outcome", cell_values));

        trainingData = new Instances("TrainingData", attributes , 0);
        trainingData.setClassIndex(trainingData.numAttributes() - 1); // Assuming outcome is on the last index
    }

    public void addDataPoint(ArrayList<Character> gridentries, int action, char outcome){
        addDataPoint(getInstance(gridentries, action, outcome));

    }

    private void addDataPoint(DenseInstance denseInstance){
        if (!this.retrain){
            String true_value = denseInstance.stringValue(denseInstance.classIndex());
            DenseInstance copyInstance = new DenseInstance(denseInstance);
            copyInstance.setDataset(trainingData);
            String result = this.predictInstance(copyInstance);
            if (!true_value.equals(result))
                retrain = true;
        }
        trainingData.add(denseInstance);    //todo add instances only if it was wrongly predicted or with random chance
    }

    public void addGrid(GridInterface grid1, GridInterface grid2, int action){
        gridIterator.setGrid(grid1);
        for (int x = 0; x < grid1.getWidth(); x++) {
            for (int y = 0; y < grid1.getHeight(); y++) {
                Example e = getExampleCharacters(x, y, action);
                if (!instances.contains(e)){
                    DenseInstance instance = getInstance(e.getIp(), e.getAction());
                    //getInstance(grid1, action, x, y);
                    instance.setClassValue(String.valueOf(grid2.getCell(x,y)));
                    addDataPoint(instance);
                    instances.add(e);
                }
            }
        }
        //System.out.println("number of instances: " + instances.size());
    }

    private Example getExampleCharacters(int x, int y, int action){
        ArrayList<Character> ip = new ArrayList<>(gridIterator.getMaxElements()-2);
        for (gridIterator.setCell(x,y); gridIterator.hasNext();){
            ip.add(gridIterator.next());
        }
        return new Example(ip,action);
    }

    public void updateTree(){
        //only retrain when new instances were included that could not have been predicted beforehand
        if (this.nrOfLearnedInstances < trainingData.numInstances()){
            if (retrain) {
                try {
                    tree.buildClassifier(trainingData);
                    //System.out.println("retrain tree on " + trainingData.numInstances() + " instances");
                    defaultvalue = "";
                    timesTrained++;
                } catch (Exception e) {
                    //should only happen when the game contains only one element
                    e.printStackTrace();

                    System.out.println("Training DT failed; use default value: " + defaultvalue);
                    Instance instance = trainingData.get(0);
                    defaultvalue = instance.stringValue(0);
                }
                nrOfLearnedInstances = trainingData.numInstances();
                retrain = false;
            }
        }
    }

    private String predictInstance (DenseInstance denseInstance){
        if (defaultvalue.equals("")){
            try {
                denseInstance.setValue(denseInstance.classIndex(), tree.classifyInstance(denseInstance));
                return denseInstance.stringValue(denseInstance.classIndex());
            } catch (Exception e){
                e.printStackTrace();
                return denseInstance.stringValue(0);
            }
        } else {
            if  (defaultvalue.equals("x")){
                //return what was on the same field
                return denseInstance.stringValue(0);
            } else{
                System.out.println("This should never happen");
                return defaultvalue;
            }
        }
    }


    public SimpleGrid predict(GridInterface grid, int action){
        SimpleGrid predictedGrid = new SimpleGrid(grid.getWidth(), grid.getHeight());
        gridIterator.setGrid(grid);
        for (int x = 0; x < grid.getWidth(); x++)
            for (int y = 0; y < grid.getHeight(); y++)
            {
                DenseInstance instance = getInstance(grid, action, x, y);
                //System.out.println("x: " + x + "; y: " + y + "; instance: " + instance);
                predictedGrid.setCell(x, y, predictInstance(instance).charAt(0));
            }

        return predictedGrid;
    }

    public String predictCell(ArrayList<Character> gridentries, int action){
        DenseInstance denseInstance = getInstance(gridentries, action);
        return this.predictInstance(denseInstance);
    }

    private DenseInstance getInstance(ArrayList<Character> gridentries, int action){
        DenseInstance instance = new DenseInstance(attributes.size());
        instance.setDataset(trainingData);
        for (int i = 0; i < gridentries.size(); i++){
            instance.setValue(i, gridentries.get(i).toString());
        }
        instance.setValue(instance.numAttributes()-2, String.valueOf(action));

        return instance;
    }

    private DenseInstance getInstance(ArrayList<Character> gridentries, int action, char result){
        DenseInstance instance = getInstance(gridentries, action);
        instance.setValue(instance.numAttributes()-1, Character.toString(result));

        return instance;
    }

    private DenseInstance getInstance(GridInterface grid, int action, int x, int y){
        DenseInstance instance = new DenseInstance(attributes.size());
        instance.setDataset(trainingData);

        int i = 0;
        try {
            for (gridIterator.setCell(x, y); gridIterator.hasNext(); ) {
                instance.setValue(i, gridIterator.next().toString());
                i++;
            }
            instance.setValue(i, String.valueOf(action));
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return instance;
    }


    public static void main(String[] args) {
        GridIterator iterator = new CrossGridIterator(2);

        MultiClassDecisionTree mdt = new MultiClassDecisionTree(iterator);
        for (Attribute att : mdt.attributes) {
            //System.out.println(att.toString());
        }

        System.out.println("Original Grid");
        Grid g = new Sokoban().getBoard();
        g.print();
        System.out.println();

        System.out.println("Predicted Grid");
        SimpleGrid predictedGrid = mdt.predict(g, 0);
        predictedGrid.print();
    }
}
