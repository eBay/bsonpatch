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

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.ListUtils;
import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonString;
import org.bson.BsonValue;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

public final class BsonDiff {

    private static final EncodePathFunction ENCODE_PATH_FUNCTION = new EncodePathFunction();

    private BsonDiff() {
    }

    private final static class EncodePathFunction implements Function<Object, String> {
        @Override
        public String apply(Object object) {
            String path = object.toString(); // see http://tools.ietf.org/html/rfc6901#section-4
            return path.replaceAll("~", "~0").replaceAll("/", "~1");
        }
    }

    public static BsonArray asBson(final BsonValue source, final BsonValue target) {
        return asBson(source, target, DiffFlags.defaults());
    }

    public static BsonArray asBson(final BsonValue source, final BsonValue target, EnumSet<DiffFlags> flags) {
        final List<Diff> diffs = new ArrayList<Diff>();
        List<Object> path = new LinkedList<Object>();
        /*
         * generating diffs in the order of their occurrence
         */
        generateDiffs(diffs, path, source, target);
        /*
         * Merging remove & add to move operation
         */
        compactDiffs(diffs);
        /*
         * Introduce copy operation
         */
        introduceCopyOperation(source, target, diffs);

        return getBsonNodes(diffs, flags);
    }

    private static List<Object> getMatchingValuePath(Map<BsonValue, List<Object>> unchangedValues, BsonValue value) {
        return unchangedValues.get(value);
    }

    private static void introduceCopyOperation(BsonValue source, BsonValue target, List<Diff> diffs) {
        Map<BsonValue, List<Object>> unchangedValues = getUnchangedPart(source, target);
        for (int i = 0; i < diffs.size(); i++) {
            Diff diff = diffs.get(i);
            if (Operation.ADD.equals(diff.getOperation())) {
                List<Object> matchingValuePath = getMatchingValuePath(unchangedValues, diff.getValue());
                if (matchingValuePath != null && !isSame(matchingValuePath, diff.getPath())) {
                    diffs.set(i, new Diff(Operation.COPY, matchingValuePath, diff.getPath()));
                }
            }
        }
    }

    private static boolean isSame(List<Object> source, List<Object> destination) {
        return source.equals(destination);
    }

    private static Map<BsonValue, List<Object>> getUnchangedPart(BsonValue source, BsonValue target) {
        Map<BsonValue, List<Object>> unchangedValues = new HashMap<BsonValue, List<Object>>();
        computeUnchangedValues(unchangedValues, Lists.newArrayList(), source, target);
        return unchangedValues;
    }

    private static void computeUnchangedValues(Map<BsonValue, List<Object>> unchangedValues, List<Object> path, BsonValue source, BsonValue target) {
        if (source.equals(target)) {
            unchangedValues.put(target, path);
            return;
        }

        if (source.getBsonType().equals(target.getBsonType())) {
            switch (source.getBsonType()) {
                case DOCUMENT:
                    computeDocument(unchangedValues, path, source, target);
                    break;
                case ARRAY:
                    computeArray(unchangedValues, path, source, target);
                default:
                /* nothing */
            }
        }
    }

    private static void computeArray(Map<BsonValue, List<Object>> unchangedValues, List<Object> path, BsonValue source, BsonValue target) {
        final int size = Math.min(source.asArray().size(), target.asArray().size());

        for (int i = 0; i < size; i++) {
            List<Object> currPath = getPath(path, i);
            computeUnchangedValues(unchangedValues, currPath, source.asArray().get(i), target.asArray().get(i));
        }
    }

    private static void computeDocument(Map<BsonValue, List<Object>> unchangedValues, List<Object> path, BsonValue source, BsonValue target) {
        final Iterator<String> firstFields = source.asDocument().keySet().iterator();
        while (firstFields.hasNext()) {
            String name = firstFields.next();
            if (target.asDocument().containsKey(name)) {
                List<Object> currPath = getPath(path, name);
                computeUnchangedValues(unchangedValues, currPath, source.asDocument().get(name), target.asDocument().get(name));
            }
        }
    }

    /**
     * This method merge 2 diffs ( remove then add, or vice versa ) with same value into one Move operation,
     * all the core logic resides here only
     */
    private static void compactDiffs(List<Diff> diffs) {
        for (int i = 0; i < diffs.size(); i++) {
            Diff diff1 = diffs.get(i);

            // if not remove OR add, move to next diff
            if (!(Operation.REMOVE.equals(diff1.getOperation()) ||
                    Operation.ADD.equals(diff1.getOperation()))) {
                continue;
            }

            for (int j = i + 1; j < diffs.size(); j++) {
                Diff diff2 = diffs.get(j);
                if (!diff1.getValue().equals(diff2.getValue())) {
                    continue;
                }

                Diff moveDiff = null;
                if (Operation.REMOVE.equals(diff1.getOperation()) &&
                        Operation.ADD.equals(diff2.getOperation())) {
                    computeRelativePath(diff2.getPath(), i + 1, j - 1, diffs);
                    moveDiff = new Diff(Operation.MOVE, diff1.getPath(), diff2.getPath());

                } else if (Operation.ADD.equals(diff1.getOperation()) &&
                        Operation.REMOVE.equals(diff2.getOperation())) {
                    computeRelativePath(diff2.getPath(), i, j - 1, diffs); // diff1's add should also be considered
                    moveDiff = new Diff(Operation.MOVE, diff2.getPath(), diff1.getPath());
                }
                if (moveDiff != null) {
                    diffs.remove(j);
                    diffs.set(i, moveDiff);
                    break;
                }
            }
        }
    }

