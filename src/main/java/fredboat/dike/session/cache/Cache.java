package fredboat.dike.session.cache;

import com.jsoniter.any.Any;
import gnu.trove.map.hash.THashMap;

import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.NotThreadSafe;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@NotThreadSafe
public class Cache {

    private THashMap<Long, Guild> guilds = new THashMap<>();
    private int largeThreshold = 0;
    private Map<String, Any> readyPayload = null;

    @GuardedBy("this")
    public void createGuild(Any d) {
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
    public List<Dispatch> computeDispatches() {
        List<Dispatch> list = new LinkedList<>();

        /* READY */
        LinkedList<HashMap<String, Object>> guildList = new LinkedList<>();

        for (Guild guild : guilds.values()) {
            HashMap<String, Object> g = new HashMap<>();
            g.put("unavailable", Boolean.FALSE);
            g.put("id", guild.getId());
            guildList.add(g);
        }

        HashMap<String, Any> ready = new HashMap<>(readyPayload);
        ready.put("guilds", Any.wrap(guildList));
        // TODO: Self info
        list.add(new Dispatch("READY", ready));

        /* GUILD_CREATE */
        for (Guild guild : guilds.values())
            list.add(guild.computeDispatch());

        return list;
    }

    @GuardedBy("this")
    public void handleReadyEvent(Any readyEvent) {
        readyPayload = readyEvent.get("d").asMap();
        readyPayload.remove("user"); // Fetched via rest as needed if we need to provide dispatches
        //TODO readyPayload.remove("guilds"); // Fetched from the cache
    }

    public int getLargeThreshold() {
        return largeThreshold;
    }

    public void setLargeThreshold(int largeThreshold) {
        this.largeThreshold = largeThreshold;
    }
}
