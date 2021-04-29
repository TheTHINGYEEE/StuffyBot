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
import java.util.logging.Logger;

import static com.github.thethingyee.stuffybot.StuffyBot.logger;

/**
 * This class schedules tracks for the audio player. It contains the queue of tracks.
 */
public class TrackScheduler extends AudioEventAdapter {
    private static final float[] BASS_BOOST = {-0.05f, 0.07f, 0.16f, 0.03f, -0.05f, -0.11f};

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
        }
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        // Only start the next track if the end reason is suitable for it (FINISHED or LOAD_FAILED)
        if (endReason.mayStartNext) {
            nextTrack();
        }
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
    public void clearQueue(Logger logger, Guild guild) {
        List<AudioTrack> tracks = new ArrayList<>(queue);
        for (AudioTrack track : tracks) {
            queue.remove(track);
        }
        logger.info("Cleared queue of guild \"" + guild.getName() + "(" + guild.getId() + ")\"");
    }
    public BlockingQueue<AudioTrack> getQueue() {
        return queue;
    }

    public void bassBoost(int percentage) {
        final int previousPercentage = this.boostPercentage;
        this.boostPercentage = percentage;

        // Disable filter factory
        if (previousPercentage > 0 && percentage == 0) {
            this.player.setFilterFactory(null);
            return;
        }
        // Enable filter factory
        if (previousPercentage == 0 && percentage > 0) {
            if (this.equalizer == null) {
                this.equalizer = new EqualizerFactory();
            }
            this.player.setFilterFactory(this.equalizer);
        }

        final float multiplier = percentage / 100.0f;
        for (int i = 0; i < BASS_BOOST.length; i++) {
            this.equalizer.setGain(i, BASS_BOOST[i] * multiplier);
        }

        this.boostPercentage = percentage;
        logger.info("Successfully set the bass boost level.");
    }
}

