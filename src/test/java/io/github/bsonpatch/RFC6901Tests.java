package io.github.bsonpatch;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;


public class RFC6901Tests {
    @Test
    public void testRFC6901Compliance() throws IOException {
    	BsonValue data = TestUtils.loadResourceAsBsonValue("/rfc6901/data.json");
        BsonValue testData = data.asDocument().get("testData");

        BsonValue emptyJson = BsonDocument.parse("{}");
        BsonArray patch = BsonDiff.asBson(emptyJson, testData);
        BsonValue result = BsonPatch.apply(patch, emptyJson);
        assertEquals(testData, result);
    }
}