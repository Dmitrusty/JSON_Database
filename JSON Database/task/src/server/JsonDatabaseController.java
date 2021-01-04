package server;

import com.google.gson.*;

public class JsonDatabaseController {
    private static CmdSet command;
    private static String key;
    private static String value;
    private static final String errMsg = "ERROR";
    private static final String okMsg = "OK";
    public static Gson gson = new Gson();

    public JsonDatabaseController() {
    }

    String process(final String clientRequestJson) {
        String response;
        String reason = "";
        String answerValue = "";

        parseRequest(clientRequestJson);
        switch (command) {
            case get:
                response = (answerValue = Main.server.database.get(key)) == null ? errMsg : okMsg;
                reason = "No such key";
                break;
            case set:
                response = Main.server.database.set(key, value) ? okMsg : errMsg;
                reason = "error writing to database";
                break;
            case delete:
                response = Main.server.database.delete(key) ? okMsg : errMsg;
                reason = "No such key";
                break;
            case error:
                reason = "Bad request from client";
            default:
                response = errMsg;
        }

        return createAnswerJson(response, reason, answerValue);
    }

    private String createAnswerJson(String response, String reason, String answerValue) {
        JsonObject answer = new JsonObject();

        answer.addProperty("response", response);

        if (response.equals(errMsg)) {
            answer.addProperty("reason", reason);
        } else if (command.equals(CmdSet.get) && response.equals(okMsg)) {
            boolean isJson = false;
            JsonElement jEl = new JsonObject();

            try {
                jEl = JsonParser.parseString(answerValue);
                isJson = true;
            } catch (JsonSyntaxException ignored) {
            }

            if (isJson) {
                answer.add("value", jEl);
            } else {
                answer.addProperty("value", answerValue);
            }
        }
        return gson.toJson(answer);
    }

    private boolean isValidCmd(final String cmd) {
        for (CmdSet c : CmdSet.values()) {
            if (c.name().equals(cmd)) {
                return true;
            }
        }
        return false;
    }


    void parseRequest(final String clientRequestJson) {
        JsonObject request = gson.fromJson(clientRequestJson, JsonObject.class);
        String s;
        JsonElement jEl;

        s = request.get(KeySet.type.name()).getAsString();
        command = s == null ? CmdSet.error :
                (isValidCmd(s) ? CmdSet.valueOf(s) : CmdSet.error);

        if (command.equals(CmdSet.error)) {
            key = value = "";
            return;
        }

        jEl = request.get(KeySet.key.name());
        if (jEl == null) {
            key = "";
        } else {
            if (jEl.isJsonArray()) key = jEl.getAsJsonArray().toString();
            else if (jEl.isJsonPrimitive()) {
                key = jEl.getAsString();
            } else {
                key = "";
                command = CmdSet.error;
                return;
            }
        }

        jEl = request.get(KeySet.value.name());
        if (jEl == null) {
            value = "";
        } else {
            if (jEl.isJsonObject()) {
                value = jEl.getAsJsonObject().toString();
            } else if (jEl.isJsonPrimitive()) {
                value = jEl.getAsJsonPrimitive().toString();
            } else {
                value = "";
                command = CmdSet.error;
                return;
            }
        }
    }
}
