package fredboat.dike.session.cache;

import com.jsoniter.any.Any;
import com.jsoniter.output.JsonStream;

import java.util.HashMap;
import java.util.Map;

public class Dispatch {

    private final String t;
    private final Map<String, Any> d;

    Dispatch(String t, Map<String, Any> d) {
        this.t = t;
        this.d = d;
    }

    public String wrap(long sequence) {
        Map<String, Object> map = new HashMap<>();

        map.put("op", 0);
        map.put("d", d);
        map.put("t", t);
        map.put("s", sequence);

        return JsonStream.serialize(map);
    }
}
