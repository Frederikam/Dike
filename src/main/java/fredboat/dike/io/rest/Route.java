package fredboat.dike.io.rest;

public enum Route {
    USER_AT_ME("users/@me");

    private final String route;

    Route(String route) {
        this.route = route;
    }

    public String getRoute() {
        return route;
    }

    @Override
    public String toString() {
        return route;
    }
}
