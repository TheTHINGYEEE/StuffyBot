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

package com.github.thethingyee.stuffybot;

import com.github.thethingyee.stuffybot.cleancode.*;
import com.github.thethingyee.stuffybot.libraries.ColorThief;
import com.github.thethingyee.stuffybot.listeners.*;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.vimeo.VimeoAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

@SpringBootApplication
public class StuffyBot extends ListenerAdapter {

    /*
    TODO:
      1. Refactor code.
      2. Fix a lot of bugs.
      3. Make the code understandable.
      4. Add multiple YouTube API Keys.
      6. Make code more neat.
      9. Make code more clean.
      4. Make code crisp.
      2. Make code precise.
      0. Make code cool.
      :)
     */

    // DECLARATIONS

    // Public JDA Object
    public static JDA jda;

    // Also considered as Music Channels on the Guild itself.
    public static HashMap<Guild, ArrayList<TextChannel>> botChannels = new HashMap<>();

    // Don't need to mind this.
    // It is just the current status index of the bot.
    private static int currentStatus = 0;

    // To call the bot.
    public static String prefix = ""; // Prefix set by config

    public static String token = ""; // Token set by config

    public static String author = ""; // Author set by config

    // Version of the bot.
    public String version = "v2.7.2"; // Version set by config, probably, idk.

    // Name of the bot without any discriminators.
    public String botName; // idk

    public BotConfig botConfig;

    public static int guildsPlaying;

    public static void main(String[] args) throws Exception {

        // Call in one single main class instance.
        // To use it in all classes who needs the main class instance.
        StuffyBot stuffyBot = new StuffyBot();

        stuffyBot.botConfig = new BotConfig(new File(WorkingWithFiles.getJarFile() + "/config.json"));
        stuffyBot.botConfig.initConfigurationFile();
        prefix = stuffyBot.botConfig.getBotPrefix();
        token = stuffyBot.botConfig.getBotToken();
        author = stuffyBot.botConfig.getBotAuthor();

        // Deletes the method logger and date logger.
        ConsoleHandler handler = new ConsoleHandler();
        handler.setFormatter(new DumbFormatter());
        for (int asd = 0; asd < logger.getHandlers().length; asd++) {
            logger.removeHandler(logger.getHandlers()[asd]);
        }
        logger.setUseParentHandlers(false);
        logger.addHandler(handler);

        // Update mode to tell if the bot is updating..
        boolean updateMode = stuffyBot.botConfig.isUpdateMode();

        // Array of statuses that will be cycling through the bot's status.
        Activity[] statuses = {
                Activity.competing("Stuffy with other bots."),
                Activity.playing("on version " + stuffyBot.getVersion()),
                Activity.watching(prefix + "help"),
                Activity.watching("ThingyTV suffer from coding me."),
                Activity.listening("the CPU fan."),
                Activity.playing("on Singapore's servers!"),
                Activity.watching("YouTube for search results."),
                Activity.watching("BandCamp for AMAZING music tracks!"),
                Activity.watching("audio from Twitch Streams!"),
                Activity.watching("Vimeo to play music :O")
        };

        // If update mode is on, activate the beta bot, not the main bot.
        // If update mode is off, activate the main bot, not the beta bot.

        String updateModeMsg = updateMode ? "Update mode is on" : "Update mode is off.";

        logger.info(updateModeMsg);

        // Starting up the bot..
        logger.info("Starting up...");

//        jda = JDABuilder.create(token, GUILD_MESSAGES, GUILD_VOICE_STATES, GUILD_MESSAGE_REACTIONS)
//                .setStatus(OnlineStatus.ONLINE)
//                .disableCache(CacheFlag.ACTIVITY, CacheFlag.EMOTE, CacheFlag.CLIENT_STATUS)
//                .build();

        // Activate the bot which has the token.
        jda = JDABuilder.createDefault(token).build();

        // Register listeners.
        logger.info("Loading listeners...");
        jda.addEventListener(new AddReaction(stuffyBot));
        jda.addEventListener(stuffyBot);
        jda.addEventListener(new CommandsAdded(stuffyBot));
        jda.addEventListener(new BotReady(stuffyBot));
        jda.addEventListener(new GuildVoice(stuffyBot));
        jda.addEventListener(new AdditionalCommands(stuffyBot));


        // Loads additional features to the bot that aren't that necessary.
        logger.info("Loading additional features...");
        if (!updateMode) {
            new Timer().schedule(new TimerTask() {
                public void run() {
                    jda.getPresence().setActivity(statuses[currentStatus]);
                    currentStatus = (currentStatus + 1) % statuses.length;
                }
            }, 0, 20 * 1000);
        } else {
            jda.getPresence().setActivity(Activity.competing("programming for updates."));
        }

        // Method to start up spring web.
        logger.info("Starting up Spring Boot...");
        SpringApplication.run(StuffyBot.class, args);

        // Loads the command line for command line commands.
        logger.info("Loading command line...");
        Scanner scanner = new Scanner(System.in);
        while (true) {
            String scanned = scanner.nextLine();
            if (scanned.equalsIgnoreCase("stop") || scanned.equalsIgnoreCase("end")) {
                logger.info("Ending all process...");
                jda.shutdown();
                System.exit(69);
            }
        }
    }

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    public final ArrayList<TextChannel> channels = new ArrayList<>();

