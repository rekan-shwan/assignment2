import java.io.File;
import java.util.concurrent.locks.ReentrantLock;

public class Test {

    public static void main(String[] args) {
        Test2 test2 = new Test2();
        String path = "C:\\Users\\rekan\\Desktop";

        Thread thread = new Thread(() -> {
            test2.lock.lock();
            try {
                int count = test2.countPDFFiles(new File(path));
                System.out.println("Total PDF files found: " + count);
            } finally {

            }
        });

        Thread thread2 = new Thread(() -> {
            test2.lock.lock(); //
            try {
                int count = test2.countPDFFiles(new File(path));
                System.out.println("Total PDF files found: " + count);
            } finally {
                test2.lock.unlock(); //
            }
        });

        thread.start();
        thread2.start();

        try {
            thread.join();
            thread2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

class Test2 {
    public int count = 0;
    public ReentrantLock lock = new ReentrantLock();

    public int countPDFFiles(File path) {
        int localCount = 0;
        File[] files = path.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(".pdf")) {

                    try {
                        count++;
                        localCount++;
                    } finally {

                    }
                } else if (file.isDirectory()) {
                    localCount += countPDFFiles(file);
                }
            }
        }
        return localCount;
    }
}