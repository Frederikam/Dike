/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.out.handle;

import fredboat.dike.io.out.LocalGateway;

public class OutForwardingHandler extends OutgoingHandler {
    public OutForwardingHandler(LocalGateway localGateway) {
        super(localGateway);
    }

    @Override
    public void handle(String message) {
        localGateway.forward(message);
    }
}
