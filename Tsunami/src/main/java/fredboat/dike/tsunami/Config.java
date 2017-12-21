package fredboat.dike.tsunami;

public class Config {

    private final int shardCount;
    private final String token;

    Config() {
        String foo = System.getProperty("shardCount");
        shardCount = foo != null ? Integer.parseInt(foo) : 1;

        token = System.getProperty("token");
    }

    public int getShardCount() {
        return shardCount;
    }

    public String getToken() {
        return token;
    }
}
