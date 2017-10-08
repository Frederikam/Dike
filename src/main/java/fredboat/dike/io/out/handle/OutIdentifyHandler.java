/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.io.out.handle;

import fredboat.dike.cache.Session;
import fredboat.dike.cache.SessionManager;
import fredboat.dike.cache.ShardIdentifier;
import fredboat.dike.io.out.LocalGateway;
import org.java_websocket.WebSocket;
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
    public void handle(WebSocket socket, String message) throws IOException {
        JSONObject json = new JSONObject(message);
        JSONObject d = json.getJSONObject("d");

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

        /* Make our own changes to the OP 2 */
        JSONObject properties = d.getJSONObject("properties");
        properties.put("$device", properties.get("$device") + " via Dike");
        properties.put("$browser", properties.get("$browser") + " via Dike");
        d.put("properties", properties);
        json.put("d", d);

        Session session = SessionManager.INSTANCE.createSession(identifier, localGateway, socket, json.toString());
        localGateway.setSession(session);
    }

}
