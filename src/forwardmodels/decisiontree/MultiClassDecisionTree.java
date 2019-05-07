package forwardmodels.decisiontree;
import games.sokoban.Grid;
import jdk.jshell.spi.ExecutionControl;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.util.ArrayList;
import java.util.List;

public class MultiClassDecisionTree {

    private ArrayList<Attribute> attributes;
    private Instances trainingData;
    private J48 tree;
    private String defaultvalue = "x";
    private int nrOfLearnedInstances = 0;
    private boolean retrain = false;

    public int getTimesTrained() {
        return timesTrained;
    }

    private int timesTrained = 0;

    public MultiClassDecisionTree(String positions){
        System.out.println("create tree");
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

        List<String> cell_values = new ArrayList<String>(9);
        cell_values.add(".");
        cell_values.add("*");
        cell_values.add("o");
        cell_values.add("A");
        cell_values.add("w");
        cell_values.add("+");
        cell_values.add("u");   // represents A and o on the same position
        cell_values.add("x");   // add symbol end of the grid

        List<String> action_values = new ArrayList();
        action_values.add("0");
        action_values.add("1");
        action_values.add("2");
        action_values.add("3");
        action_values.add("4");

        attributes = new ArrayList<Attribute>();
        for (String s : positions.split(";")){
            attributes.add(new Attribute(s, cell_values));
        }
        attributes.add(new Attribute("action", action_values));
        attributes.add(new Attribute("outcome", cell_values));

        trainingData = new Instances("TrainingData", attributes , 0);
        trainingData.setClassIndex(trainingData.numAttributes() - 1); // Assuming outcome is on the last index
    }

    public void addDataPoint(ArrayList<Character> gridentries, int action, char outcome){
        DenseInstance newInstance = getInstance(gridentries, action, outcome);
        if (!this.retrain){
            String true_value = newInstance.stringValue(newInstance.classIndex());
            DenseInstance copyInstance = new DenseInstance(newInstance);
            copyInstance.setDataset(trainingData);
            String result = this.predictInstance(copyInstance);
            if (!true_value.equals(result))
                retrain = true;
        }
        trainingData.add(newInstance);
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

    /*
    public String predict(Grid grid){
        return "";
    }*/

    /*
    public String predict(ArrayList<String[]> datapoints){
        StringBuilder sb = new StringBuilder();

        for (String[] datapoint : datapoints){
            try{
                //sb.append(tree.classifyInstance(getInstance(datapoint)));
            } catch (Exception e){
                System.out.println("Failed to classify instance: " + Arrays.toString(datapoint));
                e.printStackTrace();
            }
        }

        return sb.toString();
    }*/

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
}
