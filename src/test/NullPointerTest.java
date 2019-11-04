package test;

public class NullPointerTest {
    public static void main(String[] args) {
        printLength(null);
    }

    static void printLength(String message) {
        System.out.println("Message length is: " + message.length());
    }
}

