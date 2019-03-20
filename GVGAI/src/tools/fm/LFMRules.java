package tools.fm;

import core.game.StateObservation;
import ontology.Types;

public interface LFMRules {

    int[] getSpriteIDs();
    int[][] applyAction(int[][] from, Types.ACTIONS action);
    double score(int[][] grid);
    int winner(int[][] grid);
    int getTransition(int[] vector);
    int[] validatePattern(int[] vector, int patternSize);
    int getCellTypeID(StateObservation so, int i, int j);
}
