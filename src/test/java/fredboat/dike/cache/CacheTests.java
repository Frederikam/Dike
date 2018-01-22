package fredboat.dike.cache;

import fredboat.dike.TestBeans;
import fredboat.dike.TestConfigImpl;
import fredboat.dike.session.SessionManager;
import fredboat.dike.session.cache.Cache;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = { TestConfigImpl.class, TestBeans.class })
public class CacheTests {

    private static final Logger log = LoggerFactory.getLogger(CacheTests.class);

    @Autowired
    private TestConfigImpl testConfig;
    private Cache cache;

    @BeforeEach
    void beforeEach() {

    }

    @AfterEach
    void afterEach() {
        SessionManager.INSTANCE.getSessions().forEach(SessionManager.INSTANCE::invalidate);
    }

    @Test
    void testGuildDelete() {

    }

}
