package forwardmodels.decisiontree.conditions;

import forwardmodels.decisiontree.Node;

public class ElseCondition extends Condition {

	public ElseCondition(Node nextNode) {
		super(nextNode);
		
	}

	@Override
	public boolean test(String value) {
		return true;
	}

	@Override
	public String toString() {
		String out = "\n -- Else Condition ----";
		if(this.getNextNode()!=null){
		out+= "\n Next ->> "+this.getNextNode().toString();
		}
		return out;
	}
	
}
