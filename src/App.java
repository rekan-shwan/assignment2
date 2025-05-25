// Rekan Shwan  rs21131@auis.edu.krd
// Shakar Latif sl21081@auis.edu.krd

public class App {
    public static void main(String[] args) {
        Utilities.printColoredln(
                "Welcome to the File Extension Filter App!\n" +
                        "Counting PDF files in a directory.\n" +
                        "with Single Thread, Multi Thread (4 cores), and Half of your cores .\n",
                Utilities.GREEN);
        counterPDF();
    }

    public static void counterPDF() {
        String path = "/";
        Details details = new Details();
        do {
            Utilities.printColoredln("Please enter the path:", Utilities.YELLOW);
            path = Utilities.getUserInput();

            if (details.setPath(path)) {
                Utilities.printColoredFormatted("Path set successfully! \n", Utilities.GREEN, "%s", path);
                break;
            } else {
                Utilities.printColoredln("Invalid path. Please try again.\n", Utilities.RED);
            }
        } while (true);

        Utilities.closeScanner();
        FileCounting fileCounter = new FileCounting(details.getPath());

        fileCounter.singleThreadCount();
        // fileCounter.multiThreadCount(4);
        // fileCounter.threadPoolPDFCount(Runtime.getRuntime().availableProcessors() +
        // 1);

        MultiThreading multiThreading = new MultiThreading(details.getPath());
        multiThreading.multiThreadCount(4);
        HalfThreads halfThreads = new HalfThreads(details.getPath());
        halfThreads.threadPoolPDFCount(Runtime.getRuntime().availableProcessors() /
                2);
        Utilities.printColoredln("Bonus, ", Utilities.GREEN);
        MultiThreadingBonus multiThreadingBonus = new MultiThreadingBonus(details.getPath());
        multiThreadingBonus.multiThreadCount(4);

    }
}
