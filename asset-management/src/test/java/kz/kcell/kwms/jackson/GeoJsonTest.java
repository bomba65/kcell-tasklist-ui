package kz.kcell.kwms.jackson;

import com.bedatadriven.jackson.datatype.jts.JtsModule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import lombok.Data;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;

public class GeoJsonTest {
    GeometryFactory gf = new GeometryFactory();
    ObjectMapper mapper = new ObjectMapper().registerModule(new JtsModule());

    static @Data class Dummy {
        Point location;
    }

    @Test
    public void testSerialization() throws Exception {
        Dummy dummy = new Dummy();
        dummy.location = gf.createPoint(new Coordinate(50.0, 60.0));
        String jsonString = mapper.writeValueAsString(dummy);
        JSONAssert.assertEquals(
                "{\"location\": {\"type\": \"Point\", \"coordinates\": [50.0, 60.0]}}",
                jsonString,
                true);
    }

    @Test
    public void testDeserialization() throws IOException {
        String jsonString = "{\"location\": {\"type\": \"Point\", \"coordinates\": [30.0, 40.0]}}";
        Dummy dummyExpected = new Dummy();
        dummyExpected.location = gf.createPoint(new Coordinate(30.0, 40.0));

        Dummy dummyActual = mapper.readValue(jsonString, Dummy.class);

        Assert.assertEquals(dummyExpected, dummyActual);
    }
}
