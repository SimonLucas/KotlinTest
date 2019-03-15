package games.simplegridgame.hyper;

import evodef.AnnotatedSearchSpace;
import ggi.SimplePlayerInterface;

public abstract class AgentFactorySpace {

	private AnnotatedSearchSpace ass;
	private String agentType;

	public String getAgentType() {
		return agentType;
	}

	protected AgentFactorySpace setAgentType(String agentType){
		this.agentType = agentType;
		return this;
	}

	public AgentFactorySpace setSearchSpace(AnnotatedSearchSpace ass){
		this.ass = ass;
		return this;
	}

	public AnnotatedSearchSpace getSearchSpace(){
		return ass;
	}

	public abstract SimplePlayerInterface agent(int[] solution);

	@Override
	public String toString(){
		return "[AgentFactory: "+agentType+"]\n"+ass.toString();
	}


}
