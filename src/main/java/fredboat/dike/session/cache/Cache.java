/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.session.cache;

import com.jsoniter.any.Any;
import gnu.trove.map.hash.THashMap;

import javax.annotation.Nullable;
import java.io.IOException;

public class Cache {

    private static THashMap<Long, Guild> guilds = new THashMap<>();

    public void createGuild(Any d) throws IOException {
        Long id = d.get("id").toLong();
        guilds.put(id, new Guild(d));
    }

    public void deleteGuild(Any d) {
        Long id = d.get("d").get("id").toLong();
        if(guilds.remove(id) == null) {
            throw new RuntimeException("Attempt to delete guild " + id + " but it doesn't exist!");
        }
    }

    @Nullable
    public Guild getGuild(long id) {
        return guilds.get(id);
    }

    /**
     * Invoked when we identify
     */
    public void invalidate() {
        guilds.clear();
    }

}
