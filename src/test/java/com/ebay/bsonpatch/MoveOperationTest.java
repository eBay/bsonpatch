/*
 * Original work Copyright 2016 flipkart.com zjsonpatch.
 * Modified work Copyright 2017 eBay, Inc bsonpatch.
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

package com.ebay.bsonpatch;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

import java.io.IOException;
import java.util.Collection;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.junit.Test;
import org.junit.runners.Parameterized;

public class MoveOperationTest extends AbstractTest {

    @Parameterized.Parameters
    public static Collection<PatchTestCase> data() throws IOException {
        return PatchTestCase.load("move");
    }

    @Test
    public void testMoveValueGeneratedHasNoValue() throws IOException {
        BsonValue jsonNode1 = BsonDocument.parse("{ \"foo\": { \"bar\": \"baz\", \"waldo\": \"fred\" }, \"qux\": { \"corge\": \"grault\" } }");
        BsonValue jsonNode2 = BsonDocument.parse("{ \"foo\": { \"bar\": \"baz\" }, \"qux\": { \"corge\": \"grault\", \"thud\": \"fred\" } }");
        BsonArray patch = BsonArray.parse("[{\"op\":\"move\",\"from\":\"/foo/waldo\",\"path\":\"/qux/thud\"}]");

        BsonArray diff = BsonDiff.asBson(jsonNode1, jsonNode2);

        assertThat(diff, equalTo(patch));
    }

    @Test
    public void testMoveArrayGeneratedHasNoValue() throws IOException {
    	BsonValue jsonNode1 = BsonDocument.parse("{ \"foo\": [ \"all\", \"grass\", \"cows\", \"eat\" ] }");
    	BsonValue jsonNode2 = BsonDocument.parse("{ \"foo\": [ \"all\", \"cows\", \"eat\", \"grass\" ] }");
        BsonArray patch = BsonArray.parse("[{\"op\":\"move\",\"from\":\"/foo/1\",\"path\":\"/foo/3\"}]");

        BsonArray diff = BsonDiff.asBson(jsonNode1, jsonNode2);

        assertThat(diff, equalTo(patch));
    }
}
