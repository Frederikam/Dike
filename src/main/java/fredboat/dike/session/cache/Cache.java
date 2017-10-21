/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.session.cache;

import com.jsoniter.JsonIterator;
import gnu.trove.map.hash.THashMap;

import java.io.IOException;

public class Cache {

    private static THashMap<Long, Guild> guilds = new THashMap<>();

    private void createGuild(String json) throws IOException {
        Long id = JsonIterator.deserialize(json).get("id").as(Long.class);
        guilds.put(id, new Guild(json));
    }

    private void deleteGuild(String json) {
        Long id = JsonIterator.deserialize(json).get("id").as(Long.class);
        if(guilds.remove(id) == null) {
            throw new RuntimeException("Attempt to delete guild " + id + " but it doesn't exist!");
        }
    }

    /**
     * Invoked when we identify
     */
    public void invalidate() {
        guilds.clear();
    }

}