    //Note : only to be used for arrays
    //Finds the longest common Ancestor ending at Array
    private static void computeRelativePath(List<Object> path, int startIdx, int endIdx, List<Diff> diffs) {
        List<Integer> counters = new ArrayList<Integer>();

        resetCounters(counters, path.size());

        for (int i = startIdx; i <= endIdx; i++) {
            Diff diff = diffs.get(i);
            //Adjust relative path according to #ADD and #Remove
            if (Operation.ADD.equals(diff.getOperation()) || Operation.REMOVE.equals(diff.getOperation())) {
                updatePath(path, diff, counters);
            }
        }
        updatePathWithCounters(counters, path);
    }

    private static void resetCounters(List<Integer> counters, int size) {
        for (int i = 0; i < size; i++) {
            counters.add(0);
        }
    }

    private static void updatePathWithCounters(List<Integer> counters, List<Object> path) {
        for (int i = 0; i < counters.size(); i++) {
            int value = counters.get(i);
            if (value != 0) {
                Integer currValue = Integer.parseInt(path.get(i).toString());
                path.set(i, String.valueOf(currValue + value));
            }
        }
    }

    private static void updatePath(List<Object> path, Diff pseudo, List<Integer> counters) {
        //find longest common prefix of both the paths

        if (pseudo.getPath().size() <= path.size()) {
            int idx = -1;
            for (int i = 0; i < pseudo.getPath().size() - 1; i++) {
                if (pseudo.getPath().get(i).equals(path.get(i))) {
                    idx = i;
                } else {
                    break;
                }
            }
            if (idx == pseudo.getPath().size() - 2) {
                if (pseudo.getPath().get(pseudo.getPath().size() - 1) instanceof Integer) {
                    updateCounters(pseudo, pseudo.getPath().size() - 1, counters);
                }
            }
        }
    }

    private static void updateCounters(Diff pseudo, int idx, List<Integer> counters) {
        if (Operation.ADD.equals(pseudo.getOperation())) {
            counters.set(idx, counters.get(idx) - 1);
        } else {
            if (Operation.REMOVE.equals(pseudo.getOperation())) {
                counters.set(idx, counters.get(idx) + 1);
            }
        }
    }

    private static BsonArray getBsonNodes(List<Diff> diffs, EnumSet<DiffFlags> flags) {
        final BsonArray patch = new BsonArray();
        for (Diff diff : diffs) {
            BsonDocument bsonNode = getBsonNode(diff, flags);
            patch.add(bsonNode);
        }
        return patch;
    }

    private static BsonDocument getBsonNode(Diff diff, EnumSet<DiffFlags> flags) {
    	BsonDocument bsonNode = new BsonDocument();
        bsonNode.put(Constants.OP, new BsonString(diff.getOperation().rfcName()));

        switch (diff.getOperation()) {
            case MOVE:
            case COPY:
                bsonNode.put(Constants.FROM, new BsonString(getArrayNodeRepresentation(diff.getPath())));    // required {from} only in case of Move Operation
                bsonNode.put(Constants.PATH, new BsonString(getArrayNodeRepresentation(diff.getToPath())));  // destination Path
                break;

            case REMOVE:
                bsonNode.put(Constants.PATH, new BsonString(getArrayNodeRepresentation(diff.getPath())));
                if (!flags.contains(DiffFlags.OMIT_VALUE_ON_REMOVE))
                    bsonNode.put(Constants.VALUE, diff.getValue());
                break;

            case ADD:
            case REPLACE:
            case TEST:
                bsonNode.put(Constants.PATH, new BsonString(getArrayNodeRepresentation(diff.getPath())));
                bsonNode.put(Constants.VALUE, diff.getValue());
                break;

            default:
                // Safety net
                throw new IllegalArgumentException("Unknown operation specified:" + diff.getOperation());
        }

        return bsonNode;
    }

    private static String getArrayNodeRepresentation(List<Object> path) {
        return Joiner.on('/').appendTo(new StringBuilder().append('/'),
                Iterables.transform(path, ENCODE_PATH_FUNCTION)).toString();
    }


    private static void generateDiffs(List<Diff> diffs, List<Object> path, BsonValue source, BsonValue target) {
        if (!source.equals(target)) {
            if (source.isArray() && target.isArray()) {
                //both are arrays
                compareArray(diffs, path, source, target);
            } else if (source.isDocument() && target.isDocument()) {
                //both are json
                compareDocuments(diffs, path, source, target);
            } else {
                //can be replaced

                diffs.add(Diff.generateDiff(Operation.REPLACE, path, target));
            }
        }
    }

