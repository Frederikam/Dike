package fredboat.dike.cache;

import fredboat.dike.TestBeans;
import fredboat.dike.TestConfigImpl;
import fredboat.dike.session.Session;
import fredboat.dike.session.SessionManager;
import fredboat.dike.session.cache.Cache;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Collection;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfigImpl.class, TestBeans.class, CacheTestEventListener.class})
public class CacheTests {

    private static final Logger log = LoggerFactory.getLogger(CacheTests.class);

    @Autowired
    private TestConfigImpl config;
    private Cache cache;
    private Session session = null;
    @Autowired
    @Qualifier("testBot")
    private JDA testBot;
    @Autowired
    @Qualifier("userBot")
    private JDA userBot;
    private Guild jGuild = null;
    private fredboat.dike.session.cache.Guild dGuild = null;

    @Test
    void sessionExists() {
        Collection<Session> sessions = SessionManager.INSTANCE.getSessions();
        Assert.assertEquals(sessions.size(), 1);

        for (Session s : sessions) {session = s;}
        Assert.assertNotNull(session);
        cache = session.getCache();
    }

    @Test
    void  guildExists() {
        jGuild = testBot.getGuildById(config.testGuild());
        Assert.assertNotNull(jGuild);
        dGuild = cache.getGuild(config.testGuild());
        Assert.assertNotNull(dGuild);
    }

}
