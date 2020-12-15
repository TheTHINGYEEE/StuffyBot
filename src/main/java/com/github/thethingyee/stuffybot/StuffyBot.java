package com.github.thethingyee.stuffybot;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_MESSAGES;
import static net.dv8tion.jda.api.requests.GatewayIntent.GUILD_VOICE_STATES;


public class StuffyBot extends ListenerAdapter {

    public static JDA jda;

    public static HashMap<Guild, TextChannel> botChannel = new HashMap<>();

    public static void main(String[] args) throws Exception {

        jda = JDABuilder.create("", GUILD_MESSAGES, GUILD_VOICE_STATES)
                .addEventListeners(new StuffyBot())
                .setActivity(Activity.watching("TheTHINGYEEEEE#1859 suffer"))
                .build();
    }

    private final AudioPlayerManager playerManager;
    private final Map<Long, GuildMusicManager> musicManagers;
    public static Logger logger = Logger.getLogger(StuffyBot.class.getName());

    public StuffyBot() {
        this.musicManagers = new HashMap<>();

        this.playerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(Guild guild) {
        long guildId = Long.parseLong(guild.getId());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }

        guild.getAudioManager().setSendingHandler(musicManager.getSendHandler());

        return musicManager;
    }

    @Override
    public void onGuildMessageReceived(GuildMessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ");

        if(botChannel.containsKey(event.getGuild())) {
            if(event.getChannel().equals(botChannel.get(event.getGuild()))) {
                if (command[0].equalsIgnoreCase("s!play")) {
                    if(botChannel.containsKey(event.getGuild())) {
                        if(!this.isUrl(command[1])) {
                            StringBuilder stringBuilder = new StringBuilder();

                            for(int i = 1; i < command.length; i++) {
                                stringBuilder.append(command[i] + " ");
                            }
                            String searchyt = stringBuilder.toString();
                            searchyt = searchyt.replace(" ", "+");

                            String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&regionCode=US&maxResults=1&order=relevance&q=" + searchyt + "&key=" + "AIzaSyB8kcP9p4Fx7OG17jv2CrvOimsF9AnHtvA";

                            try {
                                Document doc = Jsoup.connect(url).timeout(10 * 1000).ignoreContentType(true).get();
                                String jsonFormat = doc.text();
                                JSONObject jsonObject = new JSONObject(jsonFormat);
                                JSONArray jsonArray = jsonObject.getJSONArray("items");
                                JSONObject item = (JSONObject) jsonArray.get(0);
                                JSONObject results = item.getJSONObject("id");

                                String topResult = results.getString("videoId");
                                String finalUrl = "https://youtube.com/watch?v=" + topResult;

                                logger.info("Video id: " + topResult);
                                loadAndPlay(event.getChannel(), finalUrl, event.getMember());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        } else {
                            loadAndPlay(botChannel.get(event.getGuild()), command[1], event.getMember());
                        }
                    } else {
                        event.getChannel().sendMessage("You need to set a music channel first! Usage: s!setchannel <channelid>").queue();
                    }
                } else if (command[0].equalsIgnoreCase("s!skip")) {
                    if(botChannel.containsKey(event.getGuild())) {
                        skipTrack(botChannel.get(event.getGuild()));
                    } else {
                        event.getChannel().sendMessage("You need to set a music channel first! Usage: s!setchannel <channelid>").queue();
                    }
                } else if(command[0].equalsIgnoreCase("s!volume")) {
                    try {
                        int num = Integer.parseInt(command[1]);
                        if(!(num > 200) || !(num < 0)) {
                            getGuildAudioPlayer(event.getGuild()).player.setVolume(num);
                            event.getChannel().sendMessage("The volume is now set to " + num);
                        } else {
                            event.getChannel().sendMessage("The volume needs to be 0-200!").queue();
                        }
                    } catch(NumberFormatException e) {
                        e.printStackTrace();
                        event.getChannel().sendMessage("You need to input an integer!").queue();
                    }
                } else if(command[0].equalsIgnoreCase("s!pause")) {
                    if(!this.getGuildAudioPlayer(event.getGuild()).player.isPaused()) {
                        this.getGuildAudioPlayer(event.getGuild()).player.setPaused(true);
                        event.getChannel().sendMessage("Audio player paused.").queue();
                    } else {
                        event.getChannel().sendMessage("The audio player is already paused!").queue();
                    }
                } else if(command[0].equalsIgnoreCase("s!resume")) {
                    if(this.getGuildAudioPlayer(event.getGuild()).player.isPaused()) {
                        this.getGuildAudioPlayer(event.getGuild()).player.setPaused(false);
                        event.getChannel().sendMessage("Audio player resumed.").queue();
                    } else {
                        event.getChannel().sendMessage("The audio player is already playing!").queue();
                    }
                } else if(command[0].equalsIgnoreCase("s!testconsole")) {
                    getGuildAudioPlayer(event.getGuild()).scheduler.removeTrack(1);
                } else if(command[0].equalsIgnoreCase("s!remove")) {
                    int num = Integer.parseInt(command[1]);
                    getGuildAudioPlayer(event.getGuild()).scheduler.removeTrack(num);
                } else if(command[0].equalsIgnoreCase("s!disconnect")) {
                    if(event.getGuild().getAudioManager().isConnected()) {
                        event.getGuild().getAudioManager().closeAudioConnection();
                        event.getChannel().sendMessage("Successfully closed Audio Connection.").queue();
                    } else {
                        event.getChannel().sendMessage("Can't disconnect. Bot is not connected to any voice channels.").queue();
                    }
                } else if(command[0].equalsIgnoreCase("s!playing")) {
                    if(getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() != null) {
                        botChannel.get(event.getGuild()).sendMessage("Now playing: " + getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack().getInfo().title).queue();
                    } else {
                        event.getChannel().sendMessage("There is nothing playing.").queue();
                    }
                } else if(command[0].equalsIgnoreCase("s!queue")) {
                    EmbedBuilder embed = new EmbedBuilder();
                    embed.setTitle("Stuffy Queue");
                    ArrayList<AudioTrack> conversion = new ArrayList<>(getGuildAudioPlayer(event.getGuild()).scheduler.getQueue());
                    if(getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() != null) {
                        embed.addField("Now playing", getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack().getInfo().title, true);
                        if(conversion.isEmpty()) {
                            embed.setDescription("There is currently nothing in the queue");
                        } else {

                            StringBuilder builder = new StringBuilder();
                            for(int j = 0; j < conversion.size(); j++) {
                                builder.append((j+1) + ". " + conversion.get(j).getInfo().title + "\n");
                            }
                            embed.setDescription(builder.toString());
                        }
                        event.getChannel().sendMessage(embed.build()).queue();
                    } else {
                        event.getChannel().sendMessage("There is nothing playing.").queue();
                    }
                }
            }
        }
        if(command[0].equalsIgnoreCase("s!setchannel")) {
            if(!botChannel.containsKey(event.getGuild())) {
                botChannel.put(event.getGuild(), event.getGuild().getTextChannelById(command[1]));
                event.getChannel().sendMessage("Successfully done! You can now use all the features.").queue();
            } else {
               event.getChannel().sendMessage("You have already set your music channel.").queue();

            }
        }

        super.onGuildMessageReceived(event);
    }

    private void loadAndPlay(final TextChannel channel, final String trackUrl, Member member) {
        final GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                channel.sendMessage("Adding to queue " + track.getInfo().title).queue();
                logger.info("Queued '" + track.getInfo().title + "' to guild '" + channel.getGuild().getName() + "'");
                play(channel.getGuild(), musicManager, track, member);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                channel.sendMessage("Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")").queue();
                logger.info("Queued playlist '" + playlist.getName() + "' to guild '" + channel.getGuild().getName() + "'");

                play(channel.getGuild(), musicManager, firstTrack, member);

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

    private void play(Guild guild, GuildMusicManager musicManager, AudioTrack track, Member member) {
        connectToUserVoiceChannel(guild.getAudioManager(), member);

        musicManager.scheduler.queue(track);
    }

    private void skipTrack(TextChannel channel) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();

        channel.sendMessage("Skipped to next track.").queue();
        logger.info("Skipped track. Guild name: " + channel.getGuild().getName());
    }

    private static void connectToUserVoiceChannel(AudioManager audioManager, Member member) {
        if (!audioManager.isConnected() && !audioManager.isAttemptingToConnect()) {
            VoiceChannel connectedChannel = member.getVoiceState().getChannel();
            if(connectedChannel == null) {
                botChannel.get(member.getGuild()).sendMessage("You need to connect to a voice channel first!").queue();
            } else {
                audioManager.openAudioConnection(connectedChannel);
                logger.info("Successfully opened Audio Connection. Guild name: " + audioManager.getGuild().getName());
            }
        }
    }
    public static boolean isUrl(String link) {
        return (link.contains("https://") || link.contains("http://"));
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        String botUser = event.getJDA().getSelfUser().getName() + "#" + event.getJDA().getSelfUser().getDiscriminator();
        logger.info("Logged in as " + botUser);
        super.onReady(event);
    }

}
