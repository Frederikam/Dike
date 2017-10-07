/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike;

import fredboat.dike.io.out.LocalGateway;

public class Launcher {

    public static void main(String[] args) {
        new LocalGateway().start();
    }

}
