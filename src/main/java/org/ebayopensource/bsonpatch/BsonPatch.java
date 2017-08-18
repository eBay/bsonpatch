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

import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;

import org.bson.BsonArray;
import org.bson.BsonNull;
import org.bson.BsonValue;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class BsonPatch {

    private static final DecodePathFunction DECODE_PATH_FUNCTION = new DecodePathFunction();

    private BsonPatch() {}

    private final static class DecodePathFunction implements Function<String, String> {
        @Override
        public String apply(String path) {
            return path.replaceAll("~1", "/").replaceAll("~0", "~"); // see http://tools.ietf.org/html/rfc6901#section-4
        }
    }

    private static BsonValue getPatchAttr(BsonValue bsonNode, String attr) {
    	BsonValue child = bsonNode.asDocument().get(attr);
        if (child == null)
            throw new InvalidBsonPatchException("Invalid BSON Patch payload (missing '" + attr + "' field)");
        return child;
    }

    private static BsonValue getPatchAttrWithDefault(BsonValue bsonNode, String attr, BsonValue defaultValue) {
    	BsonValue child = bsonNode.asDocument().get(attr);
        if (child == null)
            return defaultValue;
        else
            return child;
    }

    private static void process(BsonArray patch, BsonPatchProcessor processor, EnumSet<CompatibilityFlags> flags)
            throws InvalidBsonPatchException {

        Iterator<BsonValue> operations = patch.iterator();
        while (operations.hasNext()) {
        	BsonValue bsonNode = operations.next();
            if (!bsonNode.isDocument()) throw new InvalidBsonPatchException("Invalid BSON Patch payload (not an object)");
            Operation operation = Operation.fromRfcName(getPatchAttr(bsonNode, Constants.OP).asString().getValue().replaceAll("\"", ""));
            List<String> path = getPath(getPatchAttr(bsonNode, Constants.PATH));

            switch (operation) {
                case REMOVE: {
                    processor.remove(path);
                    break;
                }

                case ADD: {
                    BsonValue value;
                    if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                        value = getPatchAttr(bsonNode, Constants.VALUE);
                    else
                        value = getPatchAttrWithDefault(bsonNode, Constants.VALUE, BsonNull.VALUE);
                    processor.add(path, CopyingApplyProcessor.deepCopy(value));
                    break;
                }

                case REPLACE: {
                	BsonValue value;
                    if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                        value = getPatchAttr(bsonNode, Constants.VALUE);
                    else
                        value = getPatchAttrWithDefault(bsonNode, Constants.VALUE, BsonNull.VALUE);
                    processor.replace(path, CopyingApplyProcessor.deepCopy(value));
                    break;
                }

                case MOVE: {
                    List<String> fromPath = getPath(getPatchAttr(bsonNode, Constants.FROM));
                    processor.move(fromPath, path);
                    break;
                }

                case COPY: {
                    List<String> fromPath = getPath(getPatchAttr(bsonNode, Constants.FROM));
                    processor.copy(fromPath, path);
                    break;
                }

                case TEST: {
                	BsonValue value;
                    if (!flags.contains(CompatibilityFlags.MISSING_VALUES_AS_NULLS))
                        value = getPatchAttr(bsonNode, Constants.VALUE);
                    else
                        value = getPatchAttrWithDefault(bsonNode, Constants.VALUE, BsonNull.VALUE);
                    processor.test(path, CopyingApplyProcessor.deepCopy(value));
                    break;
                }
            }
        }
    }

    public static void validate(BsonArray patch, EnumSet<CompatibilityFlags> flags) throws InvalidBsonPatchException {
        process(patch, NoopProcessor.INSTANCE, flags);
    }

    public static void validate(BsonArray patch) throws InvalidBsonPatchException {
        validate(patch, CompatibilityFlags.defaults());
    }

    public static BsonValue apply(BsonArray patch, BsonValue source, EnumSet<CompatibilityFlags> flags) throws BsonPatchApplicationException {
        CopyingApplyProcessor processor = new CopyingApplyProcessor(source);
        process(patch, processor, flags);
        return processor.result();
    }

    public static BsonValue apply(BsonArray patch, BsonValue source) throws BsonPatchApplicationException {
        return apply(patch, source, CompatibilityFlags.defaults());
    }

    public static void applyInPlace(BsonArray patch, BsonValue source){
        applyInPlace(patch, source, CompatibilityFlags.defaults());
    }

    public static void applyInPlace(BsonArray patch, BsonValue source, EnumSet<CompatibilityFlags> flags){
        InPlaceApplyProcessor processor = new InPlaceApplyProcessor(source);
        process(patch, processor, flags);
    }

    private static List<String> getPath(BsonValue path) {
        List<String> paths = Splitter.on('/').splitToList(path.asString().getValue().replaceAll("\"", ""));
        return Lists.newArrayList(Iterables.transform(paths, DECODE_PATH_FUNCTION));
    }
}
