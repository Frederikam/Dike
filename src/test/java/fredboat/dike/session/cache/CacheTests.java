package fredboat.dike.session.cache;

import com.jsoniter.any.Any;
import fredboat.dike.TestBeans;
import fredboat.dike.TestConfigImpl;
import fredboat.dike.session.Session;
import fredboat.dike.session.SessionManager;
import fredboat.dike.util.DikeSessionController;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.Guild;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TestConfigImpl.class, TestBeans.class, CacheTestEventListener.class,
        DikeSessionController.class})
public class CacheTests {

    private static final Logger log = LoggerFactory.getLogger(CacheTests.class);
    private static CacheTests ins;

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

    // Uses before each to avoid being static
    @BeforeEach
    void beforeEach() {
        ins = this;

        // Validate sessions
        Collection<Session> sessions = SessionManager.INSTANCE.getSessions();
        assertEquals(sessions.size(), 1);

        for (Session s : sessions) {
            session = s;
        }
        assertNotNull(session);
        cache = session.getCache();
        assertNotNull(cache);

        // Validate guilds
        jGuild = testBot.getGuildById(config.testGuild());
        assertNotNull(jGuild);
        dGuild = cache.getGuild(config.testGuild());
        assertNotNull(dGuild);
    }

    @Test
    void sameGuilds() {
        List<Long> jGuilds = new ArrayList<>();
        List<Long> dGuilds = new ArrayList<>();
        testBot.getGuilds().forEach((g) -> jGuilds.add(g.getIdLong()));
        cache.guilds.forEachValue((g) -> dGuilds.add(g.getId()));
        assetListsEquals(jGuilds, dGuilds);
    }

    @Test
    void sameMembers() {
        List<Long> jMembers = new ArrayList<>();
        List<Long> dMembers = new ArrayList<>();
        jGuild.getMembers().forEach((m) -> jMembers.add(m.getUser().getIdLong()));
        dGuild.members.forEach((key, value) -> dMembers.add(key));
        assetListsEquals(jMembers, dMembers);
    }

    @Test
    void sameChannels() {
        List<Long> jChannels = new ArrayList<>();
        List<Long> dChannels = new ArrayList<>();
        jGuild.getTextChannels().forEach((c) -> jChannels.add(c.getIdLong()));
        jGuild.getVoiceChannels().forEach((c) -> jChannels.add(c.getIdLong()));
        jGuild.getCategories().forEach((c) -> jChannels.add(c.getIdLong()));
        dGuild.channels.forEach((key, value) -> dChannels.add(key));
        assetListsEquals(jChannels, dChannels);
    }

    @Test
    void sameRoles() {
        List<Long> jRoles = new ArrayList<>();
        List<Long> dRoles = new ArrayList<>();
        jGuild.getRoles().forEach((r) -> jRoles.add(r.getIdLong()));
        dGuild.roles.forEach((key, value) -> dRoles.add(key));
        assetListsEquals(jRoles, dRoles);
    }

    @Test
    void sameEmotes() {
        List<Long> jEmote = new ArrayList<>();
        List<Long> dEmote = new ArrayList<>();
        jGuild.getEmotes().forEach((e) -> jEmote.add(e.getIdLong()));
        dGuild.emotes.forEach((key, value) -> dEmote.add(key));
        assetListsEquals(jEmote, dEmote);
    }

    @Test
    void matchingVoiceStates() {
        jGuild.getVoiceStates().forEach((s) -> {
            if (!s.inVoiceChannel()) return;
            Any dState = dGuild.voiceStates.get(s.getMember().getUser().getIdLong());
            assertNotNull(dState);
            assertEquals(s.getChannel().getIdLong(), dState.get("channel_id").mustBeValid().toLong());
            assertEquals(s.getMember().getUser().getIdLong(), dState.get("user_id").mustBeValid().toLong());
            assertEquals(s.getSessionId(), dState.get("session_id").mustBeValid().toString());
            assertEquals(s.isDeafened(), dState.get("deaf").mustBeValid().toBoolean());
            assertEquals(s.isMuted(), dState.get("mute").mustBeValid().toBoolean());
            assertEquals(s.isSelfDeafened(), dState.get("self_deaf").mustBeValid().toBoolean());
            assertEquals(s.isSelfMuted(), dState.get("self_mute").mustBeValid().toBoolean());
            assertEquals(s.isSuppressed(), dState.get("suppress").mustBeValid().toBoolean());
        });
    }

    @AfterAll
    static void afterAll() {
        ins.testBot.shutdown();
        ins.userBot.shutdown();
    }

    @SuppressWarnings("unchecked")
    private void assetListsEquals(List<? extends Comparable> a, List<? extends Comparable> b) {
        a.sort(Comparable::compareTo);
        b.sort(Comparable::compareTo);
        assertEquals(a, b);
    }

}
