package fredboat.dike.util;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

@ThreadSafe
public class Ratelimit {

    private volatile Instant resetTime;
    private volatile int remaining = 0;
    private volatile int limit;

    private final Function<Ratelimit, Instant> resetCallback;
    private final Consumer<Ratelimit> ratelimitedCallback;

    /**
     * @param limit               the limit of this Ratelimit
     * @param resetCallback       Function invoked upon resetting the remaining limit and upon construction. Should return the new reset time
     * @param ratelimitedCallback Consumer that is invoked upon getting ratelimited
     */
    public Ratelimit(int limit, Function<Ratelimit, Instant> resetCallback, Consumer<Ratelimit> ratelimitedCallback) {
        this.limit = limit;
        this.remaining = limit;
        this.resetCallback = resetCallback;
        this.ratelimitedCallback = ratelimitedCallback;

        resetTime = resetCallback.apply(this);
    }

    /**
     * Either counts down the remaining limit for the current interval
     * <br>If the remaining limit is 0 this method will instead wait for the limit to reset
     *
     * @throws InterruptedException if interrupted
     */
    public synchronized void acquire() throws InterruptedException {
        checkReset();

        if (remaining <= 0) {
            ratelimitedCallback.accept(this);
            synchronized (this) {
                long toWait = resetTime.minusMillis(Instant.now().toEpochMilli()).toEpochMilli();
                toWait = Math.max(toWait, 0);
                wait(toWait);
                acquire(); // Try again
            }
        } else {
            remaining--; // Good to go!
        }
    }

    private synchronized void checkReset() {
        if (resetTime.isBefore(Instant.now())) {
            resetTime = resetCallback.apply(this);
            remaining = limit;
        }
    }

    public Instant getResetTime() {
        checkReset();
        return resetTime;
    }

    public int getRemaining() {
        checkReset();
        return remaining;
    }

    public boolean canAcquire() {
        checkReset();
        return remaining > 0;
    }

    public int getLimit() {
        return limit;
    }

    public void setResetTime(Instant resetTime) {
        this.resetTime = resetTime;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public void setRemaining(int remaining) {
        this.remaining = remaining;
    }
}
