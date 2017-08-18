/*
 * Original work Copyright 2016 flipkart.com zjsonpatch.
 * Modified work Copyright 2017 ebayopensource.org bsonpatch.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.ebayopensource.bsonpatch;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

enum Operation {
    ADD("add"),
    REMOVE("remove"),
    REPLACE("replace"),
    MOVE("move"),
    COPY("copy"),
    TEST("test");

    private final static Map<String, Operation> OPS = new ImmutableMap.Builder<String, Operation>()
            .put(ADD.rfcName, ADD)
            .put(REMOVE.rfcName, REMOVE)
            .put(REPLACE.rfcName, REPLACE)
            .put(MOVE.rfcName, MOVE)
            .put(COPY.rfcName, COPY)
            .put(TEST.rfcName, TEST)
            .build();


    private String rfcName;

    Operation(String rfcName) {
        this.rfcName = rfcName;
    }

    public static Operation fromRfcName(String rfcName) throws InvalidBsonPatchException {
        if (rfcName == null) throw new InvalidBsonPatchException("rfcName cannot be null");
        Operation op = OPS.get(rfcName.toLowerCase());
        if (op == null) throw new InvalidBsonPatchException("unknown / unsupported operation " + rfcName);
        return op;
    }

    public String rfcName() {
        return this.rfcName;
    }


}
