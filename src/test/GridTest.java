package test;

public class GridTest {
    int gridWidth = 10;
    int gridHeight = 50;
    int[][] bricks = new int[gridWidth][gridHeight];

    void printTest() {
        for (int i=0; i<gridWidth; i++) {
            for (int j=0; j<gridHeight; j++) {
                System.out.println(bricks[i][j]);
            }
        }
    }



}
