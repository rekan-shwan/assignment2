import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FileCounting {
    private int fileCount = 0;
    private boolean counting = true;
    private File path;

    public FileCounting(File path) {
        this.path = path;
    }

    public synchronized void incrementCount() {
        fileCount++;

    }

    public synchronized int getCount() {
        return fileCount;
    }

    public void countPDFFiles(File path) {
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                    incrementCount();
                } else if (file.isDirectory()) {
                    countPDFFiles(file);
                }
            }
        }
    }

    public void printWhileCounting(String step) {
        Thread printer = new Thread(() -> {
            int lastPrinted = 0;

            while (counting || getCount() > lastPrinted) {
                int currentCount = getCount();
                if (currentCount != lastPrinted) {
                    lastPrinted = currentCount;
                    Utilities.printColoredFormatted(
                            "Step %s: %d PDF files counted so far...\n",
                            Utilities.YELLOW, step, currentCount);
                }

            }

            Utilities.printColoredFormatted(
                    "Step %s Final: %d PDF files found.\n",
                    Utilities.GREEN, step, getCount());
        });

        printer.start();
    }

    public void singleThreadCount() {

        long startTime = System.currentTimeMillis();

        printWhileCounting("1");
        countPDFFiles(path);
        counting = false;

        long endTime = System.currentTimeMillis();

        Utilities.printColoredFormatted(
                "Single thread count completed in %dms\n",
                Utilities.BLUE, (endTime - startTime));
    }

    public void multiThreadCount(int numThreads) {

        fileCount = 0;
        counting = true;

        long startMulti = System.currentTimeMillis();
        Utilities.printColoredln(numThreads + " threads counting PDFs", Utilities.YELLOW);

        File dir = path;
        File[] subDirs = dir.listFiles(File::isDirectory);

        printWhileCounting("2");

        Thread[] threads = new Thread[numThreads];

        for (int i = 0; i < numThreads; i++) {
            int finalI = i;
            threads[i] = new Thread(() -> {
                for (int j = finalI; j < subDirs.length; j += numThreads) {
                    countPDFFiles(subDirs[j]);
                }
            });
            threads[i].start();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        synchronized (this) {
            counting = false;

        }

        long endMulti = System.currentTimeMillis();
        long durationMulti = endMulti - startMulti;

        Utilities.printColoredFormatted("%d-thread time: %d ms\n", Utilities.BLUE, numThreads, durationMulti);
    }

    public void threadPoolPDFCount(int numThread) {
        fileCount = 0;
        counting = true;

        long start = System.currentTimeMillis();
        printWhileCounting("3");

        ExecutorService executor = Executors.newFixedThreadPool(numThread);
        File[] subDirs = path.listFiles(File::isDirectory);

        CountDownLatch latch = new CountDownLatch(numThread);

        for (int i = 0; i < numThread; i++) {
            int finalI = i;
            executor.execute(() -> {
                for (int j = finalI; j < subDirs.length; j += numThread) {
                    countPDFFiles(subDirs[j]);
                }
                latch.countDown();
            });
        }

        executor.shutdown();

        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        synchronized (this) {
            counting = false;

        }

        long end = System.currentTimeMillis();
        long duration = end - start;
        Utilities.printColoredFormatted(
                "Thread pool (%d threads) execution time: %d ms\n",
                Utilities.BLUE, numThread, duration);
    }

}
/*
 * https://www.w3schools.com/java/java_files.asp
 * https://www.geeksforgeeks.org/file-class-in-java/
 * Google Gemini -> 'how to get the time of compiling a thread from start to end
 * in java'
 * ChatGPT -> 'how to count the files in a direcroty and subdirectory in java
 * with multiple threads'
 * ChatGPT -> 'how to use ThreadPool to count files in a directory and
 * subdirectory in java'
 * ChatGPT -> 'how to make the Thread of ThreadPool to wait until all threads
 * are done in java'
 * 
 */
