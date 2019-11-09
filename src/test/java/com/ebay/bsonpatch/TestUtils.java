package com.ebay.bsonpatch;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bson.BsonDocument;
import org.bson.BsonValue;

public class TestUtils {

    private TestUtils() {
    }

    public static BsonValue loadResourceAsBsonValue(String path) throws IOException {
        String testData = loadFromResources(path);
        return BsonDocument.parse(testData);
    }

    public static String loadFromResources(String path) throws IOException {
        InputStream resourceAsStream = PatchTestCase.class.getResourceAsStream(path);
        return IOUtils.toString(resourceAsStream, "UTF-8");
    }
}