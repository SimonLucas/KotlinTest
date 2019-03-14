package utilities;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class LearningStats {

    float[][] stats;

    public LearningStats(int nsteps){
        stats = new float[nsteps][3];
    }

    public void storeTick(int tick, int knownPatterns, float accuracy, double points){
        if (tick < stats.length){
            stats[tick][0] = knownPatterns;
            stats[tick][1] = accuracy;
            stats[tick][2] = (float) points;
        }
        System.out.println("Tick " + tick + ": " + knownPatterns + ", " + accuracy + ", " + points);
    }

    public void writeToFile(String filename){
        try {
            BufferedWriter br = new BufferedWriter(new FileWriter(filename));
            br.write("tick, knownPatterns, modelaccuracy, agentscore\n");
            int i = 0;
            for (float[] stats_entry : stats) {
                StringBuilder sb = new StringBuilder();
                sb.append(i++);
                sb.append(",");
                sb.append(stats_entry[0]);
                sb.append(",");
                sb.append(stats_entry[1]);
                sb.append(",");
                sb.append(stats_entry[2]);
                sb.append("\n");

                br.write(sb.toString());
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
