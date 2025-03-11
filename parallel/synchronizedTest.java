/*

장점
- 사용하기 쉽고 코드가 간결합니다.
- 자바 언어 자체에서 제공하는 기능이므로 별도의 라이브러리 추가가 필요하지 않습니다.
- 코드의 가독성이 높고 이해하기 쉽습니다.

단점
- 블록킹 방식으로 인해 성능 저하가 발생할 수 있습니다.
- 대기 시간이 길어질 경우 스레드 컨텍스트 스위칭으로 인한 오버헤드가 발생합니다.
- 시간 제한 잠금, 조건 변수 등 고급 동기화 기능을 제공하지 않습니다.
- 락을 얻는데 실패할경우 무한정 대기할수 있습니다.
- 여러개의 락을 사용할 경우 데드락 발생 가능성이 있습니다.

*/

public class SynchronizedExample {
    private int count = 0;

    public synchronized void increment() {
        count++;
    }

    public synchronized int getCount() {
        return count;
    }

    public static void main(String[] args) throws InterruptedException {
        SynchronizedExample example = new SynchronizedExample();

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



