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

import org.bson.BsonBinary;
import org.bson.BsonJavaScriptWithScope;
import org.bson.BsonValue;
import java.util.EnumSet;

class CopyingApplyProcessor extends InPlaceApplyProcessor {

    CopyingApplyProcessor(BsonValue target) {
        this(target, CompatibilityFlags.defaults());
    }
    
    CopyingApplyProcessor(BsonValue target, EnumSet<CompatibilityFlags> flags) {
        super(deepCopy(target), flags);
     }    
    
    static BsonValue deepCopy(BsonValue source) {
        BsonValue result;
        switch (source.getBsonType()) {
            case DOCUMENT:
                result = source.asDocument().clone();
                break;
            case ARRAY:
            	result = source.asArray().clone();
                break;
            case BINARY:
                result = new BsonBinary(source.asBinary().getType(), source.asBinary().getData().clone());
                break;
            case JAVASCRIPT_WITH_SCOPE:
                result = new BsonJavaScriptWithScope(source.asJavaScriptWithScope().getCode(), source.asJavaScriptWithScope().getScope().clone());
                break;
            default:
                result = source;
        }
        return result;
    }    
    
}
