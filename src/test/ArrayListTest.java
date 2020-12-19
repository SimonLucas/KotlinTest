package test;

import java.util.ArrayList;

public class ArrayListTest {
    public static void main(String[] args) {

        // demonstrates that the Java ArrayList is HashMap ready
        ArrayList<Integer> a1 = new ArrayList<Integer>();
        ArrayList<Integer> a2 = new ArrayList<Integer>();

        a1.add(1);
        a2.add(1);

        System.out.println(a1.equals(a2));

        System.out.println(a1.hashCode());
        System.out.println(a2.hashCode());
    }
}

