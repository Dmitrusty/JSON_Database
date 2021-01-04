package server;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class JsonDbUtils {
    public static JsonElement readDbFromFile(Path path) throws IOException {
        try (Reader reader = Files.newBufferedReader(path)) {
            return JsonParser.parseReader(reader);
        }
    }

    public static void writeDbToFile(JsonObject db, Path path) throws IOException { // synchronize db
        try (Writer writer = Files.newBufferedWriter(path)) {
            new Gson().toJson(db, writer);
        }
    }

    public static void createDbFileIfNotExists(Path path) throws IOException {
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        if (Files.notExists(path)) {
            Files.writeString(path, "{}");
        }
    }
}