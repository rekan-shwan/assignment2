import java.util.Scanner;

public class Utilities {
    public static final String RED = "\u001B[31m";
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String BLUE = "\u001B[34m";
    public static final String RESET = "\u001B[0m";

    private static Scanner scanner;
    
    public static void printColored(String text, String color) {
        System.out.print(color + text + RESET);
    }
    public static void printColoredln(String text, String color) {
        System.out.println(color + text + RESET);
    }

    public static void printColoredFormatted(String text, String color, Object... args) {
        System.out.printf(color + text + RESET, args);
    }

    public static String getUserInput() {
        scanner = new Scanner(System.in);
        return scanner.nextLine();
    }
    public static void closeScanner() {
        if (scanner != null) {
            scanner.close();
        }
    }
}

/*
 * chatgpt -> 'colors in java with printlin'
 */
