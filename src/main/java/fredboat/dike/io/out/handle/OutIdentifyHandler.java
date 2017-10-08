/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.out.handle;

import fredboat.dike.cache.Session;
import fredboat.dike.cache.SessionManager;
import fredboat.dike.cache.ShardIdentifier;
import fredboat.dike.io.out.LocalGateway;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class OutIdentifyHandler extends OutgoingHandler {

    private static final Logger log = LoggerFactory.getLogger(OutIdentifyHandler.class);

    public OutIdentifyHandler(LocalGateway localGateway) {
        super(localGateway);
    }

    @Override
    public void handle(String message) throws IOException {
        JSONObject d = new JSONObject(message).getJSONObject("d");

        ShardIdentifier identifier;
        if (d.has("shard")) {
            identifier = ShardIdentifier.getFromToken(
                    d.getString("token"),
                    d.getJSONArray("shard").getInt(0),
                    d.getJSONArray("shard").getInt(1)
            );
        } else {
            identifier = ShardIdentifier.getFromToken(
                    d.getString("token"),
                    0,
                    1
            );
        }

        Session session = SessionManager.INSTANCE.createSession(identifier, localGateway, message);
        localGateway.setSession(session);
    }

}
