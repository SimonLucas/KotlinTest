package tools.fm;

import core.game.Observation;
import core.game.StateObservation;
import ontology.Types;
import tools.Vector2d;

import java.util.ArrayList;

import static ontology.Types.TYPE_AVATAR;
import static tools.fm.LearnedFM.*;

@SuppressWarnings("FieldCanBeLocal")
public class LFMmc implements LFMRules {

    private static int CITY_ID = 3;
    private static int MISSILE_ID = 7;
    private static int EXPLOSION_ID = 4;

    private static int AVATAR_UP_ID = 9;
    private static int AVATAR_DOWN_ID = 10;
    private static int AVATAR_RIGHT_ID = 11;
    private static int AVATAR_LEFT_ID = 12;

    public int[] getSpriteIDs() {
        return new int[]{-1, WALL_ID, AVATAR_UP_ID, AVATAR_DOWN_ID, AVATAR_RIGHT_ID, AVATAR_LEFT_ID, CITY_ID,
                MISSILE_ID, EXPLOSION_ID, EOS_ID};
    }

    /**
     * Applies given action to given grid. Oriented avatar that spawns explosions.
     * Explosions destroy missiles, so those are replaced. Empty spaces and EOS are also replaced.
     * @param from - current game state
     * @param action - action to be applied
     * @return - new grid after action is executed
     */
    public int[][] applyAction(int[][] from, Types.ACTIONS action) {
        for (int i = 0; i < from.length; i++) {
            for (int j = 0; j < from[0].length; j++) {
                if (from[i][j] == AVATAR_LEFT_ID) {
                    if (action == Types.ACTIONS.ACTION_LEFT) {
                        if (from[i][j] == AVATAR_LEFT_ID && i > 0 && from[i - 1][j] != WALL_ID) {
                            from[i][j] = -1;
                            from[i - 1][j] = AVATAR_LEFT_ID;
                        } else from[i][j] = AVATAR_LEFT_ID;
                    } else if (action == Types.ACTIONS.ACTION_RIGHT) {
                        from[i][j] = AVATAR_RIGHT_ID;
                    } else if (action == Types.ACTIONS.ACTION_UP) {
                        from[i][j] = AVATAR_UP_ID;
                    } else if (action == Types.ACTIONS.ACTION_DOWN) {
                        from[i][j] = AVATAR_DOWN_ID;
                    } else if (action == Types.ACTIONS.ACTION_USE && i > 0 &&
                            (from[i - 1][j] == -1 || from[i - 1][j] == EOS_ID || from[i - 1][j] == MISSILE_ID)) {
                        from[i - 1][j] = EXPLOSION_ID;  // Spawn missile
                    }
                } else if (from[i][j] == AVATAR_RIGHT_ID) {
                    if (action == Types.ACTIONS.ACTION_RIGHT) {
                        if (from[i][j] == AVATAR_RIGHT_ID && i < from.length - 1 && from[i + 1][j] != WALL_ID) {
                            from[i][j] = -1;
                            from[i + 1][j] = AVATAR_RIGHT_ID;
                        } else from[i][j] = AVATAR_RIGHT_ID;
                    } else if (action == Types.ACTIONS.ACTION_LEFT) {
                        from[i][j] = AVATAR_LEFT_ID;
                    } else if (action == Types.ACTIONS.ACTION_UP) {
                        from[i][j] = AVATAR_UP_ID;
                    } else if (action == Types.ACTIONS.ACTION_DOWN) {
                        from[i][j] = AVATAR_DOWN_ID;
                    } else if (action == Types.ACTIONS.ACTION_USE && i < from.length - 1 &&
                            (from[i + 1][j] == -1 || from[i + 1][j] == EOS_ID || from[i + 1][j] == MISSILE_ID)) {
                        from[i + 1][j] = EXPLOSION_ID;  // Spawn missile
                    }
                } else if (from[i][j] == AVATAR_UP_ID) {
                    if (action == Types.ACTIONS.ACTION_UP) {
                        if (from[i][j] == AVATAR_UP_ID && j > 0 && from[i][j - 1] != WALL_ID) {
                            from[i][j] = -1;
                            from[i][j - 1] = AVATAR_UP_ID;
                        } else from[i][j] = AVATAR_UP_ID;
                    } else if (action == Types.ACTIONS.ACTION_LEFT) {
                        from[i][j] = AVATAR_LEFT_ID;
                    } else if (action == Types.ACTIONS.ACTION_RIGHT) {
                        from[i][j] = AVATAR_RIGHT_ID;
                    } else if (action == Types.ACTIONS.ACTION_DOWN) {
                        from[i][j] = AVATAR_DOWN_ID;
                    } else if (action == Types.ACTIONS.ACTION_USE && j > 0 &&
                            (from[i][j - 1] == -1 || from[i][j - 1] == EOS_ID || from[i][j - 1] == MISSILE_ID)) {
                        from[i][j - 1] = EXPLOSION_ID;  // Spawn missile
                    }
                } else if (from[i][j] == AVATAR_DOWN_ID) {
                    if (action == Types.ACTIONS.ACTION_DOWN) {
                        if (from[i][j] == AVATAR_DOWN_ID && j < from[0].length - 1 && from[i][j + 1] != WALL_ID) {
                            from[i][j] = -1;
                            from[i][j + 1] = AVATAR_DOWN_ID;
                        } else from[i][j] = AVATAR_DOWN_ID;
                    } else if (action == Types.ACTIONS.ACTION_LEFT) {
                        from[i][j] = AVATAR_LEFT_ID;
                    } else if (action == Types.ACTIONS.ACTION_RIGHT) {
                        from[i][j] = AVATAR_RIGHT_ID;
                    } else if (action == Types.ACTIONS.ACTION_UP) {
                        from[i][j] = AVATAR_UP_ID;
                    } else if (action == Types.ACTIONS.ACTION_USE && j < from[0].length - 1 &&
                            (from[i][j + 1] == -1 || from[i][j + 1] == EOS_ID || from[i][j + 1] == MISSILE_ID)) {
                        from[i][j + 1] = EXPLOSION_ID;  // Spawn missile
                    }
                }
            }
        }
        return from;
    }

