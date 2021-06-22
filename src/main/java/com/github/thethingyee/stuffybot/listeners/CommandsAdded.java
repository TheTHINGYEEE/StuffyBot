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

import com.github.natanbc.lavadsp.lowpass.*;
import com.github.thethingyee.stuffybot.*;
import com.sedmelluq.discord.lavaplayer.player.*;
import com.sedmelluq.discord.lavaplayer.track.*;
import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.guild.*;
import net.dv8tion.jda.api.hooks.*;
import org.json.*;
import org.jsoup.*;
import org.jsoup.nodes.*;

import java.io.*;
import java.net.*;
import java.util.*;

import static com.github.thethingyee.stuffybot.StuffyBot.*;
import static com.github.thethingyee.stuffybot.cleancode.WorkingWithFiles.*;

public class CommandsAdded extends ListenerAdapter {

    private final StuffyBot stuffyBot;
    private boolean filterOnline = false;

    public static HashMap<Guild, Message> audioPlayerActive = new HashMap<>();

    String youtubeApiKey = null;

    public CommandsAdded(StuffyBot stuffyBot) {
        this.stuffyBot = stuffyBot;
        try {
            logger.info("Verifying youtube API key...");
            youtubeApiKey = stuffyBot.getBotConfig().getYoutubeKeys().get(apiKeyPass(0));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int apiKeyPass(int startIndex) throws IOException {

        ArrayList<String> youtubeKeys = stuffyBot.getBotConfig().getYoutubeKeys();

        for(int i = startIndex; i < youtubeKeys.size(); i++) {
            try {
                logger.info("Checking api key: index of " + i + " out of " + youtubeKeys.size());
                String apiKey = youtubeKeys.get(i);
                String url = "https://www.googleapis.com/youtube/v3/search?regionCode=US&type=video&maxResults=1&order=relevance&q=me+at+the+zoo&key=" + apiKey;
                Document doc = Jsoup.connect(url).timeout(15 * 1000).ignoreContentType(true).get();
                doc.text();
                logger.info("API Key: index of " + i + " SUCCESS!");
                return i;
            } catch(HttpStatusException | SocketTimeoutException e) {
                logger.warning("API Key: index of " + i + " FAILED!");
            }
        }

        logger.warning("No API keys has passed the request test.");
        logger.warning("Exiting...");
        System.exit(403);
        return 0;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {

        String[] command = event.getMessage().getContentRaw().split(" ");

        if (botChannels.containsKey(event.getGuild())) {

            if (botChannels.get(event.getGuild()).contains(event.getChannel())) {
                if (command[0].equalsIgnoreCase( prefix + "play")) {
                    if (!StuffyBot.isUrl(command[1])) {
                        event.getChannel().sendMessage("Loading track...").queue();
                        StringBuilder stringBuilder = new StringBuilder();

                        for (int i = 1; i < command.length; i++) {
                            stringBuilder.append(command[i]).append(" ");
                        }
                        String searchyt = stringBuilder.toString();
                        searchyt = searchyt
                                .replace("+", "%2B")
                                .replace(" ", "+")
                                .replace("'", "%27")
                                .replace("(", "%28")
                                .replace(")", "%29")
                                .replace("&", "%26")
                                .replace(";", "%3B")
                                .replace(":", "%3A")
                                .replace("/", "%2F");

                        String url = "https://www.googleapis.com/youtube/v3/search?regionCode=US&type=video&maxResults=1&order=relevance&q=" + searchyt + "&key=" + youtubeApiKey;

                        try {
                            Document doc = Jsoup.connect(url).timeout(20 * 1000).ignoreContentType(true).get();
                            String jsonFormat = doc.text().replace(" ", "");
                            JSONObject jsonObject = new JSONObject(jsonFormat);
                            JSONArray jsonArray = jsonObject.getJSONArray("items");
                            JSONObject item = jsonArray.getJSONObject(0);
                            JSONObject results = item.getJSONObject("id");

                            if(results.has("videoId")) {
                                String topResult = results.getString("videoId");
                                String finalUrl = "https://youtube.com/watch?v=" + topResult;

                                stuffyBot.loadAndPlay(event.getChannel(), finalUrl, event.getMember());
                            } else {
                                logger.severe("Warning: Video ID tag not found. URL:\n" + url);
                            }

                        } catch(HttpStatusException e) {
                            event.getChannel().sendMessage("Failed to fetch track (Code " + e.getStatusCode() + ")").queue();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        stuffyBot.loadAndPlay(event.getChannel(), command[1], event.getMember());
                    }

                } else if (command[0].equalsIgnoreCase(prefix + "skip") || command[0].equalsIgnoreCase(prefix + "next")) {
                    if (botChannels.containsKey(event.getGuild())) {
                        stuffyBot.skipTrack(event.getChannel());
                    } else {
                        event.getChannel().sendMessage("You need to set a music channel first! Usage:" + prefix + "setchannel <channelid>").queue();
                    }
                } else if (command[0].equalsIgnoreCase(prefix + "volume")) {
                    if(command.length == 1) {
                        int vol = stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getVolume();
                        event.getChannel().sendMessage("The volume is on " + vol + "%").queue();
                        return;
                    }
                    try {
                        int num = Integer.parseInt(command[1]);
                        if ((num <= 200) && (num >= 0)) {
                            stuffyBot.getGuildAudioPlayer(event.getGuild()).player.setVolume(num);
                            event.getChannel().sendMessage("The volume is now set to " + num).queue();
                        } else {
                            event.getChannel().sendMessage("The volume needs to be 0-200!").queue();
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        event.getChannel().sendMessage("You need to input an integer!").queue();
                    }
                } else if(command[0].equalsIgnoreCase(prefix + "bass")) {
                    try {
                        int num = Integer.parseInt(command[1]);

                        if ((num <= 150) && (num >= 0)) {
                            stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.bassBoost(num, event.getGuild().getName(), event.getChannel());
                            event.getChannel().sendMessage("The bass is now set to " + num + "%\n" + "The effect will take place in a few seconds.").queue();
                        } else {
                            event.getChannel().sendMessage("The bass percentage can only be 0-150%").queue();
                        }
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        event.getChannel().sendMessage("You need to input an integer!").queue();
                    }
                } else if(command[0].equalsIgnoreCase(prefix + "pos")) {
                    AudioTrack track = stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack();
                    if(track != null) {
                        if(command.length == 1) {
                            event.getChannel().sendMessage("Current duration at the moment: " + track.getPosition() / 1000 + "seconds.").queue();
                            return;
                        }
                        try {
                            int num = Integer.parseInt(command[1]);
                            int converted = (int) track.getDuration() / 1000;

                            if(num > converted) {
                                event.getChannel().sendMessage("Can't change position: Specified value more than track's duration.").queue();
                            } else {
                                stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack().setPosition(num * 1000L);
                                event.getChannel().sendMessage("Successfully changed time position!").queue();
                            }
                        } catch(NumberFormatException e) {
                            e.printStackTrace();
                            event.getChannel().sendMessage("You need to input an integer.").queue();
                        }
                    } else {
                        event.getChannel().sendMessage("There are no playing tracks.").queue();
                    }
                } else if(command[0].equalsIgnoreCase(prefix + "forward")) {
                    AudioPlayer player = stuffyBot.getGuildAudioPlayer(event.getGuild()).player;
                    if(player.getPlayingTrack() != null) {
                        if(command.length <= 1) {
                            event.getChannel().sendMessage("Argument needed.").queue();
                            return;
                        }
                        try {
                            int num = Integer.parseInt(command[1]) * 1000;
                            int forwardDuration = (int) player.getPlayingTrack().getPosition() + num;
                            if(forwardDuration >= player.getPlayingTrack().getDuration()) {
                                event.getChannel().sendMessage("Can't forward: Specified time more than track's duration.").queue();
                            } else {
                                player.getPlayingTrack().setPosition(forwardDuration);
                                event.getChannel().sendMessage("Track's position set to " + forwardDuration/1000 + " seconds").queue();
                            }
                        } catch(NumberFormatException e) {
                            e.printStackTrace();
                            event.getChannel().sendMessage("You need to input an integer.").queue();
                        }
                    } else {
                        event.getChannel().sendMessage("There are no playing tracks.").queue();
                    }
                } else if (command[0].equalsIgnoreCase(prefix + "rewind")) {
                    AudioPlayer player = stuffyBot.getGuildAudioPlayer(event.getGuild()).player;
                    if(player.getPlayingTrack() != null) {
                        if(command.length <= 1) {
                            event.getChannel().sendMessage("Argument needed.").queue();
                            return;
                        }
                        try {
                            int num = Integer.parseInt(command[1]) * 1000;
                            int rewindDuration = (int) player.getPlayingTrack().getPosition() - num;
                            if(rewindDuration >= player.getPlayingTrack().getDuration()) {
                                event.getChannel().sendMessage("Can't forward: Specified time more than track's duration.").queue();
                            } else {
                                player.getPlayingTrack().setPosition(rewindDuration);
                                event.getChannel().sendMessage("Track's position set to " + rewindDuration/1000 + " seconds").queue();
                            }
                        } catch(NumberFormatException e) {
                            e.printStackTrace();
                            event.getChannel().sendMessage("You need to input an integer.").queue();
                        }
                    } else {
                        event.getChannel().sendMessage("There are no playing tracks.").queue();
                    }
                } else if (command[0].equalsIgnoreCase(prefix + "pause")) {
                    if (!this.stuffyBot.getGuildAudioPlayer(event.getGuild()).player.isPaused()) {
                        this.stuffyBot.getGuildAudioPlayer(event.getGuild()).player.setPaused(true);
                        event.getChannel().sendMessage("Audio player paused.").queue();
                    } else {
                        event.getChannel().sendMessage("The audio player is already paused!").queue();
                    }
                } else if (command[0].equalsIgnoreCase(prefix + "resume")) {
                    if (this.stuffyBot.getGuildAudioPlayer(event.getGuild()).player.isPaused()) {
                        this.stuffyBot.getGuildAudioPlayer(event.getGuild()).player.setPaused(false);
                        event.getChannel().sendMessage("Audio player resumed.").queue();
                    } else {
                        event.getChannel().sendMessage("The audio player is already playing!").queue();
                    }
                } else if (command[0].equalsIgnoreCase(prefix + "remove")) {
                    int num = Integer.parseInt(command[1]);
                    stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.removeTrack(num, event.getChannel());
                } else if (command[0].equalsIgnoreCase(prefix + "disconnect")) {
                    if (event.getGuild().getAudioManager().isConnected()) {
                        event.getGuild().getAudioManager().closeAudioConnection();
                        this.stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.clearQueue(event.getGuild());
                        event.getChannel().sendMessage("Successfully disconnected out of voice channel.").queue();
                    } else {
                        event.getChannel().sendMessage("Can't disconnect. The bot is not connected to any voice channels.").queue();
                    }
                } else if(command[0].equalsIgnoreCase(prefix + "clearqueue")) {
                    if(!this.stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.getQueue().isEmpty()) {
                        this.stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.clearQueue(event.getGuild());
                        event.getChannel().sendMessage("The queue has been cleared.").queue();
                    } else {
                        event.getChannel().sendMessage("The queue is already empty!").queue();
                    }
                } else if (command[0].equalsIgnoreCase(prefix + "playing")) {
                    if (stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() != null) {
                        event.getChannel().sendMessage("Now playing: " + stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack().getInfo().title).queue();
                    } else {
                        event.getChannel().sendMessage("There is nothing playing.").queue();
                    }
                } else if (command[0].equalsIgnoreCase(prefix + "queue")) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setFooter(stuffyBot.getVersion() + " / " + stuffyBot.getBotConfig().getBotAuthor());
                    embed.setColor(event.getGuild().getSelfMember().getColor());
                    embed.setTitle(stuffyBot.botName + " Queue");
                    ArrayList<AudioTrack> conversion = new ArrayList<>(stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.getQueue());
                    if (stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() != null) {
                        embed.addField("Now playing", stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack().getInfo().title, true);
                        if (conversion.isEmpty()) {
                            embed.setDescription("There is currently nothing in the queue");
                        } else {

                            StringBuilder builder = new StringBuilder();
                            for (int j = 0; j < conversion.size(); j++) {
                                builder.append(j + 1).append(". ").append(conversion.get(j).getInfo().title).append("\n");
                            }
                            embed.addField("Queued songs:", builder.toString(), false);
                        }
                        event.getChannel().sendMessage(embed.build()).queue();
                    } else {
                        event.getChannel().sendMessage("There is nothing playing.").queue();
                    }
                } else if (command[0].equalsIgnoreCase(prefix + "help")) {
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setColor(event.getGuild().getSelfMember().getColor());
                    builder.setDescription("Please tell the bot owner if there's any bugs :)");
                    builder.setTitle(stuffyBot.botName + " Help");
                    builder.addField("Commands:", "1. " + prefix + "play <search query> - Play any YouTube video and listen to it.\n" +
                            "2. " + prefix + "play <url> - Play any music from a url. Supports YouTube, Bandcamp, Twitch, and Vimeo.\n" +
                            "3. " + prefix + "pause - Pause the audio player.\n" +
                            "4. " + prefix + "resume - Resumes the audio player.\n" +
                            "5. " + prefix + "queue - See what's on the queue.\n" +
                            "6. " + prefix + "playing - See what's playing right now.\n" +
                            "7. " + prefix + "volume <number> - Sets the audio player's volume.\n" +
                            "8. " + prefix + "player - Shows the audio player to easily interact with the bot.\n" +
                            "9. " + prefix + "bass <percentage> - Sets the bass, either to boost or decrease.\n" +
                            "10. " + prefix + "pos <seconds> - Changes the position of the current track playing in seconds.\n" +
                            "11. " + prefix + "forward <seconds> - Forwards the position of the current track playing in seconds.\n" +
                            "12. " + prefix + "rewind <seconds> - Rewinds the position of the current track playing in seconds.\n" +
                            "13. " + prefix + "clearqueue - Clears the whole queued songs.\n" +
                            "14. " + prefix + "disconnect - Makes the bot disconnect to the voice channel it's connected to.", true);
                    builder.setFooter(stuffyBot.getVersion() + " / " + stuffyBot.getBotConfig().getBotAuthor());
                    event.getChannel().sendMessage(builder.build()).queue();
                } else if(command[0].equalsIgnoreCase(prefix + "player")) {
                    if(audioPlayerActive.containsKey(event.getGuild())) {
                        audioPlayerActive.get(event.getGuild()).delete().queue();
                        audioPlayerActive.remove(event.getGuild());
                    }
                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle(stuffyBot.botName + " Player");
                    builder.setDescription("Press ⏸ to pause.\n" +
                            "Press ▶️ to play.\n" +
                            "Press ⏭ to skip a song.");
                    builder.setFooter(stuffyBot.getVersion() + " / " + stuffyBot.getBotConfig().getBotAuthor());

                    boolean addPlayButton = false;
                    boolean addNextButton;
                    boolean addPauseButton = false;

                    if(stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() != null) {
                        builder.addField("Now playing:", stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack().getInfo().title, true);
                        builder.addField("# of queued songs:", stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.getQueue().size() + "", true);
                    } else {
                        builder.addField("Now playing:", "Empty", true);
                        builder.addField("# of queued songs:", "Empty", true);
                    }
                    addNextButton = !stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.getQueue().isEmpty();
                    if(stuffyBot.getGuildAudioPlayer(event.getGuild()).player.isPaused()) {
                        if(stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() == null) {
                            builder.addField("Status:", "No music playing.", false);
                            addPlayButton = false;
                        } else {
                            builder.addField("Status:", "Paused", false);
                            addPlayButton = true;
                        }
                        addPauseButton = false;
                    } else {
                        if(stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() == null) {
                            builder.addField("Status:", "No music playing", false);
                        } else {
                            builder.addField("Status:", "Playing", false);
                            addPlayButton = false;
                            addPauseButton = true;
                        }
                    }
                    boolean finalAddPlayButton = addPlayButton;
                    boolean finalAddPauseButton = addPauseButton;
                    boolean finalAddNextButton = addNextButton;
                    event.getChannel().sendMessage(builder.build()).queue(message -> {
                        if(finalAddPlayButton) {
                            message.addReaction("▶️").queue();
                        }
                        if(finalAddPauseButton) {
                            message.addReaction("⏸").queue();
                        }
                        if(finalAddNextButton) {
                            message.addReaction("⏭").queue();
                        }
                        audioPlayerActive.put(event.getGuild(), message);
                    });
                } else if(command[0].equalsIgnoreCase(prefix + "loop") || command[0].equalsIgnoreCase(prefix + "repeat")) {
                    TrackScheduler scheduler = stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler;
                    stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.setRepeating(scheduler.isRepeating());
                    if(scheduler.isRepeating()) {
                        event.getChannel().sendMessage("Song now set to NOT repeating.").queue();
                    } else {
                        event.getChannel().sendMessage("Song now set to repeating.").queue();
                    }
                } else if(command[0].equalsIgnoreCase(prefix + "tdo")) {
                    float smooth = Float.parseFloat(command[1]);
                    if(filterOnline) {
                        stuffyBot.getGuildAudioPlayer(event.getGuild()).player.setFilterFactory(null);
                        filterOnline = false;
                    }
                    stuffyBot.getGuildAudioPlayer(event.getGuild()).player.setFilterFactory((track, format, output) -> {
                        LowPassPcmAudioFilter audioFilter = new LowPassPcmAudioFilter(output, format.channelCount);
                        audioFilter.setSmoothing(200.0f);
                        return Collections.singletonList(audioFilter);
                    });
                    event.getChannel().sendMessage("Set the thing to " + smooth).queue();
                    filterOnline = true;
                }
            }
        }
        if (command[0].equalsIgnoreCase(prefix + "setchannel")) {
            if(Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) {
                if (!botChannels.containsKey(event.getGuild())) {

                    String channelId = (!command[1].equalsIgnoreCase("this")) ? command[1] : event.getChannel().getId();

                    if (event.getGuild().getTextChannelById(channelId) != null) {
                        stuffyBot.channels.add(event.getGuild().getTextChannelById(channelId));
                        botChannels.put(event.getGuild(), stuffyBot.channels);
                        try {
                            saveFile(botChannels.get(event.getGuild()), event.getGuild());
                        } catch (URISyntaxException | IOException e) {
                            e.printStackTrace();
                        }
                        event.getChannel().sendMessage("Successfully done! You can now use all the features.").queue();
                    }
                } else {
                    event.getChannel().sendMessage("Please use " + prefix + "addchannel to add more music channels!").queue();
                }
            } else {
                event.getChannel().sendMessage("You don't have the permission node MANAGE_CHANNELS.").queue();
            }
        }
        if (command[0].equalsIgnoreCase(prefix + "addchannel")) {
            if(Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) {
                if (botChannels.containsKey(event.getGuild())) {
                    if(botChannels.get(event.getGuild()).contains(event.getChannel())) {
                        if (event.getGuild().getTextChannelById(command[1]) != null) {
                            if (botChannels.get(event.getGuild()).contains(event.getGuild().getTextChannelById(command[1]))) {
                                event.getChannel().sendMessage("The channel is already on the added channels!").queue();
                            } else {
                                botChannels.get(event.getGuild()).add(event.getGuild().getTextChannelById(command[1]));
                                try {
                                    saveFile(botChannels.get(event.getGuild()), event.getGuild());
                                } catch (URISyntaxException | IOException e) {
                                    e.printStackTrace();
                                }
                                event.getChannel().sendMessage("Successfully added channel \"" + Objects.requireNonNull(event.getGuild().getTextChannelById(command[1])).getAsMention() + "\".").queue();
                            }
                        }
                    } else {
                        event.getChannel().sendMessage("Please set the first music channel by " + prefix + "setchannel").queue();
                    }
                }
            }
        }
        if(command[0].equalsIgnoreCase(prefix + "removechannel")) {
            ArrayList<TextChannel> channelArrayList = botChannels.get(event.getGuild());
            if (Objects.requireNonNull(event.getMember()).hasPermission(Permission.MANAGE_CHANNEL)) {
                if (botChannels.containsKey(event.getGuild())) {
                    if (channelArrayList.contains(event.getChannel())) {
                        if(channelArrayList.contains(event.getGuild().getTextChannelById(command[1]))) {
                            channelArrayList.remove(event.getGuild().getTextChannelById(command[1]));
                            if(channelArrayList.isEmpty()) botChannels.remove(event.getGuild());
                            try {
                                saveFile(botChannels.get(event.getGuild()), event.getGuild());
                            } catch (URISyntaxException | IOException e) {
                                e.printStackTrace();
                            }
                            event.getChannel().sendMessage("Successfully removed channel " + Objects.requireNonNull(event.getGuild().getTextChannelById(command[1])).getAsMention() + // 420 lamo
                                    " from " + stuffyBot.botName + "'s database!").queue();
                        } else {
                            event.getChannel().sendMessage("The Channel ID specified doesn't exists on " + stuffyBot.botName + "'s database.").queue();
                        }
                    }
                } else {
                    event.getChannel().sendMessage("Please set the first music channel by " + prefix + "setchannel").queue();
                }
            } else {
                event.getChannel().sendMessage("You don't have the permission node MANAGE_CHANNELS.").queue();
            }
        }
        super.onGuildMessageReceived(event);
    }
}