    private static void compareArray(List<Diff> diffs, List<Object> path, BsonValue source, BsonValue target) {
        List<BsonValue> lcs = getLCS(source, target);
        int srcIdx = 0;
        int targetIdx = 0;
        int lcsIdx = 0;
        int srcSize = source.asArray().size();
        int targetSize = target.asArray().size();
        int lcsSize = lcs.size();

        int pos = 0;
        while (lcsIdx < lcsSize) {
            BsonValue lcsNode = lcs.get(lcsIdx);
            BsonValue srcNode = source.asArray().get(srcIdx);
            BsonValue targetNode = target.asArray().get(targetIdx);


            if (lcsNode.equals(srcNode) && lcsNode.equals(targetNode)) { // Both are same as lcs node, nothing to do here
                srcIdx++;
                targetIdx++;
                lcsIdx++;
                pos++;
            } else {
                if (lcsNode.equals(srcNode)) { // src node is same as lcs, but not targetNode
                    //addition
                    List<Object> currPath = getPath(path, pos);
                    diffs.add(Diff.generateDiff(Operation.ADD, currPath, targetNode));
                    pos++;
                    targetIdx++;
                } else if (lcsNode.equals(targetNode)) { //targetNode node is same as lcs, but not src
                    //removal,
                    List<Object> currPath = getPath(path, pos);
                    diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, srcNode));
                    srcIdx++;
                } else {
                    List<Object> currPath = getPath(path, pos);
                    //both are unequal to lcs node
                    generateDiffs(diffs, currPath, srcNode, targetNode);
                    srcIdx++;
                    targetIdx++;
                    pos++;
                }
            }
        }

        while ((srcIdx < srcSize) && (targetIdx < targetSize)) {
        	BsonValue srcNode = source.asArray().get(srcIdx);
        	BsonValue targetNode = target.asArray().get(targetIdx);
            List<Object> currPath = getPath(path, pos);
            generateDiffs(diffs, currPath, srcNode, targetNode);
            srcIdx++;
            targetIdx++;
            pos++;
        }
        pos = addRemaining(diffs, path, target, pos, targetIdx, targetSize);
        removeRemaining(diffs, path, pos, srcIdx, srcSize, source);
    }

    private static Integer removeRemaining(List<Diff> diffs, List<Object> path, int pos, int srcIdx, int srcSize, BsonValue source) {

        while (srcIdx < srcSize) {
            List<Object> currPath = getPath(path, pos);
            diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.asArray().get(srcIdx)));
            srcIdx++;
        }
        return pos;
    }

    private static Integer addRemaining(List<Diff> diffs, List<Object> path, BsonValue target, int pos, int targetIdx, int targetSize) {
        while (targetIdx < targetSize) {
        	BsonValue bsonNode = target.asArray().get(targetIdx);
            List<Object> currPath = getPath(path, pos);
            diffs.add(Diff.generateDiff(Operation.ADD, currPath, CopyingApplyProcessor.deepCopy(bsonNode)));
            pos++;
            targetIdx++;
        }
        return pos;
    }

    private static void compareDocuments(List<Diff> diffs, List<Object> path, BsonValue source, BsonValue target) {
        Iterator<String> keysFromSrc = source.asDocument().keySet().iterator();
        while (keysFromSrc.hasNext()) {
            String key = keysFromSrc.next();
            if (!target.asDocument().containsKey(key)) {
                //remove case
                List<Object> currPath = getPath(path, key);
                diffs.add(Diff.generateDiff(Operation.REMOVE, currPath, source.asDocument().get(key)));
                continue;
            }
            List<Object> currPath = getPath(path, key);
            generateDiffs(diffs, currPath, source.asDocument().get(key), target.asDocument().get(key));
        }
        Iterator<String> keysFromTarget = target.asDocument().keySet().iterator();
        while (keysFromTarget.hasNext()) {
            String key = keysFromTarget.next();
            if (!source.asDocument().containsKey(key)) {
                //add case
                List<Object> currPath = getPath(path, key);
                diffs.add(Diff.generateDiff(Operation.ADD, currPath, target.asDocument().get(key)));
            }
        }
    }

    private static List<Object> getPath(List<Object> path, Object key) {
        List<Object> toReturn = new ArrayList<Object>();
        toReturn.addAll(path);
        toReturn.add(key);
        return toReturn;
    }

    private static List<BsonValue> getLCS(final BsonValue first, final BsonValue second) {

        Preconditions.checkArgument(first.isArray(), "LCS can only work on BSON arrays");
        Preconditions.checkArgument(second.isArray(), "LCS can only work on BSON arrays");

        return ListUtils.longestCommonSubsequence(Lists.newArrayList(first.asArray()), Lists.newArrayList(second.asArray()));
    }
}
