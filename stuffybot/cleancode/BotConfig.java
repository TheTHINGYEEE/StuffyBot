/*
 * Copyright (C) 2021 TheTHINGYEEEEE
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.github.thethingyee.stuffybot.cleancode;

import com.cedarsoftware.util.io.*;
import org.json.*;

import java.io.*;
import java.nio.file.*;
import java.util.*;

import static com.github.thethingyee.stuffybot.StuffyBot.logger;

public class BotConfig {

    private final File configFile;
    private String contents;

    private String botToken;
    private String botPrefix;
    private String botAuthor;
    private boolean updateMode;

    public BotConfig(File configFile) throws IOException {
        this.configFile = configFile;

        if(!configFile.exists()) {
            logger.info("Configuration file not found. Creating new one...");
            //noinspection ResultOfMethodCallIgnored
            configFile.createNewFile();

            JSONObject rootObject = new JSONObject();
            JSONObject botConfiguration = new JSONObject();

            JSONArray ytApiKeys = new JSONArray();
            ytApiKeys.put("Put as many YouTube API Keys as you want.");

            botConfiguration.put("youtubeApiKeys", ytApiKeys);

            rootObject.put("botConfiguration", botConfiguration);

            botConfiguration.put("prefix", ",");
            botConfiguration.put("author", "TheTHINGYEEEEE#6696");
            botConfiguration.put("token", "PUT YOUR BOT TOKEN HERE");
            botConfiguration.put("updatemode", false);

            FileWriter writer = new FileWriter(configFile);
            writer.write(JsonWriter.formatJson(rootObject.toString()));
            writer.flush();
            writer.close();

            throw new IOException("Setup \"config.json\" on your root directory.");
        }
    }

    public synchronized void initConfigurationFile() {
        try {
            contents = new String(Files.readAllBytes(Paths.get(configFile.getPath())));

            JSONObject jsonObject = new JSONObject(contents);

            if(jsonObject.has("botConfiguration")) {
                JSONObject configuration = jsonObject.getJSONObject("botConfiguration");
                if(configuration.has("token") &&
                        configuration.has("prefix") &&
                        configuration.has("author") &&
                        configuration.has("youtubeApiKeys") &&
                        configuration.has("updatemode")) {
                    botToken = configuration.getString("token");
                    botPrefix = configuration.getString("prefix");
                    botAuthor = configuration.getString("author");
                    updateMode = configuration.getBoolean("updatemode");
                }
            } else {
                throw new IOException("Setup \"config.json\" or delete file and re-run to reset.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getBotToken() {
        return botToken;
    }

    public String getBotPrefix() {
        return botPrefix;
    }

    public String getBotAuthor() {
        return botAuthor;
    }

    public boolean isUpdateMode() {
        return updateMode;
    }

    public ArrayList<String> getYoutubeKeys() throws IOException {
        JSONObject config = new JSONObject(contents);
        if(config.has("botConfiguration")) {
            JSONObject botConfig = config.getJSONObject("botConfiguration");
            if(botConfig.has("youtubeApiKeys")) {
                JSONArray apiKeys = new JSONArray(botConfig.getJSONArray("youtubeApiKeys"));
                if(apiKeys.isEmpty()) {
                    throw new IOException("YouTube API Keys array empty on \"config.json\"");
                }

                ArrayList<String> ytApiKeys = new ArrayList<>();
                for(int i = 0; i < apiKeys.length(); i++) {
                    ytApiKeys.add(apiKeys.getString(i));
                }

                return ytApiKeys;
            }
        }
        throw new IOException("\"Bot Configuration not found.\"");
    }
}