    /**
     * Score function for the game Missile Command, with known scoring rules and sprite type IDs.
     * @param grid - current observation grid
     * @return - score based on observation grid
     */
    public double score(int[][] grid) {
        double score = 0;
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell == MISSILE_ID) {
                    score -= 2;
                } else if (cell == CITY_ID) {
                    score += 1;
                }
            }
        }
        return score;
    }

    /**
     * Winner check for the game Missile Command, with known termination rules and sprite type IDs.
     * @param grid - current observation grid
     * @return - 0 if lost, 1 if won, -1 if still going
     */
    public int winner(int[][] grid) {
        int winner = -1;
        boolean foundCity = false;
        boolean foundMissile = false;
        boolean foundAvatar = false;
        for (int[] row : grid) {
            for (int cell : row) {
                if (cell == CITY_ID) {
                    foundCity = true;
                } else if (cell == MISSILE_ID) {
                    foundMissile = true;
                } else if (cell == AVATAR_ID) {
                    foundAvatar = true;
                }
            }
        }
        if (foundAvatar) {
            if (!foundCity) winner = 0;
            if (!foundMissile) winner = 1;
        } else winner = 0;
        return winner;
    }

    /**
     * Encoded rules of Missile Command, transitions for all possible interactions and behaviours.
     * The only sprites that move are missiles. We don't know where cities are, move down. // TODO: stupid
     * Interactions:
     * - explosion <> missile (destroys both) - done in avatar actions
     * - missile <> city (destroy city)
     * - missile <> wall (bounce back)
     * - explosion (ticks out)
     * @param vector - given pattern
     */
    public int getTransition(int[] vector) {
        // Find center point:
        int idx = vector.length / 2;
        // Points around the center:
        int above = 3;

        // Missiles are going to move
        if (vector[idx] == MISSILE_ID) {
            return -1;
        }

        // Missiles travel down through everything but walls
        if (vector[above] == MISSILE_ID) {
            if (vector[idx] != WALL_ID) return MISSILE_ID;
        }

        // Explosions tick out
        if (vector[idx] == EXPLOSION_ID) return -1;
        return vector[idx]; // No change
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
        int cityCount = 0;
        int missileCount = 0;
        int eosCount = 0;

        for (int i1 : vector) {
            if (i1 == AVATAR_LEFT_ID || i1 == AVATAR_RIGHT_ID || i1 == AVATAR_DOWN_ID || i1 == AVATAR_UP_ID) avCount++;
            else if (i1 == CITY_ID) cityCount++;
            else if (i1 == MISSILE_ID) missileCount++;
            else if (i1 == EOS_ID) eosCount++;
        }
        if (avCount > 1 || cityCount > 2 || missileCount > 8 || eosCount > 5) return null;
        return vector;
    }

    public int getCellTypeID(StateObservation so, int i, int j) {
        ArrayList<Observation>[][] from = so.getObservationGrid();
        int cell = -1;
        if (from[i][j] != null && from[i][j].size() > 0)
            if (from[i][j].get(0).category == TYPE_AVATAR) {
                Vector2d or = so.getAvatarOrientation();
                if (or.x == -1) cell = AVATAR_LEFT_ID;
                else if (or.x == 1) cell = AVATAR_RIGHT_ID;
                else if (or.y == -1) cell = AVATAR_DOWN_ID;
                else if (or.y == 1) cell = AVATAR_UP_ID;
            }
            else cell = from[i][j].get(0).itype;
        return cell;
    }
}
