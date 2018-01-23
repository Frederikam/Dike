package fredboat.dike.session.cache;

import fredboat.dike.TestBeans;
import fredboat.dike.TestConfigImpl;
import fredboat.dike.session.Session;
import fredboat.dike.session.SessionManager;
import fredboat.dike.util.DikeSessionController;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runners.MethodSorters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfigImpl.class, TestBeans.class, CacheTestEventListener.class,
        DikeSessionController.class})
public class CacheTests {

    private static final Logger log = LoggerFactory.getLogger(CacheTests.class);

    @Autowired
    private TestConfigImpl config;
    @Autowired
    @Qualifier("testBot")
    private JDA testBot;
    @Autowired
    @Qualifier("userBot")
    private JDA userBot;
    private static Guild jGuild = null;
    private static fredboat.dike.session.cache.Guild dGuild = null;
    private static Cache cache;
    private static Session session = null;

    @Test
    void sessionExists() {
        Collection<Session> sessions = SessionManager.INSTANCE.getSessions();
        Assert.assertEquals(sessions.size(), 1);

        for (Session s : sessions) {
            session = s;
        }
        Assert.assertNotNull(session);
        cache = session.getCache();
        Assert.assertNotNull(cache);
    }

    @Test
    void guildExists() {
        jGuild = testBot.getGuildById(config.testGuild());
        Assert.assertNotNull(jGuild);
        dGuild = cache.getGuild(config.testGuild());
        Assert.assertNotNull(dGuild);
    }

    @Test
    void aSameGuilds() {
        List<Long> jMembers = new ArrayList<>();
        List<Long> dMembers = new ArrayList<>();
        jGuild.getMembers().forEach((m) -> jMembers.add(m.getUser().getIdLong()));
        dGuild.members.forEach((key, value) -> dMembers.add(key));
        jMembers.sort(Long::compareTo);
        dMembers.sort(Long::compareTo);
        Assert.assertEquals(jMembers, dMembers);
    }

}
