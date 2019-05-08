package test;
import games.simplegridgame.Experiment;

public class ExperimentCaller {
    public static void main(String[] args){
        int updateRule = Integer.parseInt(System.getProperty("updateRule", "0"));
        int agent = Integer.parseInt(System.getProperty("agent", "0"));
        boolean trueModel = Boolean.parseBoolean(System.getProperty("trueModel", "false"));

        int testSteps = Integer.parseInt(System.getProperty("testSteps", "100"));
        int gamesPerEval = Integer.parseInt(System.getProperty("gamesPerEval", "1"));
        int nReps =   Integer.parseInt(System.getProperty("nReps", "3"));
        int lutSize =   Integer.parseInt(System.getProperty("lutSize", "512"));

        int w = Integer.parseInt(System.getProperty("w", "30"));
        int h = Integer.parseInt(System.getProperty("h", "30"));
        boolean visual = Boolean.parseBoolean(System.getProperty("visual", "false"));
        boolean diceRoll = Boolean.parseBoolean(System.getProperty("diceRoll", "false"));


        String outFileName="out/results_updateRule_"+updateRule+ "_agent_"+agent+"_trueModel_"+trueModel + "_testSteps_"+testSteps+"_gamesPerEval_"+gamesPerEval+
                "_w_"+w+"_h_"+h+"_visual_"+visual+"_diceRoll_"+diceRoll+"_nReps_"+nReps+"_lutSize_"+
                lutSize + ".txt";

        Experiment exp = new Experiment(updateRule, agent, trueModel, testSteps, gamesPerEval,  w, h, visual,
                diceRoll, nReps, lutSize, outFileName);
        exp.run();

    }
}
