package tracks.singlePlayer.advanced.sampleRHEA;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import tools.ElapsedCpuTimer;
import tools.fm.LearnedFM;
import tracks.singlePlayer.tools.Heuristics.StateHeuristic;
import tracks.singlePlayer.tools.Heuristics.WinScoreHeuristic;

import java.util.*;

@SuppressWarnings("ALL")
public class Agent extends AbstractPlayer {

    // Parameters
    private int POPULATION_SIZE = 10;
    private int SIMULATION_DEPTH = 10;
    private int CROSSOVER_TYPE = UNIFORM_CROSS;
    private int MUTATION = 1;
    private int TOURNAMENT_SIZE = 2;
    private int ELITISM = 1;

    // Constants
    private final long BREAK_MS = 10;
    public static final double epsilon = 1e-6;
    static final int POINT1_CROSS = 0;
    static final int UNIFORM_CROSS = 1;

    // Alg fields
    private Individual[] population, nextPop;
    private int N_ACTIONS;
    private HashMap<Integer, Types.ACTIONS> action_mapping;
    private StateHeuristic heuristic;
    private Random randomGenerator;

    // Budget vars
//    private ElapsedCpuTimer timer;
//    private double acumTimeTakenEval = 0,avgTimeTakenEval = 0, avgTimeTaken = 0, acumTimeTaken = 0;
//    private long remaining;
//    private int numEvals = 0, numIters = 0;
    private boolean keepIterating = true;
    private int NUM_INDIVIDUALS = 30;

    // FM learning vars
    private LearnedFM fm, lastFM;
    private StateObservation lastObservation;
    public static int learningCapacity;
    public static boolean learning;
    public static boolean usingRealFM = true;

    /**
     * Public constructor with state observation and time due.
     *
     * @param stateObs     state observation of the current game.
     * @param elapsedTimer Timer for the controller creation.
     */
    public Agent(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        randomGenerator = new Random();
        heuristic = new WinScoreHeuristic(stateObs);
//        this.timer = elapsedTimer;
        fm = LearnedFM.getInstance(learningCapacity);
    }

    @Override
    public Types.ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
        if (learning & lastObservation != null) {
            int[][] from = LearnedFM.observationToIntGrid(lastObservation);
            int[][] to = LearnedFM.observationToIntGrid(stateObs);
            fm.addData(from, to);

//			System.out.println("Patterns learned: " + fm.getProgress());
//            System.out.println(lastFM.checkAccuracy(from, to) + " " + fm.getProgress());
        }

//        this.timer = elapsedTimer;
//        avgTimeTaken = 0;
//        acumTimeTaken = 0;
//        numEvals = 0;
//        acumTimeTakenEval = 0;
//        numIters = 0;
//        remaining = timer.remainingTimeMillis();
//        keepIterating = true;
        NUM_INDIVIDUALS = 30;

        // INITIALISE POPULATION
        init_pop(stateObs);

        // RUN EVOLUTION
//        remaining = timer.remainingTimeMillis();
//        while (remaining > avgTimeTaken && remaining > BREAK_MS && keepIterating) {
        while (NUM_INDIVIDUALS > 0) {
            runIteration(stateObs);
//            remaining = timer.remainingTimeMillis();
        }

