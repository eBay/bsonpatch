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

public class BsonPatchApplicationException extends RuntimeException {
	private static final long serialVersionUID = 7562538769544371424L;
	
	Operation operation;
    JsonPointer path;

	public BsonPatchApplicationException(String message, Operation operation, JsonPointer path) {
        super(message);
        this.operation = operation;
        this.path = path;
    }
	
	@Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (operation != null) sb.append('[').append(operation).append(" Operation] ");
        sb.append(getMessage());
        if (path != null) sb.append(" at ").append(path.isRoot() ? "root" : path);
        return sb.toString();
    }	

}
