package fredboat.dike.session.cache;

import com.jsoniter.any.Any;

import java.util.Map;

public class Dispatch {

    private final String t;
    private final Map<String, Any> d;

    Dispatch(String t, Map<String, Any> d) {
        this.t = t;
        this.d = d;
    }
}
