/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.out.handle;

import fredboat.dike.io.out.LocalGateway;
import org.java_websocket.WebSocket;

import java.io.IOException;

public abstract class OutgoingHandler {

    final LocalGateway localGateway;

    OutgoingHandler(LocalGateway localGateway) {
        this.localGateway = localGateway;
    }

    public LocalGateway getLocalGateway() {
        return localGateway;
    }

    public abstract void handle(WebSocket socket, String message) throws IOException;
}