        // RETURN ACTION
        Types.ACTIONS best = get_best_action(population);
        if (learning) {
            lastObservation = stateObs.copy();
//            lastFM = fm.copy();
        }
        return best;
    }

    /**
     * Run evolutionary process for one generation
     * @param stateObs - current game state
     */
    private void runIteration(StateObservation stateObs) {
        ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

        if (POPULATION_SIZE > 1) {
            for (int i = ELITISM; i < POPULATION_SIZE; i++) {
//                if (remaining > 2*avgTimeTakenEval && remaining > BREAK_MS) { // if enough time to evaluate one more individual
                if (NUM_INDIVIDUALS > 0) {
                    Individual newind;

                    newind = crossover();
                    newind = newind.mutate(MUTATION);

                    // evaluate new individual, insert into population
                    add_individual(newind, nextPop, i, stateObs);

//                    remaining = timer.remainingTimeMillis();
                } else {keepIterating = false; break;}
            }
            Arrays.sort(nextPop, (o1, o2) -> {
                if (o1 == null && o2 == null) {
                    return 0;
                }
                if (o1 == null) {
                    return 1;
                }
                if (o2 == null) {
                    return -1;
                }
                return o1.compareTo(o2);
            });
        } else if (POPULATION_SIZE == 1){
            Individual newind = new Individual(SIMULATION_DEPTH, N_ACTIONS, randomGenerator).mutate(MUTATION);
            evaluate(newind, heuristic, stateObs);
            if (newind.value > population[0].value)
                nextPop[0] = newind;
        }

        population = new Individual[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
            population[i] = nextPop[i].copy();
        }

//        numIters++;
//        acumTimeTaken += (elapsedTimerIteration.elapsedMillis());
//        avgTimeTaken = acumTimeTaken / numIters;
    }

    /**
     * Evaluates an individual by rolling the current state with the actions in the individual
     * and returning the value of the resulting state; random action chosen for the opponent
     * @param individual - individual to be valued
     * @param heuristic - heuristic to be used for state evaluation
     * @param state - current state, root of rollouts
     * @return - value of last state reached
     */
    private double evaluate(Individual individual, StateHeuristic heuristic, StateObservation state) {
        NUM_INDIVIDUALS --;
//        ElapsedCpuTimer elapsedTimerIterationEval = new ElapsedCpuTimer();

        StateObservation st = null;
        int[][] currentState = null;
        if (usingRealFM) {
             st = state.copy();
        } else {
            currentState = LearnedFM.observationToIntGrid(state);
        }
        int winner = -1;

        for (int i = 0; i < SIMULATION_DEPTH; i++) {
            double acum = 0, avg;
            if (!usingRealFM) {
                winner = fm.winner(currentState);
            }

            if (!usingRealFM && winner == -1 || usingRealFM && !st.isGameOver()) {
                ElapsedCpuTimer elapsedTimerIteration = new ElapsedCpuTimer();

                if (!usingRealFM) {
                    currentState = fm.applyAction(currentState, action_mapping.get(individual.actions[i]));
                    currentState = fm.next(currentState);
                } else {
                    st.advance(action_mapping.get(individual.actions[i]));
                }

//                acum += elapsedTimerIteration.elapsedMillis();
//                avg = acum / (i+1);
//                remaining = timer.remainingTimeMillis();
//                if (remaining < 2*avg || remaining < BREAK_MS) break;
            } else {
                break;
            }
        }

        double value;
        if (usingRealFM) {
            value = heuristic.evaluateState(st);
        } else {
            value = fm.score(currentState);
            if (winner == 1) {
                value += 100000;
            } else value -= 100000;
        }

        individual.value = value;

//        numEvals++;
//        acumTimeTakenEval += (elapsedTimerIterationEval.elapsedMillis());
//        avgTimeTakenEval = acumTimeTakenEval / numEvals;
//        remaining = timer.remainingTimeMillis();

        return value;
    }

    /**
     * @return - the individual resulting from crossover applied to the specified population
     */
    private Individual crossover() {
        Individual newind = null;
        if (POPULATION_SIZE > 1) {
            newind = new Individual(SIMULATION_DEPTH, N_ACTIONS, randomGenerator);
            Individual[] tournament = new Individual[TOURNAMENT_SIZE];

            //Select a number of random distinct individuals for tournament and sort them based on value
            for (int i = 0; i < TOURNAMENT_SIZE; i++) {
                int index = randomGenerator.nextInt(population.length);
                tournament[i] = population[index];
            }
            Arrays.sort(tournament);

            //get best individuals in tournament as parents
            newind.crossover(tournament[0], tournament[1], CROSSOVER_TYPE);
        }
        return newind;
    }

    /**
     * Insert a new individual into the population at the specified position by replacing the old one.
     * @param newind - individual to be inserted into population
     * @param pop - population
     * @param idx - position where individual should be inserted
     * @param stateObs - current game state
     */
    private void add_individual(Individual newind, Individual[] pop, int idx, StateObservation stateObs) {
        evaluate(newind, heuristic, stateObs);
        pop[idx] = newind.copy();
    }

    /**
     * Initialize population
     * @param stateObs - current game state
     */
    private void init_pop(StateObservation stateObs) {

//        double remaining = timer.remainingTimeMillis();

        N_ACTIONS = stateObs.getAvailableActions().size() + 1;
        action_mapping = new HashMap<>();
        int k = 0;
        for (Types.ACTIONS action : stateObs.getAvailableActions()) {
            action_mapping.put(k, action);
            k++;
        }
        action_mapping.put(k, Types.ACTIONS.ACTION_NIL);

        population = new Individual[POPULATION_SIZE];
        nextPop = new Individual[POPULATION_SIZE];
        for (int i = 0; i < POPULATION_SIZE; i++) {
//            if (i == 0 || remaining > avgTimeTakenEval && remaining > BREAK_MS) {
            if (NUM_INDIVIDUALS > 0) {
                population[i] = new Individual(SIMULATION_DEPTH, N_ACTIONS, randomGenerator);
                evaluate(population[i], heuristic, stateObs);
//                remaining = timer.remainingTimeMillis();
            } else {break;}
        }

        if (POPULATION_SIZE > 1)
            Arrays.sort(population, new Comparator<Individual>() {
                @Override
                public int compare(Individual o1, Individual o2) {
                    if (o1 == null && o2 == null) {
                        return 0;
                    }
                    if (o1 == null) {
                        return 1;
                    }
                    if (o2 == null) {
                        return -1;
                    }
                    return o1.compareTo(o2);
                }});
        for (int i = 0; i < POPULATION_SIZE; i++) {
            if (population[i] != null)
                nextPop[i] = population[i].copy();
        }

    }

    /**
     * @param pop - last population obtained after evolution
     * @return - first action of best individual in the population (found at index 0)
     */
    private Types.ACTIONS get_best_action(Individual[] pop) {
        int bestAction = pop[0].actions[0];
        return action_mapping.get(bestAction);
    }

}
