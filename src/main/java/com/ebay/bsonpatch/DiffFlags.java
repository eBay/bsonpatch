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

public enum DiffFlags {
    /**
     * This flag omits the <i>value</i> field on remove operations.
     * This is a default flag.
     */	
    OMIT_VALUE_ON_REMOVE,
    
    /**
     * This flag omits all {@link Operation#MOVE} operations, leaving only
     * {@link Operation#ADD}, {@link Operation#REMOVE}, {@link Operation#REPLACE}
     * and {@link Operation#COPY} operations. In other words, without this flag,
     * {@link Operation#ADD} and {@link Operation#REMOVE} operations are not normalized
     * into {@link Operation#MOVE} operations.
     */
    OMIT_MOVE_OPERATION,

    /**
     * This flag omits all {@link Operation#COPY} operations, leaving only
     * {@link Operation#ADD}, {@link Operation#REMOVE}, {@link Operation#REPLACE}
     * and {@link Operation#MOVE} operations. In other words, without this flag,
     * {@link Operation#ADD} operations are not normalized into {@link Operation#COPY}
     * operations.
     */
    OMIT_COPY_OPERATION,

    /**
     * This flag adds a <i>fromValue</i> field to all {@link Operation#REPLACE}operations.
     * <i>fromValue</i> represents the the value replaced by a {@link Operation#REPLACE}
     * operation, in other words, the original value.
     *
     * @since 0.4.1
     */
    ADD_ORIGINAL_VALUE_ON_REPLACE;

    public static EnumSet<DiffFlags> defaults() {
        return EnumSet.of(OMIT_VALUE_ON_REMOVE);
    }

    public static EnumSet<DiffFlags> dontNormalizeOpIntoMoveAndCopy() {
    	return EnumSet.of(OMIT_MOVE_OPERATION, OMIT_COPY_OPERATION);
    }
    
}
