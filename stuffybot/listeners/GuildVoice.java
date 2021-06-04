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

import com.github.thethingyee.stuffybot.StuffyBot;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class GuildVoice extends ListenerAdapter {

    private final StuffyBot stuffyBot;

    public GuildVoice(StuffyBot stuffyBot) {
        this.stuffyBot = stuffyBot;
    }

    private static final HashMap<Guild, Timer> guildTimers = new HashMap<>();

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if(Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel() == null) return;
        VoiceChannel vc = event.getGuild().getSelfMember().getVoiceState().getChannel();
        assert vc != null;
        if(vc.equals(event.getChannelLeft())) {
            int members = (vc.getMembers().size() - 1);
            if(members == 0) {

                Timer timer;

                guildTimers.put(event.getGuild(), timer = new Timer(true));
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(event.getGuild().getAudioManager().isConnected()) {
                            ArrayList<AudioTrack> queue = new ArrayList<>(stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.getQueue());
                            for(int i = 0; i < queue.size(); i++) {
                                queue.remove(queue.get(i));
                            }
                            StuffyBot.logger.warning("Cleared queue of guild \"" + event.getGuild().getName() + "\" to save bandwidth.");
                            event.getGuild().getAudioManager().closeAudioConnection();
                            guildTimers.remove(event.getGuild());
                        }
                    }
                }, (30 * 1000));
            }
        }
    }
    @Override
    public void onGuildVoiceJoin(GuildVoiceJoinEvent event) {
        if(Objects.requireNonNull(event.getGuild().getSelfMember().getVoiceState()).getChannel() == null) return;
        // if someone joins and the bot will be trying to disconnect, cancel the task and leave the bot as it is.
        if (guildTimers.containsKey(event.getGuild())) {
            guildTimers.get(event.getGuild()).cancel();
            guildTimers.get(event.getGuild()).purge();
        }
    }
}
