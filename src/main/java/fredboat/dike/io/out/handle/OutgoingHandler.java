/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.out.handle;

import fredboat.dike.io.out.LocalGateway;

import java.io.IOException;

public abstract class OutgoingHandler {

    final LocalGateway localGateway;

    OutgoingHandler(LocalGateway localGateway) {
        this.localGateway = localGateway;
    }

    public LocalGateway getLocalGateway() {
        return localGateway;
    }

    public abstract void handle(String message) throws IOException;
}
