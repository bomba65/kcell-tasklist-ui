package kz.kcell.kwms.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;

/**
 * Keeps json value as json, does not try to deserialize it
 * @author roytruelove
 *
 */
public class JsonAsStringDeserializer extends JsonDeserializer<String> {

    @Override
    public String deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        TreeNode tree = jp.getCodec().readTree(jp);
        return tree.toString();
    }
}
