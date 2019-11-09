package com.ebay.bsonpatch;

import org.bson.BsonValue;

@SuppressWarnings("serial")
public class JsonPointerEvaluationException extends Exception {
    private final JsonPointer path;
    private final BsonValue target;

    public JsonPointerEvaluationException(String message, JsonPointer path, BsonValue target) {
        super(message);
        this.path = path;
        this.target = target;
    }

    public JsonPointer getPath() {
        return path;
    }

    public BsonValue getTarget() {
        return target;
    }
}