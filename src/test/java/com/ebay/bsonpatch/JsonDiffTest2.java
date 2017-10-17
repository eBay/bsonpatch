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

import static org.hamcrest.core.IsEqual.equalTo;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class JsonDiffTest2 {
    static BsonArray jsonNode;

    @BeforeClass
    public static void beforeClass() throws IOException {
        String path = "/testdata/diff.json";
        InputStream resourceAsStream = JsonDiffTest.class.getResourceAsStream(path);
        String testData = IOUtils.toString(resourceAsStream, "UTF-8");
        jsonNode = BsonArray.parse(testData);
    }

    @Test
    public void testPatchAppliedCleanly() throws Exception {
        for (int i = 0; i < jsonNode.size(); i++) {
        	BsonDocument node = jsonNode.get(i).asDocument();
        	
            BsonValue first = node.get("first");
            BsonValue second = node.get("second");
            BsonArray patch = node.getArray("patch");
            String message = node.containsKey("message") ? node.getString("message").getValue() : "";

            System.out.println("Test # " + i);
            System.out.println(first);
            System.out.println(second);
            System.out.println(patch);

            BsonValue secondPrime = BsonPatch.apply(patch, first);
            System.out.println(secondPrime);
            Assert.assertThat(message, secondPrime, equalTo(second));
        }

    }
}
