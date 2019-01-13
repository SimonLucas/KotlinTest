package test;

import ggi.AbstractGameState;

public class NewInstanceTest {
    public AbstractGameState getNew(AbstractGameState gameState) throws Exception {
        return gameState.getClass().newInstance();
    }
}
