package tracks.singlePlayer;

import tools.fm.LearnedFM;
import tools.StatSummary;
import tools.Utils;
import tracks.ArcadeMachine;

import java.util.Random;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 04/10/13 Time: 16:29 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class TestLFM {

    public static void main(String[] args) {

		boolean visuals = true;
		int gameIdx = 65;  // game idx in file examples/all_games_sp.csv
		int levelIdx = 0; // level names from 0 to 4 (game_lvlN.txt).

		// Available controllers:
//		String sampleRandomController = "tracks.singlePlayer.simple.sampleRandom.Agent";
		String sampleRandomController = "tracks.singlePlayer.simple.simpleRandom.Agent";
		String doNothingController = "tracks.singlePlayer.simple.doNothing.Agent";
		String sampleOneStepController = "tracks.singlePlayer.simple.sampleonesteplookahead.Agent";
		String sampleFlatMCTSController = "tracks.singlePlayer.simple.greedyTreeSearch.Agent";
		String sampleMCTSController = "tracks.singlePlayer.advanced.sampleMCTS.Agent";
		String sampleRHEAController = "tracks.singlePlayer.advanced.sampleRHEA.Agent";
        String sampleRSController = "tracks.singlePlayer.advanced.sampleRS.Agent";
		String sampleOLETSController = "tracks.singlePlayer.advanced.olets.Agent";

		//Load available games
		String spGamesCollection =  "examples/all_games_sp.csv";
		String[][] games = Utils.readGames(spGamesCollection);

		//Game settings
		int seed = new Random().nextInt();

		// Game and level to play
		String gameName = games[gameIdx][1];
		String game = games[gameIdx][0];
		String level1 = game.replace(gameName, gameName + "_lvl" + levelIdx);

		// 1. This starts a game, in a level, played by a human.
//		ArcadeMachine.playOneGame(game, level1, recordActionsFile, seed);

		// 2. This plays a game in a level by the controller.
		double maxPatterns = Math.pow(10, 9);
//		double maxPatterns = 2000;
		int patternPerTrial = 25000;
		int trials = (int)(maxPatterns/patternPerTrial);
		int noReps = 20;
		visuals = false;

		System.out.println("Running " + trials + " trials, with " + noReps + " repetitions each.");
		StatSummary[] win = new StatSummary[trials];
		StatSummary[] score = new StatSummary[trials];
		StatSummary[] ticks = new StatSummary[trials];

		tracks.singlePlayer.advanced.sampleRHEA.Agent.learning = false;
		tracks.singlePlayer.advanced.sampleRHEA.Agent.usingRealFM = false;
		for (int i = trials-1; i < trials; i++) {
			win[i] = new StatSummary();
			score[i] = new StatSummary();
			ticks[i] = new StatSummary();
			int capacity = i * patternPerTrial;
			if (i == trials - 1)  {
				capacity = -1;
			}
			System.out.println((i+1) + "/" + trials + ": " + capacity);

			tracks.singlePlayer.advanced.sampleRHEA.Agent.learningCapacity = capacity;
			tracks.singlePlayer.simple.simpleRandom.Agent.learningCapacity = capacity;
			LearnedFM fm = LearnedFM.getInstance(capacity);

			// Train model up to capacity
			System.out.println("Training");

			int oldCapacity = i > 0 ? (i-1) * patternPerTrial : 0;
			double patterns = fm.train(oldCapacity, capacity);
			if (i == 0) trials = (int)(patterns/patternPerTrial);

//			int cap = fm.getProgress();
//			int k = 0;
//			int maxTries = 50;
//			while (cap < capacity && k < maxTries) {
//				ArcadeMachine.runOneGame(game, level1, visuals, sampleRandomController, null, seed, 0);
//				cap = fm.getProgress();
//				k++;
//			}

			System.out.println("Done training " + fm.getProgress());

			// Play game with trained model
			System.out.println("Testing");
//			tracks.singlePlayer.advanced.sampleRHEA.Agent.learning = false;
			for (int j = 0; j < noReps; j++) {
				double[] result = ArcadeMachine.runOneGame(game, level1, visuals, sampleRHEAController, null, seed, 0);
				win[i].add(result[0]);
				score[i].add(result[1]);
				ticks[i].add(result[2]);
//				System.out.println(result[0] + " " + result[1] + " " + result[2]);
//				System.out.println(fm.getProgress());
//				System.out.println("new");
			}
			System.out.println("ave = " + win[i].mean());
			System.out.println("se = " + win[i].stdErr());
			System.out.println("ave = " + score[i].mean());
			System.out.println("se = " + score[i].stdErr());
			System.out.println("ave = " + ticks[i].mean());
			System.out.println("se = " + ticks[i].stdErr());

			if (i != trials - 1)
				i = fm.getProgress() / patternPerTrial;

//			System.out.println("\n" + fm.getTransitions());
		}

		System.out.println("Finished.");

    }
}
