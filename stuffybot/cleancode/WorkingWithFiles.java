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

import com.cedarsoftware.util.io.JsonWriter;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

public class WorkingWithFiles {

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void saveFile(ArrayList<TextChannel> channels, Guild guild) throws URISyntaxException, IOException {

        String jsonFile;

        try {
            jsonFile = new String((Files.readAllBytes(Paths.get(getJarFile() + "/textChannels.json"))));
            JSONObject jsonObject1 = new JSONObject(jsonFile);
            if(!jsonObject1.has("textChannels")) {
                JSONObject Object1 = new JSONObject();
                JSONArray textArr = new JSONArray();
                JSONObject Object2 = new JSONObject();

                JSONArray textChanArr = new JSONArray();

                for(TextChannel channel : channels) {
                    textChanArr.put(channel.getId());
                }

                Object2.put(guild.getId(), textChanArr);
                textArr.put(Object2);
                Object1.put("textChannels", textArr);

                File file = new File(getJarFile() + "/textChannels.json");
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    FileWriter writer = new FileWriter(getJarFile() + "/textChannels.json");
                    writer.write(JsonWriter.formatJson(Object1.toString()));
                    writer.flush();
                    writer.close();
//                    textChanArr = new JSONArray(new ArrayList<String>());
                }
            } else {
                JSONArray textArr = jsonObject1.getJSONArray("textChannels");
                JSONObject jsonObject2 = textArr.getJSONObject(0); // guilds

                JSONArray textChanArr = new JSONArray();

                for (TextChannel channel : channels) {
                    textChanArr.put(channel.getId());
                }

                jsonObject2.put(guild.getId(), textChanArr);

                File file = new File(getJarFile() + "/textChannels.json");
                if (!file.exists()) {
                    file.createNewFile();
                } else {
                    FileWriter writer = new FileWriter(getJarFile() + "/textChannels.json");
                    writer.write(JsonWriter.formatJson(jsonObject1.toString()));
                    writer.flush();
                    writer.close();
//                    textChanArr = new JSONArray(new ArrayList<String>());
                }
            }

        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }

    public static File getJarFile() throws URISyntaxException {
        return new File(System.getProperty("user.dir"));
    }
}
