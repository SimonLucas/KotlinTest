package tracks.singlePlayer.simple.doNothing;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.fm.LearnedFM;

import java.util.ArrayList;
import java.util.HashMap;

public class Agent extends AbstractPlayer{

	public LearnedFM fm, lastFM;
	private StateObservation lastObservation;
	public static int learningCapacity;

	/**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		fm = LearnedFM.getInstance(learningCapacity);
	}
	
	/**
	 * return ACTION_NIL on every call to simulate doNothing player
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return 	ACTION_NIL all the time
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if (lastObservation != null) {
			int[][] from = LearnedFM.observationToIntGrid(lastObservation);
			int[][] to = LearnedFM.observationToIntGrid(stateObs);
			fm.addData(from, to);

//			System.out.println("Patterns learned: " + fm.getProgress());
//			System.out.println(lastFM.checkAccuracy(from, to) + " " + fm.getProgress());
		}

		lastObservation = stateObs.copy();
//		lastFM = fm.copy();

//		ArrayList<Observation>[][] grid = stateObs.getObservationGrid();
//		HashMap<Integer, Integer> count = new HashMap<>();
//		for (int i = 0; i < grid.length; i++)
//			for (int j = 0; j < grid[0].length; j++) {
//				for (Observation o : grid[i][j]) {
//					count.merge(o.itype, 1, (a, b) -> a + b);
//				}
//			}
//		System.out.println(count);

		return Types.ACTIONS.ACTION_NIL;
	}
}
