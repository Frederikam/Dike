/*
 * Copyright (c) 2017 Frederik Mikkelsen.
 * All rights reserved.
 */

package fredboat.dike.notation;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JsonHandler implements INotationHandler {

    private static final Logger log = LoggerFactory.getLogger(JsonHandler.class);
    private final JsonFactory factory;

    public JsonHandler() {
        factory = new JsonFactory();
    }

    @Override
    public int getOp(String message) {
        try {
            // We should never find the op in an inner object or array
            //   so we will make sure we are at the right depth.
            // Starts as -1, as we begin with a struct begin
            int depth = -1;
            JsonParser parser = factory.createParser(message);

            while (parser.nextToken() != null) {
                if (parser.getCurrentToken().isStructStart()) depth++;
                else if (parser.getCurrentToken().isStructEnd()) depth--;

                if (parser.getCurrentName() != null
                        && parser.getCurrentName().equals("op")
                        && depth == 0) {
                    parser.nextToken(); // Move from the "op" field to the field's value
                    return parser.getIntValue();
                }
            }

            return -1;

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
