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

interface BsonPatchProcessor {
    void remove(JsonPointer path) throws JsonPointerEvaluationException;
    void replace(JsonPointer path, BsonValue value) throws JsonPointerEvaluationException;
    void add(JsonPointer path, BsonValue value) throws JsonPointerEvaluationException;
    void move(JsonPointer fromPath, JsonPointer toPath) throws JsonPointerEvaluationException;
    void copy(JsonPointer fromPath, JsonPointer toPath) throws JsonPointerEvaluationException;
    void test(JsonPointer path, BsonValue value) throws JsonPointerEvaluationException;
}
