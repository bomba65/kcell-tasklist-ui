package kz.kcell.kwms.model;

import com.fasterxml.jackson.annotation.JsonRawValue;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import kz.kcell.kwms.jackson.JsonAsStringDeserializer;
import lombok.*;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class JsonRawValueDeserializationTest {

/*    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    static class Pojo {
        @JsonRawValue
        @JsonDeserialize(using = JsonAsStringDeserializer.class)
        String json;

    }

    ObjectMapper mapper = new ObjectMapper();

    @Test
    public void testEntity() throws Exception {
        EquipmentInstance equipmentInstance = EquipmentInstance.builder()
                .params("{\"Hello\": \"World\"}")
                .definition(new EquipmentDefinition())
                .build();

        String jsonString = mapper.writeValueAsString(equipmentInstance);

        System.out.println(jsonString);

        JSONAssert.assertEquals(
                "{\n" +
                        "  \"id\":null,\n" +
                        "  \"sn\":null,\n" +
                        "  \"definition\":{\n" +
                        "    \"id\":null,\n" +
                        "    \"name\":null,\n" +
                        "    \"schema\":{},\n" +
                        "    \"version\":0\n" +
                        "  },\n" +
                        "  \"params\":{\n" +
                        "    \"Hello\": \"World\"\n" +
                        "  },\n" +
                        "  \"version\":0,\n" +
                        "  \"connections\":null\n" +
                        "}",
                jsonString,
                true);

        EquipmentInstance instance = mapper.readValue(jsonString, EquipmentInstance.class);

        JSONAssert.assertEquals("{\"Hello\": \"World\"}", instance.params, true);
    }

    @Test
    public void testPojo() throws IOException {
        Pojo pojo = new Pojo();
        pojo.setJson("{\"foo\":18}");

        String output = mapper.writeValueAsString(pojo);
        assertEquals("{\"json\":{\"foo\":18}}", output);

        Pojo deserialized = mapper.readValue(output, Pojo.class);
        assertEquals("{\"foo\":18}", deserialized.getJson());
    }*/
}
