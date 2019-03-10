package tools;

import core.game.Observation;
import ontology.Types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static ontology.Types.TYPE_FROMAVATAR;

@SuppressWarnings("FieldCanBeLocal")
public class LearnedFM {

    private static int ALIEN_ID = 9;
    private static int AVATAR_ID = 1;
    private static int BASE_ID = 3;
    private static int MISSILE_ID = 2;
    private static int BOMB_ID = 6;
    private static int EOS_ID = 4;

    private static LearnedFM forwardModel;
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
    }

    public HashMap<Pattern, Integer> getTransitions() {
        return transitions;
    }

    public int getProgress() { return transitions.size(); }

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

    public void train(int nPatterns) {
        transitions.clear();
        int[] ids = new int[]{-1, AVATAR_ID, MISSILE_ID, BASE_ID, BOMB_ID, ALIEN_ID};
        ArrayList<int[]> vectorList = combinationRepetition(ids, patternSize * patternSize);
        if (nPatterns == -1) nPatterns = vectorList.size();
        System.out.println(vectorList.size() + " patterns found, using " + nPatterns + " for training.");
        for (int i = 0; i < nPatterns; i++) {
            int[] vector = vectorList.get(i);
            int[] validate = validatePattern(vector);
            if (i % 1000 == 0) System.out.println((nPatterns - i) + " remaining");
            if (validate == null) continue;
            transitions.put(new Pattern(vector), getTransition(vector));
        }
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

    /**
     * Applies given action to given grid. Hardcoded for Aliens. NOT GENERAL.
     * @param from - current game state
     * @param action - action to be applied
     * @return - new grid after action is executed
     */
    public int[][] applyAction(int[][] from, Types.ACTIONS action) {
        int x = -1;
        int y = -1;
        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < from[0].length; j++) {
                if (from[i][j] == AVATAR_ID) {
                    x = i;
                    y = j;
                }
            }
        }
        if (x != -1 && y != -1) {
            if (action == Types.ACTIONS.ACTION_LEFT && x > 0) {
                from[x][y] = -1;
                from[x-1][y] = AVATAR_ID;
            } else if (action == Types.ACTIONS.ACTION_RIGHT && x < from.length - 1) {
                from[x][y] = -1;
                from[x+1][y] = AVATAR_ID;
            } else if (action == Types.ACTIONS.ACTION_USE && y > 0) {
                from[x][y-1] = MISSILE_ID;  // Spawn missile
            }
        }
        return from;
    }

    /**
     * Score function for the game Aliens, with known scoring rules and sprite type IDs. NOT GENERAL.
     * @param grid - current observation grid
     * @return - score based on observation grid
     */
    public double score(int[][] grid) {
        double score = 0;
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell == ALIEN_ID) {
                    score -= 2;
                } else if (cell == BASE_ID) {
                    score -= 1;
                }
            }
        }
        return score;
    }

    /**
     * Winner check for the game Aliens, with known termination rules and sprite type IDs. NOT GENERAL.
     * @param grid - current observation grid
     * @return - 0 if lost, 1 if won, -1 if still going
     */
    public int winner(int[][] grid) {
        int winner = -1;
        boolean foundAlien = false;
        boolean foundAvatar = false;
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell == ALIEN_ID) {
                    foundAlien = true;
                } else if (cell == AVATAR_ID) {
                    foundAvatar = true;
                }
            }
        }
        if (foundAvatar) {
            if (!foundAlien) winner = 1;
        } else winner = 0;
        return winner;
    }

    /**
     * Encoded rules of Aliens, transitions for all possible interactions and behaviours. NOT GENERAL.
     * @param vector
     * @return
     */
    private int getTransition(int[] vector) {
        // i = patternSize * row + col
        // Find center point:
        int idx = vector.length / 2;
        // Points around the center:
        int topleft = 0;
        int above = 1;
        int topright = 2;
        int left = 3;
        int right = 5;
        int bottomleft = 6;
        int below = 7;
        int bottomright = 8;

        // Aliens, bombs and missiles are going to move and interact with others
        if (vector[idx] == ALIEN_ID || vector[idx] == MISSILE_ID || vector[idx] == BOMB_ID) {
            return -1;
        }
        if (vector[below] == MISSILE_ID) {
            if (vector[idx] == -1 || vector[idx] == ALIEN_ID) return MISSILE_ID;
            else return -1;
        }
        if (vector[above] == BOMB_ID) {
            if (vector[idx] == -1) return BOMB_ID;
            else return -1;
        }
        if (vector[above] == ALIEN_ID && vector[topright] == EOS_ID) {
            return ALIEN_ID;
        }
        // TODO: we don't know the movement direction of the aliens on horizontal
        // Assuming pessimistic model, avatar is destroyed by aliens, nothing else is
        if ((vector[left] == ALIEN_ID || vector[right] == ALIEN_ID)
                && (vector[idx] == AVATAR_ID || vector[idx] == -1)) return ALIEN_ID;

        return vector[idx]; // No change found
    }

    /**
     * Validates a given pattern according to some simple Aliens rules:
     * - only 1 avatar
     * - max 6 aliens
     * - max 2 missiles
     * - max 5 end of screen tiles
     * - avatar not on top row
     * - no base above bomb
     * - no base below missile
     * @param vector - given pattern to check
     * @return - null if pattern does not validate, pattern back if validates.
     */
    private int[] validatePattern(int[] vector) {
        int avCount = 0;
        int alienCount = 6;
        int missileCount = 0;
        int eosCount = 0;

        for (int i = 0; i < vector.length; i++) {
            int i1 = vector[i];
            int row = i / patternSize;
            int col = i % patternSize;

            if (i1 == BOMB_ID) {
                int above = (row-1) * patternSize + col;
                if (above > 0 && above < vector.length && vector[above] == BASE_ID) return null;
            }

            if (i1 == MISSILE_ID) {
                int below = (row+1) * patternSize + col;
                if (below > 0 && below < vector.length && vector[below] == BASE_ID) return null;
            }

            if (i1 == AVATAR_ID) avCount++;
            else if (i1 == ALIEN_ID) alienCount++;
            else if (i1 == MISSILE_ID) missileCount++;
            else if (i1 == EOS_ID) eosCount++;
        }
        if (avCount > 1 || alienCount > 6 || missileCount > 2 || eosCount > 5) return null;
        for (int i = 0; i < vector.length; i++) {
            if (vector[i] == AVATAR_ID && i < patternSize) return null;
        }
        return vector;
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

    public static int[][] observationToIntGrid(ArrayList<Observation>[][] from) {
        int[][] to = new int[from.length][from[0].length];
        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < from[0].length; j++) {
                to[i][j] = getCellTypeID(from, i, j);
            }
        }
        return to;
    }

    private static int getCellTypeID(ArrayList<Observation>[][] from, int i, int j) {
        int cell = -1;
        if (from[i][j] != null && from[i][j].size() > 0)
            if (from[i][j].get(0).category == TYPE_FROMAVATAR)
                cell = MISSILE_ID;
            else cell = from[i][j].get(0).itype;

        return cell;
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
        for (int x = i - patternSize/2; x < i + patternSize/2; x++) {
            for (int y = j - patternSize/2; y < j + patternSize/2; y++) {
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
            for (int i = 0; i < r; i++) {
                vector[i] = arr[chosen[i]];
            }
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
