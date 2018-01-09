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

import org.bson.BsonArray;
import org.bson.BsonValue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

class InternalUtils {

    static List<BsonValue> toList(BsonArray input) {
        int size = input.size();
        List<BsonValue> toReturn = new ArrayList<BsonValue>(size);
        for (int i = 0; i < size; i++) {
            toReturn.add(input.get(i));
        }
        return toReturn;
    }

    static List<BsonValue> longestCommonSubsequence(final List<BsonValue> a, final List<BsonValue> b) {
        if (a == null || b == null) {
            throw new NullPointerException("List must not be null for longestCommonSubsequence");
        }

        List<BsonValue> toReturn = new LinkedList<BsonValue>();

        int aSize = a.size();
        int bSize = b.size();
        int temp[][] = new int[aSize + 1][bSize + 1];

        for (int i = 1; i <= aSize; i++) {
            for (int j = 1; j <= bSize; j++) {
                if (i == 0 || j == 0) {
                    temp[i][j] = 0;
                } else if (a.get(i - 1).equals(b.get(j - 1))) {
                    temp[i][j] = temp[i - 1][j - 1] + 1;
                } else {
                    temp[i][j] = Math.max(temp[i][j - 1], temp[i - 1][j]);
                }
            }
        }
        int i = aSize, j = bSize;
        while (i > 0 && j > 0) {
            if (a.get(i - 1).equals(b.get(j - 1))) {
                toReturn.add(a.get(i - 1));
                i--;
                j--;
            } else if (temp[i - 1][j] > temp[i][j - 1])
                i--;
            else
                j--;
        }
        Collections.reverse(toReturn);
        return toReturn;
    }
}