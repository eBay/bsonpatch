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

import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.StringContains.containsString;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.commons.io.output.StringBuilderWriter;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonValue;
import org.bson.json.JsonWriterSettings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

@RunWith(Parameterized.class)
public abstract class AbstractTest {

    @Parameter
    public PatchTestCase p;
    
    private static final JsonWriterSettings jwsettings = JsonWriterSettings.builder().indent(true).build();

    protected boolean matchOnErrors() {
        return true;
    }

    @Test
    public void test() throws Exception {
        if (p.isOperation()) {
            testOperation();
        } else {
            testError();
        }
    }

    private void testOperation() throws Exception {
        BsonDocument node = p.getNode();

        BsonValue first = node.get("node");
        BsonValue second = node.get("expected");
        BsonArray patch = node.getArray("op");
        String message = node.containsKey("message") ? node.getString("message").getValue() : "";

        BsonValue secondPrime = BsonPatch.apply(patch, first);

        assertThat(message, secondPrime, equalTo(second));
    }

    private Class<?> exceptionType(String type) throws ClassNotFoundException {
        return Class.forName(type.contains(".") ? type : "org.ebayopensource.bsonpatch." + type);
    }

    private String errorMessage(String header) {
        return errorMessage(header, null);
    }
    private String errorMessage(String header, Exception e) {

        StringBuilder res =
                new StringBuilder()
                        .append(header)
                        .append("\nFull test case (in file ")
                        .append(p.getSourceFile())
                        .append("):\n")
                        .append(p.getNode().toJson(jwsettings));
        if (e != null) {
            res.append("\nFull error: ");
            e.printStackTrace(new PrintWriter(new StringBuilderWriter(res)));
        }
        return res.toString();
    }

    private void testError() throws  ClassNotFoundException {
        BsonDocument node = p.getNode();
        BsonValue first = node.get("node");
        BsonArray patch = node.getArray("op");
        String message = node.containsKey("message") ? node.getString("message").getValue() : "";
        Class<?> type =
                node.containsKey("type") ? exceptionType(node.getString("type").getValue()) : BsonPatchApplicationException.class;

        try {
            BsonPatch.apply(patch, first);

            fail(errorMessage("Failure expected: " + message));
        } catch (Exception e) {
            if (matchOnErrors()) {
                StringWriter fullError = new StringWriter();
                e.printStackTrace(new PrintWriter(fullError));

                assertThat(
                        errorMessage("Operation failed but with wrong exception type", e),
                        e,
                        instanceOf(type));
                if (message != null) {
                    assertThat(
                            errorMessage("Operation failed but with wrong message", e),
                            e.getMessage(),
                            containsString(message));    // equalTo would be better, but fail existing tests
                }
            }
        }
    }
}
