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

import java.util.EnumSet;
import java.util.List;

import org.bson.BsonArray;
import org.bson.BsonBinary;
import org.bson.BsonDocument;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonValue;

class InPlaceApplyProcessor implements BsonPatchProcessor {

    private BsonValue target;
    private EnumSet<CompatibilityFlags> flags;

    InPlaceApplyProcessor(BsonValue target) {
    	this(target, CompatibilityFlags.defaults());
    }

    InPlaceApplyProcessor(BsonValue target, EnumSet<CompatibilityFlags> flags) {
        this.target = target;
        this.flags = flags;
    }    

    public BsonValue result() {
        return target;
    }

    @Override
    public void move(List<String> fromPath, List<String> toPath) {
        BsonValue parentNode = getParentNode(fromPath, Operation.MOVE);
        String field = fromPath.get(fromPath.size() - 1).replaceAll("\"", "");
        BsonValue valueNode = parentNode.isArray() ? parentNode.asArray().get(Integer.parseInt(field)) : parentNode.asDocument().get(field);
        remove(fromPath);
        add(toPath, valueNode);
    }

    @Override
    public void copy(List<String> fromPath, List<String> toPath) {
        BsonValue parentNode = getParentNode(fromPath, Operation.COPY);
        String field = fromPath.get(fromPath.size() - 1).replaceAll("\"", "");
        BsonValue valueNode = parentNode.isArray() ? parentNode.asArray().get(Integer.parseInt(field)) : parentNode.asDocument().get(field);
        BsonValue valueToCopy = valueNode != null ? cloneBsonValue(valueNode) : null;
        add(toPath, valueToCopy);
    }

    @Override
    public void test(List<String> path, BsonValue value) {
        if (path.isEmpty()) {
            error(Operation.TEST, "path is empty , path : ");
        } else {
        	BsonValue parentNode = getParentNode(path, Operation.TEST);
            String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
            if (fieldToReplace.equals("") && path.size() == 1)
                if (target.equals(value)) {
                    target = value;
                } else {
                    error(Operation.TEST, "value mismatch");
                }
            else if (!parentNode.isDocument() && !parentNode.isArray())
                error(Operation.TEST, "parent is not a container in source, path provided : " + PathUtils.getPathRepresentation(path) + " | node : " + parentNode);
            else if (parentNode.isArray()) {
                final BsonArray target = parentNode.asArray();
                String idxStr = path.get(path.size() - 1);

                if ("-".equals(idxStr)) {
                    // see http://tools.ietf.org/html/rfc6902#section-4.1
                    if(!target.get(target.size() - 1).equals(value)) {
                        error(Operation.TEST, "value mismatch");
                    }
                } else {
                    int idx = arrayIndex(idxStr.replaceAll("\"", ""), target.size(), false);
                    if (!target.get(idx).equals(value)) {
                        error(Operation.TEST, "value mismatch");
                    }
                }
            } else {
                final BsonDocument target = parentNode.asDocument();
                String key = path.get(path.size() - 1).replaceAll("\"", "");
                BsonValue actual = target.get(key);
                if (actual == null)
                    error(Operation.TEST, "noSuchPath in source, path provided : " + PathUtils.getPathRepresentation(path));
                else if (!actual.equals(value))
                    error(Operation.TEST, "value mismatch");
            }
        }
    }

    @Override
    public void add(List<String> path, BsonValue value) {
        if (path.isEmpty()) {
            error(Operation.ADD, "path is empty , path : ");
        } else {
        	BsonValue parentNode = getParentNode(path, Operation.ADD);
            String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
            if (fieldToReplace.equals("") && path.size() == 1)
                target = value;
            else if (!parentNode.isDocument() && !parentNode.isArray())
                error(Operation.ADD, "parent is not a container in source, path provided : " + PathUtils.getPathRepresentation(path) + " | node : " + parentNode);
            else if (parentNode.isArray())
                addToArray(path, value, parentNode);
            else
                addToObject(path, parentNode, value);
        }
    }

    private void addToObject(List<String> path, BsonValue node, BsonValue value) {
        final BsonDocument target = node.asDocument();
        String key = path.get(path.size() - 1).replaceAll("\"", "");
        target.put(key, value);
    }

