package com.ebay.bsonpatch;

import org.bson.BsonArray;
import org.bson.BsonValue;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class ValidationTest {

    @ParameterizedTest
    @MethodSource("argsForValidationTest")
    public void testValidation(BsonArray patch) {
        assertThrows(
                InvalidBsonPatchException.class,
                () -> BsonPatch.validate(patch)
        );
    }

    public static Stream<Arguments> argsForValidationTest() throws IOException {
        BsonValue patches = BsonArray.parse(
                TestUtils.stripJsonComments(
                    TestUtils.loadFromResources("/testdata/invalid-patches.json")));

        List<Arguments> args = new ArrayList<>();

        for (BsonValue patch : patches.asArray()) {
            args.add(Arguments.of(patch));
        }

        return args.stream();
    }
}