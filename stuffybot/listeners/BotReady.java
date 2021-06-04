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

package com.github.thethingyee.stuffybot.listeners;

import com.cedarsoftware.util.io.JsonWriter;
import com.github.thethingyee.stuffybot.StuffyBot;
import com.github.thethingyee.stuffybot.cleancode.WorkingWithFiles;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Objects;

import static com.github.thethingyee.stuffybot.StuffyBot.jda;
import static com.github.thethingyee.stuffybot.StuffyBot.logger;

public class BotReady extends ListenerAdapter {
    private final StuffyBot stuffyBot;

    public BotReady(StuffyBot stuffyBot) {
        this.stuffyBot = stuffyBot;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Loading settings...");
        stuffyBot.botName = event.getJDA().getSelfUser().getName();
        try {
            File file = new File(WorkingWithFiles.getJarFile() + "/textChannels.json");
            if (!file.exists()) {
                file.createNewFile();
                FileWriter writer = new FileWriter(WorkingWithFiles.getJarFile() + "/textChannels.json");
                writer.write(JsonWriter.formatJson("{\"textChannels\":[{}]}"));
                writer.flush();
                writer.close();
                logger.info("File textChannels.json not found. \nCreated new file.");
            } else {
                String jsonFile;
                try {
                    jsonFile = new String((Files.readAllBytes(Paths.get(WorkingWithFiles.getJarFile() + "/textChannels.json"))));
                    JSONObject parsed = new JSONObject(jsonFile);
                    JSONArray textArr = parsed.getJSONArray("textChannels");
                    JSONObject obj1 = textArr.getJSONObject(0); // guilds
                    StringBuilder stringBuilder = new StringBuilder();
                    // obj1.length() returns the amount of guilds register onto the json file.
                    for (Guild guild : jda.getGuilds()) {
                        stringBuilder.append(guild.getName()).append(", ");
                        try {
                            boolean arrr = obj1.has(guild.getId());

                            if (!arrr) {
                                logger.warning("Guild \"" + Objects.requireNonNull(jda.getGuildById(guild.getId())).getName() + "\" hasn't set their music channel yet!");
                            } else {
                                ArrayList<TextChannel> channels = new ArrayList<>();
                                for (int j = 0; j < obj1.getJSONArray(guild.getId()).length(); j++) {
                                    channels.add(
                                            guild.getTextChannelById(obj1.getJSONArray(guild.getId()).getString(j)));
                                }
                                StuffyBot.botChannels.put(jda.getGuildById(guild.getId()), channels);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    logger.info("The bot is on: " + stringBuilder.toString());
                    logger.info(StuffyBot.botChannels.toString());
                } catch (IOException | URISyntaxException e) {
                    e.printStackTrace();
                }

            }
        } catch(IOException | URISyntaxException ex) {
            ex.printStackTrace();
        }

        String botUser = event.getJDA().getSelfUser().getName() + "#" + event.getJDA().getSelfUser().getDiscriminator();
        logger.info("Logged in as " + botUser);
    }
}
