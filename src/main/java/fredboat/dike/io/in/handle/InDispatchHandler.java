/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.in.handle;

import com.jsoniter.JsonIterator;
import fredboat.dike.io.in.DiscordGateway;

import java.io.IOException;

public class InDispatchHandler extends IncomingHandler {

    public InDispatchHandler(DiscordGateway discordGateway) {
        super(discordGateway);
    }

    private int sequence = -1;

    @Override
    public void handle(String message) throws IOException {
        JsonIterator iter = JsonIterator.parse(message);

        String type = null;

        for (String field = iter.readObject(); field != null; field = iter.readObject()) {
            switch (field) {
                case "s":
                    sequence = iter.readInt();
                    break;
                case "t":
                    type = iter.readString();
                    break;
            }
        }

        //TODO: Cache
        assert type != null;
        switch (type) {
            case "READY":
                discordGateway.setState(DiscordGateway.State.CONNECTED);
                break;
            case "RESUMED":
                discordGateway.setState(DiscordGateway.State.CONNECTED);
                break;
        }

        discordGateway.forward(message);
    }

    public int getSequence() {
        return sequence;
    }
}
