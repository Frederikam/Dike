/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.notation;

import fredboat.dike.util.JsonHandler;
import org.junit.jupiter.api.Assertions;

class JsonHandlerTest {

    private JsonHandler handler = new JsonHandler();

    @org.junit.jupiter.api.Test
    void getOp() {
        Assertions.assertEquals(handler.getOp("{\"test\":\"test2\",\"foo\":{\"op\":5},\"op\":3}"), 3);
        Assertions.assertEquals(handler.getOp("{}"), -1);
    }

}
