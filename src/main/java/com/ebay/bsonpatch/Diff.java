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

import java.util.List;

import org.bson.BsonValue;

class Diff {
    private final Operation operation;
    private final List<Object> path;
    private final BsonValue value;
    private List<Object> toPath; //only to be used in move operation

    Diff(Operation operation, List<Object> path, BsonValue value) {
        this.operation = operation;
        this.path = path;
        this.value = value;
    }

    Diff(Operation operation, List<Object> fromPath, List<Object> toPath) {
        this.operation = operation;
        this.path = fromPath;
        this.toPath = toPath;
        this.value = null;
    }

    public Operation getOperation() {
        return operation;
    }

    public List<Object> getPath() {
        return path;
    }

    public BsonValue getValue() {
        return value;
    }

    public static Diff generateDiff(Operation replace, List<Object> path, BsonValue target) {
        return new Diff(replace, path, target);
    }

    List<Object> getToPath() {
        return toPath;
    }
}
