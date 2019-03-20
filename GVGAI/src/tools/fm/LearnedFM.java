package tools.fm;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class LearnedFM {

    static int WALL_ID = 0;
    static int AVATAR_ID = 1;
    static int EOS_ID = -2;

    private static LearnedFM forwardModel;
    private static LFMRules fmRules;
    public static LearnedFM getInstance(int capacity) {
        if (forwardModel == null) {
            forwardModel = new LearnedFM(capacity);
        }
        forwardModel.capacity = capacity;
        return forwardModel;
    }

    private HashMap<Pattern, Integer> transitions;
    private int patternSize = 3;
    private int capacity;

    private LearnedFM(int capacity) {
        transitions = new HashMap<>();
        this.capacity = capacity;
//        fmRules = new LFMaliens();  // TODO: change this if using a different game
        fmRules = new LFMmc();
    }

    public HashMap<Pattern, Integer> getTransitions() {
        return transitions;
    }

    public int getProgress() { return transitions.size(); }

    public void reset() { transitions.clear(); }

    public LearnedFM copy() {
        LearnedFM copy = new LearnedFM(capacity);
        for (Map.Entry<Pattern, Integer> e : transitions.entrySet()) {
            copy.transitions.put(e.getKey().copy(), e.getValue());
        }
        return copy;
    }

    public boolean atCapacity() {
        return (capacity != -1 && transitions.size() >= capacity) ||
            (transitions.size() >= Math.pow(5, 9));
    }

    public int train(int oldCapacity, int nPatterns) {
        if (oldCapacity == -1) {
            transitions.clear();
        }
        int[] ids = fmRules.getSpriteIDs();
        ArrayList<int[]> vectorList = combinationRepetition(ids, patternSize * patternSize);
        if (nPatterns == -1) nPatterns = vectorList.size();
        System.out.println(vectorList.size() + " patterns found, using " + nPatterns + " for training.");
        int count = 0;
        int i = oldCapacity;
        while (count < nPatterns && i < vectorList.size()) {
            int[] vector = vectorList.get(i);
            i++;
            transitions.put(new Pattern(vector), getTransition(vector));
            count++;
        }
        return vectorList.size();
    }

    /**
     * Adds pattern data to the hashmap. Considering only first observation in the list, if any. If none, -1.
     * @param from - previous observation of the grid, tick t
     * @param to - new observation of the grid, tick t+1
     */
    public void addData(int[][] from, int[][] to) {
        if (capacity != -1 && transitions.size() >= capacity) return;

        // Go through all cells in the grid
        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < from[i].length; j++) {
                // Get first observation at cell (i, j) from 2nd observation grid
                int newCell = to[i][j];

                // Get pxp grid around point (i,j) from 1st grid and flatten it into array.
                int[] vector = vectorExtractor(from, i, j);

                // Add data into hashmap
                transitions.put(new Pattern(vector), newCell);
            }
        }
    }

    /**
     * Function to transition from one grid to another.
     * @param from - current grid
     * @return next grid according to current learned model
     */
    public int[][] next(int[][] from) {
        int[][] to = new int[from.length][from[0].length];
        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < from[0].length; j++) {
                // Get pxp grid around point (i,j) from 1st grid and flatten it into array.
                int[] vector = vectorExtractor(from, i, j);

                // Check if pattern is recognized
                Integer newCell = transitions.get(new Pattern(vector));
                if (newCell != null) {
                    // Pattern exists, put value into new grid
                    to[i][j] = newCell;
                } else {
                    // Pattern doesn't exist yet, assume value remains the same, so put the same as in 1st grid
                    to[i][j] = from[i][j];
                }
            }
        }
        return to;
    }

    public double checkPrediction(int[][] from, int[][] actualTo) {
        int[][] prediction = next(from);
        return checkAccuracy(prediction, actualTo);
    }

    public double checkAccuracy(int[][] predictionTo, int[][] actualTo) {
        int count = 0;
        int[][] diff = new int[predictionTo.length][predictionTo[0].length];
        for (int i = 0; i < predictionTo.length; i++) {
            for (int j = 0; j < predictionTo[0].length; j++) {
                if (predictionTo[i][j] == actualTo[i][j]) {
                    diff[i][j] = 1;
                    count++;
                }
            }
        }
//        System.out.println(diff);  // Can display this in a graph
        return count * 1.0 / (predictionTo.length * predictionTo[0].length);
    }

    public static int[][] observationToIntGrid(StateObservation so) {
        ArrayList<Observation>[][] from = so.getObservationGrid();
        int[][] to = new int[from.length][from[0].length];
        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < from[0].length; j++) {
                to[i][j] = getCellTypeID(so, i, j);
            }
        }
        return to;
    }

    /**
     * Retrieves neighbours of point i, j.
     * Considers type ID of first observation in each cell only
     * @param grid - given grid to search
     * @param i - point row
     * @param j - point column
     * @return - flattened array of ints
     */
    private int[] vectorExtractor(int[][] grid, int i, int j) {
        int[] vector = new int[patternSize * patternSize];
        int k = 0;
        for (int x = i - patternSize/2; x < i + patternSize/2 + 1; x++) {
            for (int y = j - patternSize/2; y < j + patternSize/2 + 1; y++) {
                vector[k] = EOS_ID;
                if (x >= 0 && x < grid.length && y >= 0 && y < grid[0].length)
                    vector[k] = grid[x][y];
                k++;
            }
        }
        return vector;
    }

    /**
     * Recursively generate all combinations of elements in arr[] with repetitions.
     * @param vectorList - list to keep vectors in to retrieve later
     * @param chosen - temp array to store indices of current combination
     * @param arr - input array
     * @param index - current index
     * @param r - size of combination
     * @param start - start index in arr[]
     * @param end - end index in arr[]
     */
    private static void combinationRepetitionUtil(ArrayList<int[]> vectorList, int[] chosen, int[] arr, int index,
                                                   int r, int start, int end) {
        if (index == r) {
            int[] vector = new int[r];
            int avCount = 0;
            for (int i = 0; i < r; i++) {
                vector[i] = arr[chosen[i]];
                if (vector[i] == AVATAR_ID) avCount++;
            }
            if (avCount > 0) {
                int[][] vectors = fmRules.getAvatarVariations(vector);
                if (vectors != null) {
                    for (int[] v : vectors) {
                        if (validatePattern(v, (int) Math.sqrt(arr.length)) != null)
                            vectorList.add(v);
                    }
                    return;
                }
            }

            if (validatePattern(vector, (int)Math.sqrt(arr.length)) != null)
                vectorList.add(vector);
            return;
        }

        // One by one choose all elements (without considering the fact whether element is already chosen or not)
        // and recur
        for (int i = start; i <= end; i++) {
            chosen[index] = i;
            combinationRepetitionUtil(vectorList, chosen, arr, index + 1, r, start, end);
        }
    }

    /**
     * Generate all combinations of elements in arr[] of size r, with repetitions, and return list of vectors.
     * @param arr - input array
     * @param r - size of output vectors
     * @return - list of vectors combining elements in arr[]
     */
    private static ArrayList<int[]> combinationRepetition(int[] arr, int r) {
        // Allocate memory
        ArrayList<int[]> vectorList = new ArrayList<>();
        int[] chosen = new int[r + 1];

        // Call the recursive function
        combinationRepetitionUtil(vectorList, chosen, arr, 0, r, 0, arr.length - 1);

        return vectorList;
    }

    /**
     * Game-specific functions.
     */

    public int[][] applyAction(int[][] from, Types.ACTIONS action) {
        return fmRules.applyAction(from, action);
    }
    public double score(int[][] grid) {
        return fmRules.score(grid);
    }
    public int winner(int[][] grid) {
        return fmRules.winner(grid);
    }
    private int getTransition(int[] vector) {
        return fmRules.getTransition(vector);
    }
    private static int[] validatePattern(int[] vector, int patternSize) {
        return fmRules.validatePattern(vector, patternSize);
    }
    private static int getCellTypeID(StateObservation so, int i, int j) {
        return fmRules.getCellTypeID(so, i, j);
    }
}

final class Pattern {
    private int[] vector;

    Pattern(int[] vector) {
        this.vector = vector.clone();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Pattern)) return false;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] != ((Pattern)obj).vector[i]) return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(vector);
    }

    @Override
    public String toString() {
        int patternSize = 3;
        StringBuilder s = new StringBuilder("\n[");
        for (int i = 0; i < vector.length; i++) {
            if (i != 0 && i % patternSize == 0) s.append("\n");
            s.append(vector[i]);
            if (i != vector.length - 1) s.append(" ");
        }
        s.append("]");
//        return Arrays.toString(vector);
        return s.toString();
    }

    Pattern copy() {
        return new Pattern(vector);
    }
}
