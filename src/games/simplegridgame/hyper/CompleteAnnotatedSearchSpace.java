package games.simplegridgame.hyper;

import evodef.AnnotatedSearchSpace;
import ntbea.params.Param;

import java.util.Arrays;

public class CompleteAnnotatedSearchSpace implements AnnotatedSearchSpace {

	protected Param[] params;
	protected int[] dimensions;

	@Override
	public Param[] getParams(){
		return params;
	}

	@Override
	public int nDims() {
		return dimensions.length;
	}

	@Override
	public int nValues(int i) {
		return dimensions[i];
	}

	@Override
	public String toString() {
		return paramsToString()+"\n"+ Arrays.toString(dimensions);
	}

	private String paramsToString(){
		String[] names = new String[params.length];
		for(int i=0; i<names.length; i++)
			names[i] = getParams()[i].getName();
		return Arrays.toString(names).replaceAll("\\[","").replaceAll("\\]","");
	}
}