    private void addToArray(List<String> path, BsonValue value, BsonValue parentNode) {
        final BsonArray target = parentNode.asArray();
        String idxStr = path.get(path.size() - 1);

        if ("-".equals(idxStr)) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            int idx = arrayIndex(idxStr.replaceAll("\"", ""), target.size(), false);
            target.add(idx, value);
        }
    }

    @Override
    public void replace(List<String> path, BsonValue value) {
        if (path.isEmpty()) {
            error(Operation.REPLACE, "path is empty");
        } else {
            BsonValue parentNode = getParentNode(path, Operation.REPLACE);
            String fieldToReplace = path.get(path.size() - 1).replaceAll("\"", "");
            if (isNullOrEmpty(fieldToReplace) && path.size() == 1)	
                target = value;
            else if (parentNode.isDocument())
                parentNode.asDocument().put(fieldToReplace, value);
            else if (parentNode.isArray())
                parentNode.asArray().set(arrayIndex(fieldToReplace, parentNode.asArray().size() - 1, false), value);
            else
                error(Operation.REPLACE, "noSuchPath in source, path provided : " + PathUtils.getPathRepresentation(path));
        }
    }

    @Override
    public void remove(List<String> path) {
        if (path.isEmpty()) {
            error(Operation.REMOVE, "path is empty");
        } else {
            BsonValue parentNode = getParentNode(path, Operation.REMOVE);
            String fieldToRemove = path.get(path.size() - 1).replaceAll("\"", "");
            if (parentNode.isDocument())
                parentNode.asDocument().remove(fieldToRemove);
            else if (parentNode.isArray()) {
            	// If path specifies a non-existent array element and the REMOVE_NONE_EXISTING_ARRAY_ELEMENT flag is not set, then
            	// arrayIndex will throw an error.
            	int i = arrayIndex(fieldToRemove, parentNode.asArray().size() - 1, flags.contains(CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT));
            	// However, BsonArray.remove(int) is not very forgiving, so we need to avoid making the call if the index is past the end
            	// otherwise, we'll get an IndexArrayOutOfBounds error
            	if (i < parentNode.asArray().size()) {
            		parentNode.asArray().remove(i);
            	}
            } else
                error(Operation.REMOVE, "noSuchPath in source, path provided : " + PathUtils.getPathRepresentation(path));
        }
    }

    private void error(Operation forOp, String message) {
        throw new BsonPatchApplicationException("[" + forOp + " Operation] " + message);
    }

    private BsonValue getParentNode(List<String> fromPath, Operation forOp) {
        List<String> pathToParent = fromPath.subList(0, fromPath.size() - 1); // would never by out of bound, lets see
        BsonValue node = getNode(target, pathToParent, 1);
        if (node == null)
        	error(forOp, "noSuchPath in source, path provided: " + PathUtils.getPathRepresentation(fromPath));
        return node;
    }

    private BsonValue getNode(BsonValue ret, List<String> path, int pos) {
        if (pos >= path.size()) {
            return ret;
        }
        String key = path.get(pos);
        if (ret.isArray()) {
            int keyInt = Integer.parseInt(key.replaceAll("\"", ""));
            // Check for index out of bounds, treat as no such path error
            if (keyInt >= ret.asArray().size()) {
            	return null;
            }
            BsonValue element = ret.asArray().get(keyInt);
            if (element == null)
                return null;
            else
                return getNode(ret.asArray().get(keyInt), path, ++pos);
        } else if (ret.isDocument()) {
            if (ret.asDocument().containsKey(key)) {
                return getNode(ret.asDocument().get(key), path, ++pos);
            }
            return null;
        } else {
            return ret;
        }
    }

    private int arrayIndex(String s, int max, boolean allowNoneExisting) {
        int index;
        try {
            index = Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            throw new BsonPatchApplicationException("Object operation on array target");
        }
        if (index < 0) {
            throw new BsonPatchApplicationException("index Out of bound, index is negative");
        } else if (index > max) {
        	if (!allowNoneExisting)
        		throw new BsonPatchApplicationException("index Out of bound, index is greater than " + max);
        }
        return index;
    }
    
    private boolean isNullOrEmpty(String string) {
        return string == null || string.length() == 0;
    }
    
    private static BsonValue cloneBsonValue(BsonValue from) {
        BsonValue to;
        switch (from.getBsonType()) {
            case DOCUMENT:
                to = from.asDocument().clone();
                break;
            case ARRAY:
                to = from.asArray().clone();
                break;
            case BINARY:
            	to = new BsonBinary(from.asBinary().getType(), from.asBinary().getData().clone());
                break;
            case JAVASCRIPT_WITH_SCOPE:
            	to = new BsonJavaScriptWithScope(from.asJavaScriptWithScope().getCode(), from.asJavaScriptWithScope().getScope().clone());
                break;
            default:
                to = from; // assume that from is immutable
        }
        return to;
    }
    
}
