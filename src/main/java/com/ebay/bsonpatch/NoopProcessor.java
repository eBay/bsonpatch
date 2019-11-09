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

/** 
 * A JSON patch processor that does nothing, intended for testing and validation. 
 */
public class NoopProcessor implements BsonPatchProcessor {
    static final NoopProcessor INSTANCE;
    static {
        INSTANCE = new NoopProcessor();
    }

    @Override public void remove(JsonPointer path) {}
    @Override public void replace(JsonPointer path, BsonValue value) {}
    @Override public void add(JsonPointer path, BsonValue value) {}
    @Override public void move(JsonPointer fromPath, JsonPointer toPath) {}
    @Override public void copy(JsonPointer fromPath, JsonPointer toPath) {}
    @Override public void test(JsonPointer path, BsonValue value) {}

}
