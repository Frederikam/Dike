/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.notation;

public interface INotationHandler {

    /**
     * @param message the object notation message
     * @return the {@code object.op} value, or {@code -1} if not found
     */
    int getOp(String message);

}
