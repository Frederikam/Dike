package fredboat.dike;

import java.util.List;

public interface Config {

    String host();
    int port();
    List<String> whitelist();

}
