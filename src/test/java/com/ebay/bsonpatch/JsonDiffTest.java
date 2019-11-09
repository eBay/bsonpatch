/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *  
 *   http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.ebay.bsonpatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsonDiffTest {
    private static BsonArray jsonNode;

    @BeforeClass
    public static void beforeClass() throws IOException {
        String path = "/testdata/sample.json";
        InputStream resourceAsStream = JsonDiffTest.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        jsonNode = BsonArray.parse(testData);
    }

    @Test
    public void testSampleJsonDiff() {
        for (int i = 0; i < jsonNode.size(); i++) {
            BsonValue first = jsonNode.get(i).asDocument().get("first");
            BsonValue second = jsonNode.get(i).asDocument().get("second");
            BsonArray actualPatch = BsonDiff.asBson(first, second);
            BsonValue secondPrime = BsonPatch.apply(actualPatch, first);
            Assert.assertEquals("BSON Patch not symmetrical [index=" + i + ", first=" + first + "]", second, secondPrime);

        }
    }

    @Test
    public void testGeneratedJsonDiff() {
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            BsonArray first = TestDataGenerator.generate(random.nextInt(10));
            BsonArray second = TestDataGenerator.generate(random.nextInt(10));
            BsonArray actualPatch = BsonDiff.asBson(first, second);
            BsonArray secondPrime = BsonPatch.apply(actualPatch, first).asArray();
            Assert.assertEquals(second,secondPrime);
        }
    }

    @Test
    public void testRenderedRemoveOperationOmitsValueByDefault() {
        BsonDocument source = new BsonDocument();
        BsonDocument target = new BsonDocument();
        source.put("field", new BsonString("value"));

        BsonArray diff = BsonDiff.asBson(source, target);

        Assert.assertEquals(Operation.REMOVE.rfcName(), diff.get(0).asDocument().getString("op").getValue());
        Assert.assertEquals("/field", diff.get(0).asDocument().getString("path").getValue());
        Assert.assertNull(diff.get(0).asDocument().get("value"));
    }

    @Test
    public void testRenderedRemoveOperationRetainsValueIfOmitDiffFlagNotSet() {
        BsonDocument source = new BsonDocument();
        BsonDocument target = new BsonDocument();
        source.put("field", new BsonString("value"));

        EnumSet<DiffFlags> flags = DiffFlags.defaults().clone();
        Assert.assertTrue("Expected OMIT_VALUE_ON_REMOVE by default", flags.remove(DiffFlags.OMIT_VALUE_ON_REMOVE));
        BsonArray diff = BsonDiff.asBson(source, target, flags);

        Assert.assertEquals(Operation.REMOVE.rfcName(), diff.get(0).asDocument().getString("op").getValue());
        Assert.assertEquals("/field", diff.get(0).asDocument().getString("path").getValue());
        Assert.assertEquals("value", diff.get(0).asDocument().getString("value").getValue());
    }

    @Test
    public void testRenderedOperationsExceptMoveAndCopy() throws Exception {
    	BsonDocument source = new BsonDocument();
    	source.put("age", new BsonInt32(10));
    	BsonDocument target = new BsonDocument();
    	target.put("height", new BsonInt32(10));

        EnumSet<DiffFlags> flags = DiffFlags.dontNormalizeOpIntoMoveAndCopy().clone(); //only have ADD, REMOVE, REPLACE, Don't normalize operations into MOVE & COPY

        BsonArray diff = BsonDiff.asBson(source, target, flags);

        for (BsonValue d : diff) {
            Assert.assertNotEquals(Operation.MOVE.rfcName(), d.asDocument().getString("op").getValue());
            Assert.assertNotEquals(Operation.COPY.rfcName(), d.asDocument().getString("op").getValue());
        }

        BsonValue targetPrime = BsonPatch.apply(diff, source);
        Assert.assertEquals(target, targetPrime);


    }
    
    @Test
    public void testPath() throws Exception {
        BsonValue source = BsonDocument.parse("{\"profiles\":{\"abc\":[],\"def\":[{\"hello\":\"world\"}]}}");
        BsonArray patch = BsonArray.parse("[{\"op\":\"copy\",\"from\":\"/profiles/def/0\", \"path\":\"/profiles/def/0\"},{\"op\":\"replace\",\"path\":\"/profiles/def/0/hello\",\"value\":\"world2\"}]");

        BsonValue target = BsonPatch.apply(patch, source);
        BsonValue expected = BsonDocument.parse("{\"profiles\":{\"abc\":[],\"def\":[{\"hello\":\"world2\"},{\"hello\":\"world\"}]}}");
        Assert.assertEquals(target, expected);
    }    
    
}
