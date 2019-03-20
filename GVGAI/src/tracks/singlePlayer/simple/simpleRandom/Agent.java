package tracks.singlePlayer.simple.simpleRandom;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.fm.LearnedFM;

/**
 * Created with IntelliJ IDEA.
 * User: ssamot
 * Date: 14/11/13
 * Time: 21:45
 * This is a Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class Agent extends AbstractPlayer {
    /**
     * Random generator for the agent.
     */
    protected Random randomGenerator;
    /**
     * List of available actions for the agent
     */
    protected ArrayList<Types.ACTIONS> actions;

    public LearnedFM fm, lastFM;
    private StateObservation lastObservation;
    public static int learningCapacity;

    /**
     * Public constructor with state observation and time due.
     * @param so state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation so, ElapsedCpuTimer elapsedTimer)
    {
        fm = LearnedFM.getInstance(learningCapacity);
//        fm.reset();
        randomGenerator = new Random();
        actions = so.getAvailableActions();
    }


    /**
     * Picks an action. This function is called every game step to request an
     * action from the player.
     * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
     * @return An action for the current state
     */
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if (lastObservation != null) {
            int[][] from = LearnedFM.observationToIntGrid(lastObservation);
            int[][] to = LearnedFM.observationToIntGrid(stateObs);
            fm.addData(from, to);

//			System.out.println("Patterns learned: " + fm.getProgress());
//			System.out.println(lastFM.checkAccuracy(from, to) + " " + fm.getProgress());
        }

        lastObservation = stateObs.copy();
//		lastFM = fm.copy();
//        System.out.println(fm.getProgress());


//        ArrayList<Observation>[][] grid = stateObs.getObservationGrid();
//        HashMap<Integer, Integer> count = new HashMap<>();
//        for (ArrayList<Observation>[] arrayLists : grid)
//            for (int j = 0; j < grid[0].length; j++) {
//                for (Observation o : arrayLists[j]) {
//                    count.merge(o.itype, 1, (a, b) -> a + b);
//                }
//            }
//        System.out.println(count);

        int index = randomGenerator.nextInt(actions.size());
        return actions.get(index);
    }

}
