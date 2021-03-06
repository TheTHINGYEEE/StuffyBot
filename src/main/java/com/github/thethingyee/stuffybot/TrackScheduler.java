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

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import static com.github.thethingyee.stuffybot.StuffyBot.logger;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {

    private static final float[] BASS_BOOST = {0.15f, 0.14f, 0.13f, 0.14f, 0.05f, 0.01f, 0.02f, 0.03f, 0.04f, 0.05f, 0.06f, 0.07f, 0.08f, 0.09f, 0.1f};

    private boolean repeating = false;

    private final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;

    private EqualizerFactory equalizer;
    private int boostPercentage;


    /**
     * @param player The audio player this scheduler uses
     */
    public TrackScheduler(AudioPlayer player) {
        this.player = player;
        this.queue = new LinkedBlockingQueue<>();
    }

    /**
     * Add the next track to queue or play right away if nothing is in the queue.
     *
     * @param track The track to play or add to queue.
     */
    public void queue(AudioTrack track) {
        // Calling startTrack with the noInterrupt set to true will start the track only if nothing is currently playing. If
        // something is playing, it returns false and does nothing. In that case the player was already playing so this
        // track goes to the queue instead.
        if (!player.startTrack(track, true)) {
            queue.offer(track);
        }
    }

    /**
     * Start the next track, stopping the current one if it is playing.
     */
    public void nextTrack() {
        // Start the next track, regardless of if something is already playing or not. In case queue was empty, we are
        // giving null to startTrack, which is a valid argument and will simply stop the player.
        if(queue.isEmpty()) {
            player.destroy();
        } else {
            player.startTrack(queue.poll(), false);
            for(int i = 0; i < queue.size(); i++) {
                StuffyBot.guildsPlaying++;
            }
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            if(repeating) {
                player.startTrack(track.makeClone(), false);
            } else {
                StuffyBot.guildsPlaying--;
                nextTrack();
            }
        } else {
            StuffyBot.guildsPlaying--;
        }
    }

    public boolean isRepeating() {
        return !repeating;
    }

    public void setRepeating(boolean repeating) {
        this.repeating = repeating;
    }

    public void removeTrack(int queueNumber, TextChannel channel) {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        AudioTrack tracktoBeRemoved = tracks.get((queueNumber - 1));
        queue.remove(tracks.get((queueNumber - 1)));
        EmbedBuilder builder = new EmbedBuilder();
        builder.setTitle("Status");
        builder.addField("Removed:", tracktoBeRemoved.getInfo().title, false);
        builder.setFooter(tracktoBeRemoved.getInfo().uri);
        builder.setColor(channel.getGuild().getSelfMember().getColor());
        channel.sendMessage(builder.build()).queue();
    }
    public void clearQueue(Guild guild) {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        for (AudioTrack track : tracks) {
            queue.remove(track);
        }
        logger.info("Cleared queue of guild \"" + guild.getName() + "(" + guild.getId() + ")\"");
    }
    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public void bassBoost(int percentage, String guildName, TextChannel channel) {
        final int previousPercentage = this.boostPercentage;
        this.boostPercentage = percentage;

        if (previousPercentage > 0 && percentage == 0) {
            channel.sendMessage("Turning off equalizer...").queue();
            this.player.setFilterFactory(null);
            return;
        }

        if (previousPercentage == 0 && percentage > 0) {
            channel.sendMessage("Turning on equalizer...").queue();
            if (this.equalizer == null) {
                this.equalizer = new EqualizerFactory();
            }
            this.player.setFilterFactory(this.equalizer);
        }

        final float multiplier = percentage / 100.0f;
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < BASS_BOOST.length; i++) {
            this.equalizer.setGain(i, BASS_BOOST[i] * multiplier);
            builder.append(BASS_BOOST[i] * multiplier).append(", ");
        }

        this.boostPercentage = percentage;
        logger.info("Successfully set the bass boost level. Guild: " + guildName);
        channel.sendMessage(builder.toString()).queue();
        StringBuilder bands = new StringBuilder();
        for(int i = 0; i < 14; i++) {
            bands.append(this.equalizer.getGain(i)).append(", ");
        }
        channel.sendMessage(bands.toString()).queue();
    }
}

