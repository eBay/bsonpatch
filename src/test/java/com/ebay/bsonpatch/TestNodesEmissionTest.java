package com.ebay.bsonpatch;

import java.io.IOException;
import java.util.EnumSet;

import static org.junit.Assert.*;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.junit.Test;

public class TestNodesEmissionTest {

    private static EnumSet<DiffFlags> flags;

    static {
        flags = DiffFlags.defaults();
        flags.add(DiffFlags.EMIT_TEST_OPERATIONS);
    }

    @Test
    public void testNodeEmittedBeforeReplaceOperation() throws IOException {
        BsonValue source = BsonDocument.parse("{\"key\":\"original\"}");
        BsonValue target = BsonDocument.parse("{\"key\":\"replaced\"}");

        BsonArray diff = BsonDiff.asBson(source, target, flags);

        BsonValue testNode = BsonDocument.parse("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeCopyOperation() throws IOException {
        BsonValue source = BsonDocument.parse("{\"key\":\"original\"}");
        BsonValue target = BsonDocument.parse("{\"key\":\"original\", \"copied\":\"original\"}");

        BsonArray diff = BsonDiff.asBson(source, target, flags);

        BsonValue testNode = BsonDocument.parse("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeMoveOperation() throws IOException {
        BsonValue source = BsonDocument.parse("{\"key\":\"original\"}");
        BsonValue target = BsonDocument.parse("{\"moved\":\"original\"}");

        BsonArray diff = BsonDiff.asBson(source, target, flags);

        BsonValue testNode = BsonDocument.parse("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveOperation() throws IOException {
        BsonValue source = BsonDocument.parse("{\"key\":\"original\"}");
        BsonValue target = BsonDocument.parse("{}");

        BsonArray diff = BsonDiff.asBson(source, target, flags);

        BsonValue testNode = BsonDocument.parse("{\"op\":\"test\",\"path\":\"/key\",\"value\":\"original\"}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveFromMiddleOfArray() throws IOException {
        BsonValue source = BsonDocument.parse("{\"key\":[1,2,3]}");
        BsonValue target = BsonDocument.parse("{\"key\":[1,3]}");

        BsonArray diff = BsonDiff.asBson(source, target, flags);

        BsonValue testNode = BsonDocument.parse("{\"op\":\"test\",\"path\":\"/key/1\",\"value\":2}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }

    @Test
    public void testNodeEmittedBeforeRemoveFromTailOfArray() throws IOException {
        BsonValue source = BsonDocument.parse("{\"key\":[1,2,3]}");
        BsonValue target = BsonDocument.parse("{\"key\":[1,2]}");

        BsonArray diff = BsonDiff.asBson(source, target, flags);

        BsonValue testNode = BsonDocument.parse("{\"op\":\"test\",\"path\":\"/key/2\",\"value\":3}");
        assertEquals(2, diff.size());
        assertEquals(testNode, diff.iterator().next());
    }
}