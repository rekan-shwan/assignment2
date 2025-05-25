import java.io.File;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class MultiThreading {
    private LongAdder fileCount = new LongAdder();
    private final SynchronousQueue<Integer> syncQueue = new SynchronousQueue<>();
    private boolean counting = true;
    private final File path;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition countUpdated = lock.newCondition();
    private Thread printerThread;

    public MultiThreading(File path) {
        this.path = path;
    }

    private void incrementCount() {
        fileCount.increment();
        countUpdated.signalAll();

        new Thread(() -> {
            try {
                syncQueue.put(fileCount.intValue());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    private void setCounting(boolean value) {
        lock.lock();
        try {
            counting = value;
            countUpdated.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void countPDFFiles(File path) {
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {

                lock.lock();
                try {
                    if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {
                        incrementCount();
                    }
                } finally {
                    lock.unlock();
                }

                if (file.isDirectory()) {
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

                    if (newCount == -1) {
                        break;
                    }

                    if (newCount != lastPrinted) {
                        lastPrinted = newCount;
                        Utilities.printColoredFormatted(
                                "Step %s: %d PDF files counted so far...\n",
                                Utilities.YELLOW, step, newCount);
                    }
                }

                Utilities.printColoredFormatted(
                        "Step %s Final: %d PDF files found.\n",
                        Utilities.GREEN, step, fileCount.intValue());

            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        printerThread.start();
    }

    private void stopPrinter() {
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
        fileCount.reset();
        setCounting(true);

        long startMulti = System.currentTimeMillis();
        Utilities.printColoredln(numThreads + " threads counting PDFs", Utilities.YELLOW);

        File[] subDirs = path.listFiles(File::isDirectory);
        if (subDirs == null || subDirs.length == 0) {
            setCounting(false);
            return;
        }

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
                Thread.currentThread().interrupt();
            }
        }

        setCounting(false);
        stopPrinter();

        long endMulti = System.currentTimeMillis();
        Utilities.printColoredFormatted("%d-thread time: %d ms\n", Utilities.BLUE, numThreads, (endMulti - startMulti));
    }
}