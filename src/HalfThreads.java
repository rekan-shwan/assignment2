import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HalfThreads {
    private volatile int fileCount = 0;
    private volatile boolean counting = true;
    private File path;
    private Thread printerThread;

    private final Object notificationLock = new Object();

    public HalfThreads(File path) {
        this.path = path;
    }

    public void incrementCount() {
        synchronized (notificationLock) {
            fileCount++;
            notificationLock.notifyAll();
        }
    }

    public int getCount() {
        return fileCount;
    }

    public boolean isCounting() {
        return counting;
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
        printerThread = new Thread(() -> {
            int lastPrinted = 0;

            synchronized (notificationLock) {
                try {
                    while (isCounting() || getCount() > lastPrinted) {
                        int currentCount = getCount();

                        if (currentCount != lastPrinted) {
                            lastPrinted = currentCount;
                            Utilities.printColoredFormatted(
                                    "Step %s: %d PDF files counted so far...\n",
                                    Utilities.YELLOW, step, currentCount);
                        }

                        if (isCounting() && currentCount == lastPrinted) {
                            notificationLock.wait(100);
                        }
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            Utilities.printColoredFormatted(
                    "Step %s Final: %d PDF files found.\n",
                    Utilities.GREEN, step, getCount());
        });

        printerThread.start();
    }

    private void stopPrinter() {
        synchronized (notificationLock) {
            counting = false;
            notificationLock.notifyAll();
        }

        if (printerThread != null) {
            try {
                printerThread.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public void threadPoolPDFCount(int numThread) {
        fileCount = 0;
        counting = true;

        long start = System.currentTimeMillis();
        printWhileCounting("3");

        ExecutorService executor = Executors.newFixedThreadPool(numThread);
        File[] subDirs = path.listFiles(File::isDirectory);

        if (subDirs == null || subDirs.length == 0) {
            stopPrinter();
            return;
        }

        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch completionLatch = new CountDownLatch(numThread);

        for (int i = 0; i < numThread; i++) {
            int finalI = i;
            executor.execute(() -> {
                try {

                    startLatch.await();

                    // Now all threads start together
                    for (int j = finalI; j < subDirs.length; j += numThread) {
                        countPDFFiles(subDirs[j]);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    completionLatch.countDown();
                }
            });
        }

        startLatch.countDown();

        executor.shutdown();

        try {
            completionLatch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        stopPrinter();

        long end = System.currentTimeMillis();
        long duration = end - start;
        Utilities.printColoredFormatted(
                "Thread pool (%d threads) execution time: %d ms\n",
                Utilities.BLUE, numThread, duration);
    }
}