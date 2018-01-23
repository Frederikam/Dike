package fredboat.dike.session.cache;

import fredboat.dike.TestConfigImpl;
import net.dv8tion.jda.core.events.Event;
import net.dv8tion.jda.core.hooks.EventListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;

@Service
public class CacheTestEventListener implements EventListener {

    @Autowired
    private TestConfigImpl config;
    private final HashMap<Class<? extends Event>, Latch> latches = new HashMap<>();

    @Override
    public void onEvent(Event event) {
        synchronized (latches) {
            Latch latch = latches.computeIfAbsent(event.getClass(), (__) -> new Latch());
            latch.event = event;
        }
    }

    public Latch getLatch(Class<? extends Event> clz) {
        synchronized (latches) {
            return latches.computeIfAbsent(clz, (__) -> new Latch());
        }
    }

    public class Latch {
        public volatile Event event;
        public final CountDownLatch latch = new CountDownLatch(1);
    }

}
