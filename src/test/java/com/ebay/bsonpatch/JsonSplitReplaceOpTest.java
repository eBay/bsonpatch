package com.ebay.bsonpatch;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.junit.Test;

import java.util.EnumSet;

import static org.junit.Assert.assertEquals;

/**
 * @author isopropylcyanide
 */
public class JsonSplitReplaceOpTest {


    @Test
    public void testJsonDiffSplitsReplaceIntoAddAndRemoveOperationWhenFlagIsAdded() {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\", \"F4\" ] }";
        BsonDocument sourceNode = BsonDocument.parse(source);
        BsonDocument targetNode = BsonDocument.parse(target);

        BsonArray diff = BsonDiff.asBson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(3, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), diff.get(0).asDocument().getString("op").getValue());
        assertEquals("/ids/1", diff.get(0).asDocument().getString("path").getValue());
        assertEquals("F3", diff.get(0).asDocument().getString("value").getValue());

        assertEquals(Operation.ADD.rfcName(), diff.get(1).asDocument().getString("op").getValue());
        assertEquals("/ids/1", diff.get(1).asDocument().getString("path").getValue());
        assertEquals("F6", diff.get(1).asDocument().getString("value").getValue());

        assertEquals(Operation.ADD.rfcName(), diff.get(2).asDocument().getString("op").getValue());
        assertEquals("/ids/2", diff.get(2).asDocument().getString("path").getValue());
        assertEquals("F4", diff.get(2).asDocument().getString("value").getValue());
    }

    @Test
    public void testJsonDiffDoesNotSplitReplaceIntoAddAndRemoveOperationWhenFlagIsNotAdded() {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\", \"F4\" ] }";
        BsonDocument sourceNode = BsonDocument.parse(source);
        BsonDocument targetNode = BsonDocument.parse(target);

        BsonArray diff = BsonDiff.asBson(sourceNode, targetNode);
        System.out.println(diff);
        assertEquals(2, diff.size());
        assertEquals(Operation.REPLACE.rfcName(), diff.get(0).asDocument().getString("op").getValue());
        assertEquals("/ids/1", diff.get(0).asDocument().getString("path").getValue());
        assertEquals("F6", diff.get(0).asDocument().getString("value").getValue());

        assertEquals(Operation.ADD.rfcName(), diff.get(1).asDocument().getString("op").getValue());
        assertEquals("/ids/2", diff.get(1).asDocument().getString("path").getValue());
        assertEquals("F4", diff.get(1).asDocument().getString("value").getValue());
    }

    @Test
    public void testJsonDiffDoesNotSplitsWhenThereIsNoReplaceOperationButOnlyRemove() {
        String source = "{ \"ids\": [ \"F1\", \"F3\" ] }";
        String target = "{ \"ids\": [ \"F3\"] }";

        BsonDocument sourceNode = BsonDocument.parse(source);
        BsonDocument targetNode = BsonDocument.parse(target);

        BsonArray diff = BsonDiff.asBson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(1, diff.size());
        assertEquals(Operation.REMOVE.rfcName(), diff.get(0).asDocument().getString("op").getValue());
        assertEquals("/ids/0", diff.get(0).asDocument().getString("path").getValue());
        assertEquals("F1", diff.get(0).asDocument().getString("value").getValue());
    }

    @Test
    public void testJsonDiffDoesNotSplitsWhenThereIsNoReplaceOperationButOnlyAdd() {
        String source = "{ \"ids\": [ \"F1\" ] }";
        String target = "{ \"ids\": [ \"F1\", \"F6\"] }";

        BsonDocument sourceNode = BsonDocument.parse(source);
        BsonDocument targetNode = BsonDocument.parse(target);

        BsonArray diff = BsonDiff.asBson(sourceNode, targetNode, EnumSet.of(
                DiffFlags.ADD_EXPLICIT_REMOVE_ADD_ON_REPLACE
        ));
        assertEquals(1, diff.size());
        assertEquals(Operation.ADD.rfcName(), diff.get(0).asDocument().getString("op").getValue());
        assertEquals("/ids/1", diff.get(0).asDocument().getString("path").getValue());
        assertEquals("F6", diff.get(0).asDocument().getString("value").getValue());
    }
}