/*
 * MIT License
 *
 * Copyright (c) 2017 Frederik Ar. Mikkelsen
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
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
