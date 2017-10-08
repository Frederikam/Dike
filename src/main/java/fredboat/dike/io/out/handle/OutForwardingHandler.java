/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.out.handle;

import fredboat.dike.io.out.LocalGateway;
import org.java_websocket.WebSocket;

public class OutForwardingHandler extends OutgoingHandler {
    public OutForwardingHandler(LocalGateway localGateway) {
        super(localGateway);
    }

    @Override
    public void handle(WebSocket socket, String message) {
        localGateway.forward(message);
    }
}
