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

import org.bson.BsonValue;

class Diff {
    private final Operation operation;
    private final JsonPointer path;
    private final BsonValue value;
    private JsonPointer toPath; //only to be used in move operation
    private final BsonValue srcValue; // only used in replace operation

    Diff(Operation operation, JsonPointer path, BsonValue value) {
        this.operation = operation;
        this.path = path;
        this.value = value;
        this.srcValue = null;
    }

    Diff(Operation operation, JsonPointer fromPath, JsonPointer toPath) {
        this.operation = operation;
        this.path = fromPath;
        this.toPath = toPath;
        this.value = null;
        this.srcValue = null;
    }
    
    Diff(Operation operation, JsonPointer path, BsonValue srcValue, BsonValue value) {
        this.operation = operation;
        this.path = path;
        this.value = value;
        this.srcValue = srcValue;
    }    

    public Operation getOperation() {
        return operation;
    }

    public JsonPointer getPath() {
        return path;
    }

    public BsonValue getValue() {
        return value;
    }

    public static Diff generateDiff(Operation replace, JsonPointer path, BsonValue target) {
        return new Diff(replace, path, target);
    }
    
    public static Diff generateDiff(Operation replace, JsonPointer path, BsonValue source, BsonValue target) {
        return new Diff(replace, path, source, target);
    }    

    JsonPointer getToPath() {
        return toPath;
    }
    
    public BsonValue getSrcValue(){
        return srcValue;
    }    
}
