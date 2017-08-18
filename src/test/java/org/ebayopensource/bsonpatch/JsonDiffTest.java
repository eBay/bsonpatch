/*
 * Original work Copyright 2016 flipkart.com zjsonpatch.
 * Modified work Copyright 2017 ebayopensource.org bsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.ebayopensource.bsonpatch;

import java.io.IOException;
import java.io.InputStream;
import java.util.EnumSet;
import java.util.Random;

import org.apache.commons.io.IOUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsonDiffTest {
    static BsonArray jsonNode;

    @BeforeClass
    public static void beforeClass() throws IOException {
        String path = "/testdata/sample.json";
        InputStream resourceAsStream = JsonDiffTest.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        jsonNode = BsonArray.parse(testData);
    }

    @Test
    public void testSampleJsonDiff() throws Exception {
        for (int i = 0; i < jsonNode.size(); i++) {
            BsonValue first = jsonNode.get(i).asDocument().get("first");
            BsonValue second = jsonNode.get(i).asDocument().get("second");

            System.out.println("Test # " + i);
            System.out.println(first);
            System.out.println(second);

            BsonArray actualPatch = BsonDiff.asBson(first, second);

            System.out.println(actualPatch);

            BsonValue secondPrime = BsonPatch.apply(actualPatch, first);
            System.out.println(secondPrime);
            Assert.assertTrue(second.equals(secondPrime));
        }
    }

    @Test
    public void testGeneratedJsonDiff() throws Exception {
        Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            BsonArray first = TestDataGenerator.generate(random.nextInt(10));
            BsonArray second = TestDataGenerator.generate(random.nextInt(10));

            BsonArray actualPatch = BsonDiff.asBson(first, second);
            System.out.println("Test # " + i);

            System.out.println(first);
            System.out.println(second);
            System.out.println(actualPatch);

            BsonArray secondPrime = BsonPatch.apply(actualPatch, first).asArray();
            System.out.println(secondPrime);
            Assert.assertTrue(second.equals(secondPrime));
        }
    }

    @Test
    public void testRenderedRemoveOperationOmitsValueByDefault() throws Exception {
        BsonDocument source = new BsonDocument();
        BsonDocument target = new BsonDocument();
        source.put("field", new BsonString("value"));

        BsonArray diff = BsonDiff.asBson(source, target);

        Assert.assertEquals(Operation.REMOVE.rfcName(), diff.get(0).asDocument().getString("op").getValue());
        Assert.assertEquals("/field", diff.get(0).asDocument().getString("path").getValue());
        Assert.assertNull(diff.get(0).asDocument().get("value"));
    }

    @Test
    public void testRenderedRemoveOperationRetainsValueIfOmitDiffFlagNotSet() throws Exception {
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
}
