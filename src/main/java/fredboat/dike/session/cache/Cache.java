/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.session.cache;

import com.jsoniter.any.Any;
import gnu.trove.map.hash.THashMap;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

@NotThreadSafe
public class Cache {

    private static THashMap<Long, Guild> guilds = new THashMap<>();
    private int largeThreshold = 0;

    @GuardedBy("this")
    public void createGuild(Any d) throws IOException {
        Long id = d.get("id").toLong();
        guilds.put(id, new Guild(this, d));
    }

    @GuardedBy("this")
    public void deleteGuild(Any d) {
        Long id = d.get("d").get("id").toLong();
        if (guilds.remove(id) == null) {
            throw new RuntimeException("Attempted to delete guild " + id + " but it doesn't exist!");
        }
    }

    @Nullable
    public Guild getGuild(long id) {
        return guilds.get(id);
    }

    /**
     * Invoked when we identify
     */
    @GuardedBy("this")
    public void invalidate() {
        guilds.clear();
    }

    @GuardedBy("this")
    public List<Dispatch> provideDispatches() {
        List<Dispatch> list = new LinkedList<>();

        for (Guild guild : guilds.values())
            list.addAll(guild.computeDispatches());

        return list;
    }

    public int getLargeThreshold() {
        return largeThreshold;
    }

    public void setLargeThreshold(int largeThreshold) {
        this.largeThreshold = largeThreshold;
    }
}
