package fredboat.dike.util;

import javax.annotation.concurrent.ThreadSafe;
import java.time.Instant;
import java.util.function.Consumer;
import java.util.function.Function;

@ThreadSafe
public class Ratelimit {

    private Instant resetTime = Instant.EPOCH;
    private int remaining = 0;
    private int limit;

    private final Function<Ratelimit, Instant> resetCallback;
    private final Consumer<Ratelimit> ratelimitedCallback;

    /**
     * @param limit               the limit of this Ratelimit
     * @param resetCallback       Function invoked upon resetting the remaining limit and upon construction. Should return the new reset time
     * @param ratelimitedCallback Consumer that is invoked upon getting ratelimited
     */
    public Ratelimit(int limit, Function<Ratelimit, Instant> resetCallback, Consumer<Ratelimit> ratelimitedCallback) {
        this.limit = limit;
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
            Thread.sleep(resetTime.minusMillis(System.currentTimeMillis()).toEpochMilli());
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

    public int getLimit() {
        return limit;
    }

    public void setResetTime(Instant resetTime) {
        this.resetTime = resetTime;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }
}
