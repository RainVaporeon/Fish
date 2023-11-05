package com.spiritlight.chess.fish.internal.utils.resources;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.*;

public class Resources {
    public static InputStream get(String file) {
        try {
            InputStream v = Resources.class.getResourceAsStream(file);
            if(v != null || file.startsWith("/")) return v;
            return Resources.class.getResourceAsStream("/" + file);
        } catch (Exception ex) {
            return null;
        }
    }

    public static JsonElement getAsJson(String file) {
        try(InputStream stream = Resources.get(file)) {
            if(stream == null) throw new NullPointerException();
            Reader reader = new BufferedReader(new InputStreamReader(stream));
            return JsonParser.parseReader(reader);
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }
}
