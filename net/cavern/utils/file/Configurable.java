package net.cavern.utils.file;

import com.google.gson.JsonObject;

public interface Configurable {
    void saveConfig(JsonObject out);
    void loadConfig(JsonObject in);
}