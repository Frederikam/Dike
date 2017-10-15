/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.out.handle;

import fredboat.dike.io.out.LocalGateway;
import fredboat.dike.util.OpCodes;
import org.java_websocket.WebSocket;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OutResumeHandler extends OutgoingHandler {

    private static final Logger log = LoggerFactory.getLogger(OutResumeHandler.class);

    public OutResumeHandler(LocalGateway localGateway) {
        super(localGateway);
    }

    @Override
    public void handle(WebSocket socket, String message) throws IOException {
        log.warn("Received OP 6 but Dike does not support resuming. Responding with OP 9...");
        JSONObject op9 = new JSONObject();
        op9.put("op", OpCodes.OP_9_INVALIDATE_SESSION);
        op9.put("d", false); // Trigger an identify
        socket.send(op9.toString());
    }
}
