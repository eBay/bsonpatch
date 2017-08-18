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

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.EnumSet;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.junit.Test;

public class ApiTest {

    @Test
    public void applyInPlaceMutatesSource() throws Exception {
        BsonArray patch = BsonArray.parse("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        BsonDocument source = new BsonDocument();
        BsonDocument beforeApplication = CopyingApplyProcessor.deepCopy(source).asDocument();
        BsonPatch.apply(patch, source);
        assertThat(source, is(beforeApplication));
    }

    @Test
    public void applyDoesNotMutateSource() throws Exception {
    	BsonArray patch = BsonArray.parse("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        BsonDocument source = new BsonDocument();
        BsonPatch.applyInPlace(patch, source);
        assertThat(source.getString("b").getValue(), is("b-value"));
    }

    @Test
    public void applyDoesNotMutateSource2() throws Exception {
    	BsonArray patch = BsonArray.parse("[{ \"op\": \"add\", \"path\": \"/b\", \"value\": \"b-value\" }]");
        BsonDocument source = new BsonDocument();
        BsonDocument beforeApplication = CopyingApplyProcessor.deepCopy(source).asDocument();
        BsonPatch.apply(patch, source);
        assertThat(source, is(beforeApplication));
    }

    @Test
    public void applyInPlaceMutatesSourceWithCompatibilityFlags() throws Exception {
    	BsonArray patch = BsonArray.parse("[{ \"op\": \"add\", \"path\": \"/b\" }]");
    	BsonDocument source = new BsonDocument();
        BsonPatch.applyInPlace(patch, source, EnumSet.of(CompatibilityFlags.MISSING_VALUES_AS_NULLS));
        assertTrue(source.get("b").isNull());
    }

    @Test(expected = InvalidBsonPatchException.class)
    public void applyingNonArrayPatchShouldThrowAnException() throws IOException {
    	BsonArray invalid = BsonArray.parse("[\"not\", \"a patch\"]");
    	BsonDocument to = BsonDocument.parse("{\"a\":1}");
        BsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidBsonPatchException.class)
    public void applyingAnInvalidArrayShouldThrowAnException() throws IOException {
    	BsonArray invalid = BsonArray.parse("[1, 2, 3, 4, 5]");
    	BsonDocument to = BsonDocument.parse("{\"a\":1}");
        BsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidBsonPatchException.class)
    public void applyingAPatchWithAnInvalidOperationShouldThrowAnException() throws IOException {
    	BsonArray invalid = BsonArray.parse("[{\"op\": \"what\"}]");
    	BsonDocument to = BsonDocument.parse("{\"a\":1}");
        BsonPatch.apply(invalid, to);
    }

    @Test(expected = InvalidBsonPatchException.class)
    public void validatingNonArrayPatchShouldThrowAnException() throws IOException {
    	BsonArray invalid = BsonArray.parse("[\"not\", \"a patch\"]");
        BsonPatch.validate(invalid);
    }

    @Test(expected = InvalidBsonPatchException.class)
    public void validatingAnInvalidArrayShouldThrowAnException() throws IOException {
    	BsonArray invalid = BsonArray.parse("[1, 2, 3, 4, 5]");
        BsonPatch.validate(invalid);
    }

    @Test(expected = InvalidBsonPatchException.class)
    public void validatingAPatchWithAnInvalidOperationShouldThrowAnException() throws IOException {
    	BsonArray invalid = BsonArray.parse("[{\"op\": \"what\"}]");
        BsonPatch.validate(invalid);
    }
}

