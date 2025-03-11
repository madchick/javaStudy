/*

장점
- 시간 제한 잠금, 조건 변수 등 다양한 동기화 기능을 제공합니다.
- 잠금 획득 및 해제를 명시적으로 제어할 수 있습니다.
- synchronized 키워드보다 성능이 우수합니다.
- 락을 얻는데 실패할경우 대기시간을 설정하거나, 락을 얻지않고 다른 작업을 수행하도록 설정할수 있습니다.
- 락을 요청한 순서대로 락을 획득하도록 설정할수 있습니다.

단점
- synchronized 키워드보다 사용하기 복잡합니다.
- 잠금 및 해제를 명시적으로 처리해야 하므로 코드가 길어질 수 있습니다.
- finally 구문을 사용하여 unlock()을 보장해야 하기때문에 개발자의 책임이 증가합니다.

*/

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LockExample {
    private int count = 0;
    private Lock lock = new ReentrantLock();

    public void increment() {
        lock.lock();
        try {
            count++;
        } finally {
            lock.unlock();
        }
    }

    public int getCount() {
        lock.lock();
        try {
            return count;
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LockExample example = new LockExample();

        Thread thread1 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                example.increment();
            }
        });

        Thread thread2 = new Thread(() -> {
            for (int i = 0; i < 1000; i++) {
                example.increment();
            }
        });

        thread1.start();
        thread2.start();
        thread1.join();
        thread2.join();

        System.out.println("Count: " + example.getCount()); // 결과: 2000
    }
}