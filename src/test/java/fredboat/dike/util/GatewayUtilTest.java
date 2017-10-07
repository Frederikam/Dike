/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class GatewayUtilTest {
    @Test
    void getGateway() {
        Assertions.assertNotNull(GatewayUtil.getGateway());
    }

}
