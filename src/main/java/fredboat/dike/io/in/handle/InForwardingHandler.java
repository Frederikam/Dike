/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import fredboat.dike.io.in.LocalGateway;

public class InForwardingHandler extends IncomingHandler {
    public InForwardingHandler(LocalGateway localGateway) {
        super(localGateway);
    }

    @Override
    public void handle(String message) {
        localGateway.forward(message);
    }
}
