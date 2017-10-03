/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.cache;

public class ShardIdentifier {

    private final long user;
    private final int shardId;
    private final int shardCount;

    public ShardIdentifier(long user, int shardId, int shardCount) {
        this.user = user;
        this.shardId = shardId;
        this.shardCount = shardCount;
    }

    public long getUser() {
        return user;
    }

    public int getShardId() {
        return shardId;
    }

    public int getShardCount() {
        return shardCount;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ShardIdentifier that = (ShardIdentifier) o;

        if (user != that.user) return false;
        //noinspection SimplifiableIfStatement
        if (shardId != that.shardId) return false;
        return shardCount == that.shardCount;
    }

    @Override
    public int hashCode() {
        int result = (int) (user ^ (user >>> 32));
        result = 31 * result + shardId;
        result = 31 * result + shardCount;
        return result;
    }
}
