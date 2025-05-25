import java.io.File;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicBoolean;

public class MultiThreadingBonus {
    private final AtomicInteger fileCount = new AtomicInteger(0);
    private final SynchronousQueue<Integer> syncQueue = new SynchronousQueue<>();
    private final AtomicBoolean counting = new AtomicBoolean(true);
    private final File path;
    private Thread printerThread;

    public MultiThreadingBonus(File path) {
        this.path = path;
    }

    private void incrementCount() {
        int newValue = fileCount.incrementAndGet();
        try {
            syncQueue.put(newValue);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void countPDFFiles(File dir) {
        File[] files = dir.listFiles();
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
        printerThread = new Thread(() -> {
            int lastPrinted = 0;
            try {
                while (true) {
                    int newCount = syncQueue.take();
                    if (newCount == -1)
                        break;

                    if (newCount != lastPrinted) {
                        lastPrinted = newCount;
                        Utilities.printColoredFormatted(
                                "Step %s: %d PDF files counted so far...\n",
                                Utilities.YELLOW, step, newCount);
                    }
                }

                Utilities.printColoredFormatted(
                        "Step %s Final: %d PDF files found.\n",
                        Utilities.GREEN, step, fileCount.get());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        printerThread.start();
    }

    private void stopPrinter() {
        counting.set(false);
        try {
            syncQueue.put(-1);
            if (printerThread != null) {
                printerThread.join();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public void multiThreadCount(int numThreads) {
        fileCount.set(0);
        counting.set(true);

        long startTime = System.currentTimeMillis();
        Utilities.printColoredln(numThreads + " threads counting PDFs (Using SynchronousQueue)", Utilities.YELLOW);

        File[] subDirs = path.listFiles(File::isDirectory);
        if (subDirs == null || subDirs.length == 0) {
            counting.set(false);
            return;
        }

        printWhileCounting("Bonus");

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
                Thread.currentThread().interrupt();
            }
        }

        stopPrinter();

        long endTime = System.currentTimeMillis();
        Utilities.printColoredFormatted(
                "%d-thread (SynchronousQueue) count completed in %d ms\n",
                Utilities.BLUE, numThreads, (endTime - startTime));
    }
}
