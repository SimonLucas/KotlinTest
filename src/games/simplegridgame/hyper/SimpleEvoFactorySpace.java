package games.simplegridgame.hyper;

import agents.SimpleEvoAgent;
import ggi.SimplePlayerInterface;
import ntbea.params.Param;

public class SimpleEvoFactorySpace extends AgentFactorySpace{

	@Override
	public SimplePlayerInterface agent(int[] solution){
		Param[] params = getSearchSpace().getParams();
		SimpleEvoAgent testAgent = new SimpleEvoAgent();

		testAgent.setFlipAtLeastOneValue  ((boolean)params[0].getValue(solution[0]));
		testAgent.setProbMutation         ((double)params[1].getValue(solution[1]));
		testAgent.setSequenceLength       ((int)params[2].getValue(solution[2]));
		testAgent.setNEvals               ((int)params[3].getValue(solution[3]));
		testAgent.setUseShiftBuffer       ((boolean)params[4].getValue(solution[4]));
		testAgent.setUseMutationTransducer((boolean)params[5].getValue(solution[5]));
		testAgent.setRepeatProb           ((double)params[6].getValue(solution[6]));
		testAgent.setDiscountFactor       ((double)params[7].getValue(solution[7]));
		return testAgent;
	}

}
