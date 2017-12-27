package fredboat.dike.io.out.handle;

import fredboat.dike.io.out.LocalGateway;
import org.java_websocket.WebSocket;
import org.json.JSONObject;

public class OutHeartbeatHandler extends OutgoingHandler {

    public OutHeartbeatHandler(LocalGateway localGateway) {
        super(localGateway);
    }

    @Override
    public void handle(WebSocket socket, String message) {
        //noinspection ConstantConditions this should not happen
        localGateway.getContext(socket).onHeartbeat(new JSONObject(message).getInt("d"));

        JSONObject ack = new JSONObject()
                .put("op", 11)
                .put("d", JSONObject.NULL)
                .put("t", JSONObject.NULL)
                .put("s", JSONObject.NULL);
        socket.send(ack.toString());
    }
}
