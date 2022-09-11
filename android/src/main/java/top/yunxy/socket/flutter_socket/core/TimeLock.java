package top.yunxy.socket.flutter_socket.core;

public class TimeLock {
    private volatile boolean state = false;

    public void lock() {
        while (state) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        state = true;
    }

    public void lock(long mills) throws InterruptedException {
        long cur = System.currentTimeMillis();
        while (state) {
            if (System.currentTimeMillis() - cur > mills) {
                throw new InterruptedException("lock timeout");
            }
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        state = true;
    }

    public void unlock() {
        state = false;
    }
}
