package server;

import com.google.gson.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static server.JsonDbUtils.*;

public class JsonDatabase {

    private final Path DB_PATH;
    private JsonObject db;

    private final ReentrantReadWriteLock rReadWriteLock = new ReentrantReadWriteLock(true);
    private final ReentrantReadWriteLock.ReadLock rRead = rReadWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock rWrite = rReadWriteLock.writeLock();

    private final Gson gson = new Gson();

    public JsonDatabase(String dbPath) throws IOException {
        this.DB_PATH = Path.of(dbPath);
        createDbFileIfNotExists(DB_PATH);
        this.db = readDbFromFile(DB_PATH).getAsJsonObject();
    }

    public boolean set(String key, String value) {
        boolean result = false;

        rWrite.lock();
        try {
            JsonElement keyJ = JsonParser.parseString(key);
            if (keyJ.isJsonPrimitive()) {
                setValue(db, keyJ.getAsString(), value);
                writeDbToFile(db, DB_PATH);
                result = true;
            } else if (keyJ.isJsonArray()) {
                set2(db, (JsonArray) keyJ, value);
                writeDbToFile(db, DB_PATH);
                result = true;
            }
        } catch (IOException ignored) {
        } finally {
            rWrite.unlock();
        }
        return result;
    }

    public String get(String key) {
        JsonElement jeKey = gson.fromJson(key, JsonElement.class);

        rRead.lock();
        KeyObject keyObject = findKeyObject(db, jeKey);
        String result = getEntity(keyObject);
        rRead.unlock();

        return result;
    }

    public boolean delete(String key) {
        JsonElement jeKey = gson.fromJson(key, JsonElement.class);

        rWrite.lock();
        KeyObject keyObject = findKeyObject(db, jeKey);

        JsonObject newJsonObject = deleteEntity(db, keyObject);
        if (newJsonObject == null) {
            rWrite.unlock();
            return false;
        }

        try {
            writeDbToFile(newJsonObject, DB_PATH);
            this.db = readDbFromFile(DB_PATH).getAsJsonObject();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            rWrite.unlock();
        }

        return true;
    }

    private void set2(JsonObject rootJson, JsonArray keyJ, String value) {
        JsonArray keys = keyJ.getAsJsonArray();

        JsonObject currentJObject = rootJson;
        JsonObject newJObject;
        String k = "";
        boolean branchIsCreated = false;
        for (int i = 0; i < keys.size(); i++) {
            k = keys.get(i).getAsString();
            if (!branchIsCreated && currentJObject.keySet().contains(k)) {
                JsonElement newJObj = currentJObject.get(k);
                if (newJObj.isJsonObject()) {
                    newJObject = newJObj.getAsJsonObject();

                    if (i < keys.size() - 1) {
                        currentJObject = newJObject;
                    }
                }
            } else {
                if (!branchIsCreated) {
                    branchIsCreated = true;
                }

                newJObject = new JsonObject();
                currentJObject.add(k, newJObject);

                if (i < keys.size() - 1) {
                    currentJObject = newJObject;
                }
            }
        }
        setValue(currentJObject, k, value);
    }

    private void setValue(JsonObject currentJObject, String key, String value) {
        JsonElement valueJ = JsonParser.parseString(value);

        if (valueJ.isJsonObject()) {
            currentJObject.add(key, valueJ.getAsJsonObject());
        } else if (valueJ.isJsonPrimitive()) {
            currentJObject.addProperty(key, valueJ.getAsString());
        }
    }

    private String getEntity(KeyObject keyObject) {
        JsonElement jsonEl = keyObject.getCurrentJObject().get(keyObject.k);
        if (jsonEl == null) {
            return null;
        }
        if (jsonEl.isJsonObject()) return gson.toJson(jsonEl);
        else if (jsonEl.isJsonPrimitive()) {
            return jsonEl.getAsString();
        }
        return null;
    }

    private JsonObject deleteEntity(final JsonObject rootJObject, final KeyObject keyObject) {

        if (keyObject.currentJObject.keySet().contains(keyObject.k)) {
            keyObject.currentJObject.remove(keyObject.k);
            return rootJObject;
        }
        return null;
    }

    private KeyObject findKeyObject(final JsonObject currentJObject, final JsonElement jeKey) {
        JsonObject newJObject = null;
        KeyObject keyObject = new KeyObject(currentJObject, "");

        if (jeKey.isJsonArray()) {
            JsonArray keys = jeKey.getAsJsonArray();
            for (int i = 0; i < keys.size(); i++) {
                keyObject.k = keys.get(i).getAsString();
                if (keyObject.currentJObject.keySet().contains(keyObject.k)) {
                    JsonElement jElem = keyObject.currentJObject.get(keyObject.k);
                    if (jElem.isJsonObject()) {
                        newJObject = jElem.getAsJsonObject();
                    } else if (jElem.isJsonPrimitive()) {
                        if (i < keys.size() - 1) {
                            break;
                        }
                    }
                    if (i < keys.size() - 1) {
                        keyObject.currentJObject = newJObject;
                    }
                } else {
                    break;
                }
            }
        } else if (jeKey.isJsonPrimitive()) {
            keyObject.k = jeKey.getAsString();
        }
        return keyObject;
    }

    private static class KeyObject {
        JsonObject currentJObject;
        String k;

        public KeyObject(JsonObject currentJObject, String k) {
            this.currentJObject = currentJObject;
            this.k = k;
        }

        public JsonObject getCurrentJObject() {
            return currentJObject;
        }
    }
}
