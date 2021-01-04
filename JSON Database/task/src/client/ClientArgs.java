package client;

import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ClientArgs {
    @Parameter(names = {"-t"}, description = "request type", order = 0)
    public String type;
    @Parameter(names = {"-k"}, description = "key", order = 1)
    public String key;
    @Parameter(names = {"-v"}, description = "data to add to db (if type=set)", order = 2)
    public String value;
    @Parameter(names = {"-in"}, description = "file name", order = 3)
    public String fileName;

    @Override
    public String toString() {
        if (fileName != null) {
            try (Reader reader = Files.newBufferedReader(Path.of("JSON Database/task/src/client/data/" + fileName))) {
                return JsonParser.parseReader(reader).getAsJsonObject().toString();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new Gson().toJson(this);
    }
}
