package tracks.singlePlayer.advanced.sampleRHEA;
import java.util.Random;

public class Individual implements Comparable{

    protected int[] actions; // actions in individual. length of individual = actions.length
    protected int n; // number of legal actions
    protected double value;
    private Random gen;

    Individual(int L, int n, Random gen) {
        actions = new int[L];
        for (int i = 0; i < L; i++) {
            actions[i] = gen.nextInt(n);
        }
        this.n = n;
        this.gen = gen;
    }

    public void setActions (int[] a) {
        actions = a.clone();
    }

    /**
     * Returns new individual
     * @param MUT - number of genes to mutate
     * @return - new individual mutated from original
     */
    Individual mutate(int MUT) {
        Individual b = this.copy();
        b.setActions(actions);

        int count = 0;
        if (n > 1) { // make sure you can actually mutate
            while (count < MUT) {

                int a; // index of action to mutate

                // find random gene to mutate
                a = gen.nextInt(b.actions.length);

                int s;
                s = gen.nextInt(n); // find new action
                b.actions[a] = s;

                count++;
            }
        }

        return b;
    }

    /**
     * Modifies individual crossover is applied to.
     * @param parent1 - first parent of offspring
     * @param parent2 - second parent of offspring
     * @param CROSSOVER_TYPE - type of crossover to be performed, 1-point or uniform.
     */
    public void crossover (Individual parent1, Individual parent2, int CROSSOVER_TYPE) {
        if (CROSSOVER_TYPE == Agent.POINT1_CROSS) {
            // 1-point
            int p = gen.nextInt(actions.length - 3) + 1;
            for ( int i = 0; i < actions.length; i++) {
                if (i < p)
                    actions[i] = parent1.actions[i];
                else
                    actions[i] = parent2.actions[i];
            }

        } else if (CROSSOVER_TYPE == Agent.UNIFORM_CROSS) {
            // uniform
            for (int i = 0; i < actions.length; i++) {
                if (gen.nextFloat() > 0.5) {
                    actions[i] = parent1.actions[i];
                } else {
                    actions[i] = parent2.actions[i];
                }
            }
        }
    }

    @Override
    public int compareTo(Object o) {
        Individual a = this;
        Individual b = (Individual)o;
        return Double.compare(b.value, a.value);
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Individual)) return false;

        Individual a = this;
        Individual b = (Individual)o;

        for (int i = 0; i < actions.length; i++) {
            if (a.actions[i] != b.actions[i]) return false;
        }

        return true;
    }

    public Individual copy () {
        Individual a = new Individual(this.actions.length, this.n, this.gen);
        a.value = this.value;
        a.setActions(this.actions);

        return a;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("" + value + ": ");
        for (int action : actions) s.append(action).append(" ");
        return s.toString();
    }
}
