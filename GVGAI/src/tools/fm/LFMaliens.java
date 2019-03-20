package tools.fm;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;

import java.util.ArrayList;
import java.util.HashMap;

import static ontology.Types.TYPE_FROMAVATAR;
import static tools.fm.LearnedFM.EOS_ID;

@SuppressWarnings("FieldCanBeLocal")
public class LFMaliens implements LFMRules {

    private static int ALIEN_ID = 9;
    private static int AVATAR_ID = 1;
    private static int BASE_ID = 3;
    private static int MISSILE_ID = 2;
    private static int BOMB_ID = 6;

    public int[] getSpriteIDs() {
        return new int[]{-1, AVATAR_ID, MISSILE_ID, BASE_ID, BOMB_ID, ALIEN_ID, EOS_ID};
    }

    /**
     * Applies given action to given grid. Hardcoded for Aliens.
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
     * Score function for the game Aliens, with known scoring rules and sprite type IDs.
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
     * Winner check for the game Aliens, with known termination rules and sprite type IDs.
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
     * Encoded rules of Aliens, transitions for all possible interactions and behaviours.
     * @param vector
     * @return
     */
    public int getTransition(int[] vector) {
        // Find center point:
        int idx = vector.length / 2;
        // Points around the center:
        int topleft = 0;
        int above = 3;
        int topright = 6;
        int left = 1;
        int right = 7;
        int bottomleft = 2;
        int below = 5;
        int bottomright = 9;

        // Aliens, bombs and missiles are going to move and interact with others
        if (vector[idx] == ALIEN_ID || vector[idx] == MISSILE_ID || vector[idx] == BOMB_ID) {
            return -1;
        }
        if (vector[below] == MISSILE_ID) {  // Missile moves up through empty spaces and aliens
            if (vector[idx] == -1 || vector[idx] == ALIEN_ID) return MISSILE_ID;
        }
        if (vector[above] == BOMB_ID) {  // Bombs move down through empty spaces and avatar
            if (vector[idx] == -1 || vector[idx] == AVATAR_ID) return BOMB_ID;
        }
        if (vector[above] == ALIEN_ID && (vector[topright] == EOS_ID || vector[topleft] == EOS_ID)) {
            // Aliens go down when encountering EOS at right & left
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
    public int[] validatePattern(int[] vector, int patternSize) {
        int avCount = 0;
        int alienCount = 0;
        int missileCount = 0;
        int eosCount = 0;

        for (int i = 0; i < vector.length; i++) {
            int i1 = vector[i];
            int row = i / patternSize;
            int col = i % patternSize;

            if (i1 == BOMB_ID) {
                int above = (col-1) * patternSize + row;
                if (above > 0 && above < vector.length && vector[above] == BASE_ID) return null;
            }

            if (i1 == MISSILE_ID) {
                int below = (col+1) * patternSize + row;
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

    public int getCellTypeID(StateObservation so, int i, int j) {
        ArrayList<Observation>[][] from = so.getObservationGrid();
        int cell = -1;
        if (from[i][j] != null && from[i][j].size() > 0)
            if (from[i][j].get(0).category == TYPE_FROMAVATAR)
                cell = MISSILE_ID;
            else cell = from[i][j].get(0).itype;

        return cell;
    }
}
