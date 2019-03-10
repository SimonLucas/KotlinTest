package tracks.singlePlayer.simple.doNothing;

import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.LearnedFM;

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
			int[][] from = LearnedFM.observationToIntGrid(lastObservation.getObservationGrid());
			int[][] to = LearnedFM.observationToIntGrid(stateObs.getObservationGrid());
			fm.addData(from, to);

//			System.out.println("Patterns learned: " + fm.getProgress());
//			System.out.println(lastFM.checkAccuracy(from, to) + " " + fm.getProgress());
		}

		lastObservation = stateObs.copy();
//		lastFM = fm.copy();

		return Types.ACTIONS.ACTION_NIL;
	}
}
