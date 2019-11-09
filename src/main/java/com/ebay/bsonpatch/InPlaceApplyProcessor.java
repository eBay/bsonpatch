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
    public void move(JsonPointer fromPath, JsonPointer toPath) throws JsonPointerEvaluationException {
        BsonValue valueNode = fromPath.evaluate(target);
        remove(fromPath);
        set(toPath, valueNode, Operation.MOVE);
    }

    @Override
    public void copy(JsonPointer fromPath, JsonPointer toPath) throws JsonPointerEvaluationException {
    	BsonValue valueNode = fromPath.evaluate(target);
    	BsonValue valueToCopy = valueNode != null ? cloneBsonValue(valueNode) : null;
        set(toPath, valueToCopy, Operation.COPY);
    }
    
    private static String show(BsonValue value) {
        if (value == null || value.isNull())
            return "null";
        else if (value.isArray())
            return "array";
        else if (value.isDocument())
            return "object";
        else if (value.isBoolean())
        	return String.valueOf(value.asBoolean().getValue());
        else if (value.isInt32())
        	return String.valueOf(value.asInt32().intValue());
        else if (value.isInt64())
        	return String.valueOf(value.asInt64().longValue());
        else if (value.isDouble())
        	return String.valueOf(value.asDouble().doubleValue());
        else
            return "value " + value.toString();     // Caveat: numeric may differ from source (e.g. trailing zeros)
    }    

    @Override
    public void test(JsonPointer path, BsonValue value) throws JsonPointerEvaluationException {
    	BsonValue valueNode = path.evaluate(target);
        if (!valueNode.equals(value))
            throw new BsonPatchApplicationException(
                    "Expected value " + show(value) + " but found " + show(valueNode), Operation.TEST, path);
    }

    @Override
    public void add(JsonPointer path, BsonValue value) throws JsonPointerEvaluationException {
        set(path, value, Operation.ADD);
    }
    

    @Override
    public void replace(JsonPointer path, BsonValue value) throws JsonPointerEvaluationException {
        if (path.isRoot()) {
            target = value;
            return;
        }

        BsonValue parentNode = path.getParent().evaluate(target);
        JsonPointer.RefToken token = path.last();
        if (parentNode.isDocument()) {
            if (!parentNode.asDocument().containsKey(token.getField()))
                throw new BsonPatchApplicationException(
                        "Missing field \"" + token.getField() + "\"", Operation.REPLACE, path.getParent());
            parentNode.asDocument().put(token.getField(), value);
        } else if (parentNode.isArray()) {
            if (token.getIndex() >= parentNode.asArray().size())
                throw new BsonPatchApplicationException(
                        "Array index " + token.getIndex() + " out of bounds", Operation.REPLACE, path.getParent());
            parentNode.asArray().set(token.getIndex(), value);
        } else {
            throw new BsonPatchApplicationException(
                    "Can't reference past scalar value", Operation.REPLACE, path.getParent());
        }
    }

    @Override
    public void remove(JsonPointer path) throws JsonPointerEvaluationException {
        if (path.isRoot())
            throw new BsonPatchApplicationException("Cannot remove document root", Operation.REMOVE, path);

        BsonValue parentNode = path.getParent().evaluate(target);
        JsonPointer.RefToken token = path.last();
        if (parentNode.isDocument())
            parentNode.asDocument().remove(token.getField());
        else if (parentNode.isArray()) {
            if (!flags.contains(CompatibilityFlags.REMOVE_NONE_EXISTING_ARRAY_ELEMENT) && token.getIndex() >= parentNode.asArray().size()) {
            	
                throw new BsonPatchApplicationException(
                        "Array index " + token.getIndex() + " out of bounds", Operation.REPLACE, path.getParent());
            } else if (token.getIndex() >= parentNode.asArray().size()) {
            	// do nothing, don't get upset about index out of bounds if REMOVE_NONE_EXISTING_ARRAY_ELEMENT set 
            	// can't just call remove on BsonArray because it throws index out of bounds exception
            } else {
            	parentNode.asArray().remove(token.getIndex());
            }
        } else {
            throw new BsonPatchApplicationException(
                    "Cannot reference past scalar value", Operation.REPLACE, path.getParent());
        }
    }
    
    static BsonValue cloneBsonValue(BsonValue from) {
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
    
    private void set(JsonPointer path, BsonValue value, Operation forOp) throws JsonPointerEvaluationException {
        if (path.isRoot())
            target = value;
        else {
        	BsonValue parentNode = path.getParent().evaluate(target);
            if (!parentNode.isDocument() && !parentNode.isArray())
                throw new BsonPatchApplicationException("Cannot reference past scalar value", forOp, path.getParent());
            else if (parentNode.isArray())
                addToArray(path, value, parentNode);
            else
                addToObject(path, parentNode, value);
        }
    }    

    private void addToObject(JsonPointer path, BsonValue node, BsonValue value) {
        final BsonDocument target = node.asDocument();
        String key = path.last().getField();
        target.put(key, value);
    }

    private void addToArray(JsonPointer path, BsonValue value, BsonValue parentNode) {
        final BsonArray target = parentNode.asArray();
        int idx = path.last().getIndex();

        if (idx == JsonPointer.LAST_INDEX) {
            // see http://tools.ietf.org/html/rfc6902#section-4.1
            target.add(value);
        } else {
            if (idx > target.size())
                throw new BsonPatchApplicationException(
                        "Array index " + idx + " out of bounds", Operation.ADD, path.getParent());
            target.add(idx, value);
        }
    }    
    
}
