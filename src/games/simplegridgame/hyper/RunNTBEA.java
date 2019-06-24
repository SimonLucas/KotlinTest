package games.simplegridgame.hyper;

import evodef.NoisySolutionEvaluator;
import evodef.SearchSpace;
import games.sokoban.SokobanGameEnv;
import ntbea.NTupleBanditEA;
import ntbea.NTupleSystem;
import ntbea.NTupleSystemReport;
import ntbea.params.Param;
import utilities.ElapsedTimer;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;

public class RunNTBEA {
	public static void main(String[] args) {
		String fileOut;
		double kExplore = 300;
		double epsilon = 0.5;
		int	   nEvals = 100;
		//int    lutSize = 512;

		if(args.length==5){
			fileOut = args[0];
			kExplore = Double.parseDouble(args[1]);
			epsilon =  Double.parseDouble(args[2]);
			nEvals =   Integer.parseInt(  args[3]);
			//lutSize =  Integer.parseInt  (args[4]);
		}else{
			fileOut = "result.txt";
		}

		AgentFactorySpace agentFactory = new SimpleEvoFactorySpace().
				setSearchSpace(new SimpleEvoParams());

//		SimpleGridGameEnv env = new SimpleGridGameEnv(agentFactory);
//		env.setLutSize(lutSize);
//		env.setTrueFitnessSamples(100);
//		int patterns = env.train();

		SokobanGameEnv env = new SokobanGameEnv(agentFactory);

		NTupleBanditEA banditEA = new NTupleBanditEA().setKExplore(kExplore).setEpsilon(epsilon);

		NTupleSystem model = new NTupleSystem();

		model.use1Tuple = true;
		model.use2Tuple = true;
		model.use3Tuple = false;
		model.useNTuple = true;
		banditEA.setModel(model);

		System.out.println("NTBEA k: "+kExplore);
		System.out.println("NTBEA epsilon: "+epsilon);
		System.out.println("use1Tuple: "+model.use1Tuple);
		System.out.println("use2Tuple: "+model.use2Tuple);
		System.out.println("use3Tuple: "+model.use3Tuple);
		System.out.println("useNTuple: "+model.useNTuple);

		ElapsedTimer timer = new ElapsedTimer();
		System.out.println("SS size: "+searchSpaceSize(env.searchSpace()));
		System.out.println("Evaluations: "+nEvals);
		int[] solution = banditEA.runTrial(env, nEvals);
		Param[] params = agentFactory.getSearchSpace().getParams();

		System.out.println("Report: ");
		new NTupleSystemReport().setModel(model).printDetailedReport();
		new NTupleSystemReport().setModel(model).printSummaryReport();

		System.out.println("Model created: ");
		System.out.println(model);
		System.out.println("Model used: ");
		System.out.println(banditEA.getModel());

		System.out.println();
		System.out.println("Solution returned: " + Arrays.toString(solution));
		printExplicitSolution(solution, params);
		System.out.println();
		System.out.println(timer);
		double trueFitness = env.trueFitness(solution);
		System.out.println("Solution fitness:  " + trueFitness);
		System.out.println("k Explore: " + banditEA.kExplore);
		System.out.println(timer+"\n");

		try(FileWriter w = new FileWriter(fileOut)){
			String solutionArray = Arrays.toString(solution);
			solutionArray = solutionArray.replaceAll("\\[","").replaceAll("\\]","").replaceAll(" ","");
			String solutionNames="";
			for(Param p :agentFactory.getSearchSpace().getParams()){
				solutionNames = solutionNames+p.getName()+",";
			}
			solutionNames = solutionNames.substring(0,solutionNames.length()-1);
			w.append(solutionNames+",truefitness"+",truefitnessError"+"\n");
			w.append(solutionArray+","+trueFitness+","+env.getLastTrueFitnessError());
			w.flush();
		}catch (IOException e){
			e.printStackTrace();
		}

	}

	private static long searchSpaceSize(SearchSpace s){
		long size=1;
		for(int i=0; i<s.nDims(); i++){
			size*=s.nValues(i);
		}
		return size;
	}

	private static void printExplicitSolution(int[] solution, Param[] params){
		for(int i=0; i<params.length; i++){
			System.out.println(params[i].getName()+": "+params[i].getValue(solution[i]));
		}
	}

}
