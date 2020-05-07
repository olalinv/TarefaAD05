package com.robottitto.util;

import com.google.gson.Gson;
import com.robottitto.model.Config;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class JsonUtils {

    public static Config readConfig(String json) throws FileNotFoundException {
        return new Gson().fromJson(new FileReader(json), Config.class);
    }

}