    public static Logger logger = Logger.getLogger(StuffyBot.class.getName());

    public StuffyBot() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new BandcampAudioSourceManager());
        playerManager.registerSourceManager(new VimeoAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    public static boolean isUrl(String link) {
        return (link.contains("https://") || link.contains("http://"));
    }

    public String getVersion() {
        return version;
    }

    public synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    public void loadAndPlay(final TextChannel channel, final String trackUrl, Member member) {
        final GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                if (Objects.requireNonNull(member.getVoiceState()).getChannel() != null) {
                    try {
                        logger.info("Source of audioTrack: " + track.getSourceManager().getSourceName());
                        switch (track.getSourceManager().getSourceName()) {
                            case "youtube": {
                                String id = track.getInfo().uri.replace("https://www.youtube.com/watch?v=", "");
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setFooter(version + " / " + getBotConfig().getBotAuthor());

                                builder.setTitle("Track added.", track.getInfo().uri);
                                builder.addField("Track name:", track.getInfo().title, false);
                                builder.addField("Video ID:", id, true);

                                URL thumbnailUrl = new URL("https://i.ytimg.com/vi/" + id + "/mqdefault.jpg");
                                BufferedImage thumbnail = ImageIO.read(thumbnailUrl);
                                int[] rgbArr = ColorThief.getColor(thumbnail);
                                Color color = new Color(rgbArr[0], rgbArr[1], rgbArr[2]);
                                builder.setColor(color);

                                // Used to set the image with the video's thumbnail.
                                builder.setImage("https://i.ytimg.com/vi/" + id + "/mqdefault.jpg");

                                channel.sendMessage(builder.build()).queue();
                                logger.info("Queued '" + track.getInfo().title + "' to guild '" + channel.getGuild().getName() + "'");
                                play(channel.getGuild(), musicManager, track, member, channel);
                                break;
                            }
                            case "bandcamp": {
                                String thumbnailURI = "https://cdn.thingyservers.xyz/images/bandcamp-logo.png";
                                logger.info(track.getInfo().uri);
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setFooter(version + " / " + getBotConfig().getBotAuthor());

                                builder.setTitle("Track added.", track.getInfo().uri);
                                builder.addField("Track name:", track.getInfo().title, false);
                                builder.addField("Music ID:", "null", true);

                                URL thumbnailUrl = new URL(thumbnailURI);
                                BufferedImage thumbnail = ImageIO.read(thumbnailUrl);
                                int[] rgbArr = ColorThief.getColor(thumbnail);
                                Color color = new Color(rgbArr[0], rgbArr[1], rgbArr[2]);
                                builder.setColor(color);

                                // Used to set the image with the video's thumbnail.
                                builder.setImage(thumbnailUrl.getHost());

                                channel.sendMessage(builder.build()).queue();
                                logger.info("Queued '" + track.getInfo().title + "' to guild '" + channel.getGuild().getName() + "'");
                                play(channel.getGuild(), musicManager, track, member, channel);
                                break;
                            }
                            case "twitch": {
                                String thumbnailURI = "https://cdn.thingyservers.xyz/images/twitch-logo.png";

                                logger.info(track.getInfo().uri);
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setFooter(version + " / " + getBotConfig().getBotAuthor());

                                builder.setTitle("Track added.", track.getInfo().uri);
                                builder.addField("Track name:", track.getInfo().title, false);
                                builder.addField("Music ID:", "null", true);

                                URL thumbnailUrl = new URL(thumbnailURI);
                                BufferedImage thumbnail = ImageIO.read(thumbnailUrl);
                                int[] rgbArr = ColorThief.getColor(thumbnail);
                                Color color = new Color(rgbArr[0], rgbArr[1], rgbArr[2]);
                                builder.setColor(color);

                                // Used to set the image with the video's thumbnail.
                                builder.setImage(thumbnailURI);

                                channel.sendMessage(builder.build()).queue();
                                logger.info("Queued '" + track.getInfo().title + "' to guild '" + channel.getGuild().getName() + "'");
                                play(channel.getGuild(), musicManager, track, member, channel);
                                break;
                            }
                            case "local": {
                                String thumbnailURI = "https://cdn.thingyservers.xyz/images/headphones-logo.png";

                                logger.info(track.getInfo().uri);
                                EmbedBuilder builder = new EmbedBuilder();
                                builder.setFooter(version + " / " + getBotConfig().getBotAuthor());

                                builder.setTitle("Track added.");
                                builder.addField("Track name:", track.getInfo().title, false);
                                builder.addField("Music ID:", "Local Audio", true);

                                URL thumbnailUrl = new URL(thumbnailURI);
                                BufferedImage thumbnail = ImageIO.read(thumbnailUrl);
                                int[] rgbArr = ColorThief.getColor(thumbnail);
                                Color color = new Color(rgbArr[0], rgbArr[1], rgbArr[2]);
                                builder.setColor(color);

                                // Used to set the image with the video's thumbnail.
                                builder.setImage(thumbnailURI);

                                channel.sendMessage(builder.build()).queue();
                                logger.info("Queued '" + track.getInfo().title + "' to guild '" + channel.getGuild().getName() + "'");
                                play(channel.getGuild(), musicManager, track, member, channel);
                                break;
                            }
                        }
                        guildsPlaying++;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                } else {
                    String id = track.getInfo().uri.replace("https://www.youtube.com/watch?v=", "");

                    EmbedBuilder builder = new EmbedBuilder();
                    builder.setTitle("Track not added.", track.getInfo().uri);
                    builder.setDescription("You need to be in a voice channel first!");
                    builder.addField("Track name:", track.getInfo().title, true);
                    builder.addField("Video ID:", id, true);
                    builder.setImage("https://i.ytimg.com/vi/" + id + "/mqdefault.jpg");
                    builder.setFooter(version + " / " + getBotConfig().getBotAuthor());
                    builder.setColor(Color.RED);
                    channel.sendMessage(builder.build()).queue();
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                if (playlist.getTracks() != null) {
                    logger.info("Loaded playlist! Tracks: " + playlist.getTracks().size() + " Guild: " + channel.getGuild().getName());
                    if (playlist.getTracks().size() <= 15) {
                        for (int i = 0; i < playlist.getTracks().size(); i++) {
                            play(channel.getGuild(), musicManager, playlist.getTracks().get(i), member, channel);
                        }
                        channel.sendMessage(playlist.getTracks().size() + " tracks added to queue.").queue();
                    } else {
                        channel.sendMessage("Sorry but you can only play 15 tracks on the playlist.").queue();
                        for (int i = 0; i < 14; i++) {
                            play(channel.getGuild(), musicManager, playlist.getTracks().get(i), member, channel);
                        }
                        channel.sendMessage("15 tracks added to queue.").queue();
                    }
                }

            }

            @Override
            public void noMatches() {
                channel.sendMessage("Nothing found by " + trackUrl).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                channel.sendMessage("Could not play: " + exception.getMessage()).queue();
                logger.log(Level.WARNING, "Could not play track: " + exception.getMessage() + "\nGuild name: " + channel.getGuild().getName());
            }
        });
    }

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, Member member, TextChannel channel) {
        connectToUserVoiceChannel(guild.getAudioManager(), member, channel);

        musicManager.scheduler.queue(track);
    }

    public void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
        logger.info("Skipped track. Guild name: " + channel.getGuild().getName());
    }

    private void connectToUserVoiceChannel(AudioManager audioManager, Member member, TextChannel channel) {
        if (!audioManager.isConnected()) {
            VoiceChannel connectedChannel = Objects.requireNonNull(member.getVoiceState()).getChannel();
            if (connectedChannel == null) {
                channel.sendMessage("You need to connect to a voice channel first!").queue();
                this.getGuildAudioPlayer(channel.getGuild()).scheduler.clearQueue(channel.getGuild());
            } else {
                audioManager.openAudioConnection(connectedChannel);
                logger.info("Successfully opened Audio Connection. Guild name: " + audioManager.getGuild().getName());
            }
        }
    }

    public BotConfig getBotConfig() {
        return botConfig;
    }
}