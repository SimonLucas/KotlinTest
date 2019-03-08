package test;
import games.simplegridgame.Experiment;

public class ExperimentCaller {
    public static void main(String[] args){
        int learnSteps = Integer.parseInt(System.getProperty("learnSteps", "3"));
        int testSteps = Integer.parseInt(System.getProperty("testSteps", "100"));
        int gamesPerEval = Integer.parseInt(System.getProperty("gamesPerEval", "1"));
        int nPredictionTests = Integer.parseInt(System.getProperty("nPredictionTests", "30"));
        int w = Integer.parseInt(System.getProperty("w", "30"));
        int h = Integer.parseInt(System.getProperty("h", "30"));
        boolean visual = Boolean.parseBoolean(System.getProperty("visual", "false"));
        int lutSizeLimit =   Integer.parseInt(System.getProperty("lutSizeLimit", "0"));
        boolean diceRoll = Boolean.parseBoolean(System.getProperty("diceRoll", "false"));
        int nReps =   Integer.parseInt(System.getProperty("nReps", "5"));
        int startLut =   Integer.parseInt(System.getProperty("startLut", "512"));
        int endLut =   Integer.parseInt(System.getProperty("endLut", "512"));
        int stepLut =   Integer.parseInt(System.getProperty("stepLut", "32"));

        String outFileName="results_learnSteps_"+ learnSteps + "_testSteps_"+testSteps+"_gamesPerEval_"+gamesPerEval+"_nPredictionTests_"+
                nPredictionTests+"_w_"+w+"_h_"+h+"_visual_"+visual+"_lutSizeLimit_"+lutSizeLimit+"_diceRoll_"+diceRoll+"_nReps_"+nReps+"_startLut_"+
                startLut+"_endLut_"+endLut+"_stepLut"+stepLut+".txt";

        Experiment exp = new Experiment(learnSteps, testSteps, gamesPerEval, nPredictionTests, w, h, visual, lutSizeLimit, diceRoll, nReps, startLut, endLut, stepLut, outFileName);
        exp.run();

    }
}
