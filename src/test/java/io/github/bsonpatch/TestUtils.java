package io.github.bsonpatch;

import org.apache.commons.io.IOUtils;
import org.bson.BsonDocument;
import org.bson.BsonValue;

import java.io.IOException;
import java.io.InputStream;

public class TestUtils {

    private TestUtils() {
    }

    public static BsonValue loadResourceAsBsonValue(String path) throws IOException {
        String testData = loadFromResources(path);
        return BsonDocument.parse(testData);
    }

    public static String loadFromResources(String path) throws IOException {
        InputStream resourceAsStream = PatchTestCase.class.getResourceAsStream(path);
        return IOUtils.toString(resourceAsStream, "UTF-8");
    }

    public static String stripJsonComments(String s) {
        StringBuilder out = new StringBuilder();
        boolean inStr = false, esc = false, inLine = false, inBlock = false;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i), n = (i + 1 < s.length()) ? s.charAt(i + 1) : '\0';
            if (inLine) {
                if (c == '\n') { inLine = false; out.append(c); }
                continue;
            }
            if (inBlock) {
                if (c == '*' && n == '/') { inBlock = false; i++; }
                continue;
            }
            if (inStr) {
                out.append(c);
                if (!esc && c == '\\') { esc = true; }
                else if (esc) { esc = false; }
                else if (c == '"') { inStr = false; }
                continue;
            }
            if (c == '"') { inStr = true; out.append(c); continue; }
            if (c == '/' && n == '/') { inLine = true; i++; continue; }
            if (c == '/' && n == '*') { inBlock = true; i++; continue; }
            out.append(c);
        }
        return out.toString();
    }
}