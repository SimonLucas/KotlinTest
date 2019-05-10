package games.simplegridgame.hyper;

import ntbea.params.BooleanParam;
import ntbea.params.DoubleParam;
import ntbea.params.IntegerParam;
import ntbea.params.Param;

public class SimpleEvoParams extends CompleteAnnotatedSearchSpace{


	public SimpleEvoParams(){
		boolean[] flipAtLeastOneValue=	new boolean[]{false,true};
		double[]  probMutation= 		new double[]{0.1,0.2,0.3,0.4,0.5,0.7};
		int[]     sequenceLength= 		new int[]{5,10,20,40,50};
		int[]     nEvals= 				new int[]{10,20,30,40,50};
		boolean[] useShiftBuffer= 		new boolean[]{false,true};
		boolean[] useMutationTransducer=new boolean[]{false,true};
		double[]  repeatProb= 			new double[]{0.3,0.4,0.5,0.7};
		double[]  discountFactor= 		new double[]{0.999,0.99,0.9,0.8};

		params = createParams(flipAtLeastOneValue, probMutation, sequenceLength, nEvals, useShiftBuffer, useMutationTransducer, repeatProb, discountFactor);
		dimensions = createDimensions(flipAtLeastOneValue, probMutation, sequenceLength, nEvals, useShiftBuffer, useMutationTransducer, repeatProb, discountFactor);
	}


	private Param[] createParams(boolean[] a, double[] b, int[] c, int[] d, boolean[] e, boolean[] f, double[] g, double[] h){
		return new Param[]{
				new BooleanParam().setArray(a).setName("flipAtLeastOneValue"),
				new  DoubleParam().setArray(b).setName("probMutation"),
				new IntegerParam().setArray(c).setName("sequenceLength"),
				new IntegerParam().setArray(d).setName("nEvals"),
				new BooleanParam().setArray(e).setName("useShiftBuffer"),
				new BooleanParam().setArray(f).setName("useMutationTransducer"),
				new  DoubleParam().setArray(g).setName("repeatProb"),
				new  DoubleParam().setArray(h).setName("discountFactor")
		};
	}

	private int[] createDimensions(boolean[] a, double[] b, int[] c, int[] d, boolean[] e, boolean[] f, double[] g, double[] h){
		return new int[]{
				a.length,
				b.length,
				c.length,
				d.length,
				e.length,
				f.length,
				g.length,
				h.length
		};
	}

}
