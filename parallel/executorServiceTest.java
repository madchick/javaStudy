import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExecutorServiceExample {
    public static void main(String[] args) {
        // 스레드 풀 생성 (최대 2개의 스레드)
        ExecutorService executor = Executors.newFixedThreadPool(10);

        // 작업 정의
        Runnable task = () -> {
            for (int i = 0; i < 5; i++) {
                System.out.println(Thread.currentThread().getName() + ": " + i);
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };

        // 작업 제출
        for (int i = 0; i < 4; i++) {
            executor.submit(task);
        }

        // 스레드 풀 종료
        executor.shutdown();
    }
}