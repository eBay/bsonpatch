/*
 * Original work Copyright 2016 flipkart.com zjsonpatch.
 * Modified work Copyright 2017 eBay, Inc bsonpatch.
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

package com.ebay.bsonpatch;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.bson.BsonArray;
import org.bson.BsonDocument;
import org.bson.BsonInt32;
import org.bson.BsonString;

public class TestDataGenerator {
    private static Random random = new Random();
    private static List<String> name = Arrays.asList("summers", "winters", "autumn", "spring", "rainy");
    private static List<Integer> age = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);
    private static List<String> gender = Arrays.asList("male", "female");
    private static List<String> country = Arrays.asList("india", "aus", "nz", "sl", "rsa", "wi", "eng", "bang", "pak");
    private static List<String> friends = Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j",
            "a", "b", "c", "d", "e", "f", "g", "h", "i", "j");

    public static BsonArray generate(int count) {
        BsonArray jsonNode = new BsonArray();
        for (int i = 0; i < count; i++) {
            BsonDocument objectNode = new BsonDocument();
            objectNode.put("name", new BsonString(name.get(random.nextInt(name.size()))));
            objectNode.put("age", new BsonInt32(age.get(random.nextInt(age.size()))));
            objectNode.put("gender", new BsonString(gender.get(random.nextInt(gender.size()))));
            BsonArray countryNode = getArrayNode(country.subList(random.nextInt(country.size() / 2), (country.size() / 2) + random.nextInt(country.size() / 2)));
            objectNode.put("country", countryNode);
            BsonArray friendNode = getArrayNode(friends.subList(random.nextInt(friends.size() / 2), (friends.size() / 2) + random.nextInt(friends.size() / 2)));
            objectNode.put("friends", friendNode);
            jsonNode.add(objectNode);
        }
        return jsonNode;
    }

    private static BsonArray getArrayNode(List<String> args) {
    	BsonArray countryNode = new BsonArray();
        for(String arg : args){
            countryNode.add(new BsonString(arg));
        }
        return countryNode;
    }
}
