/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.LocalGateway;

public abstract class IncomingHandler {

    final LocalGateway localGateway;

    IncomingHandler(LocalGateway localGateway) {
        this.localGateway = localGateway;
    }

    public LocalGateway getLocalGateway() {
        return localGateway;
    }

    public abstract void handle(String message);
}
