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
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.util.Objects;

public class AddReaction extends ListenerAdapter {

    private final StuffyBot stuffyBot;

    public AddReaction(StuffyBot stuffyBot) {
        this.stuffyBot = stuffyBot;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void onGuildMessageReactionAdd(GuildMessageReactionAddEvent event) {

        // to-do: if paused, only show play button. if not, only show pause button; add functions for the reactions.

        if (event.getUser().isBot() && !event.getUser().equals(StuffyBot.jda.getSelfUser())) {
            event.getChannel().retrieveMessageById(event.getMessageId()).complete().removeReaction(
                    event.getReactionEmote().getName(), event.getUser());
            return;
        }
        if (event.getUser().equals(StuffyBot.jda.getSelfUser())) {
            return;
        }

        Message msg = event.getChannel().retrieveMessageById(event.getMessageId()).complete();
        if (msg.getAuthor().equals(StuffyBot.jda.getSelfUser()) && (msg.getEmbeds().get(0) != null) &&
                Objects.requireNonNull(msg.getEmbeds().get(0).getTitle()).equals(stuffyBot.botName + " Player")) {
            if (event.getReactionEmote().getName().equals("⏸")) {
                if (!stuffyBot.getGuildAudioPlayer(msg.getGuild()).player.isPaused()) {
                    stuffyBot.getGuildAudioPlayer(msg.getGuild()).player.setPaused(true);
                    msg.removeReaction(event.getReactionEmote().getName(), event.getUser()).complete();
                    msg.removeReaction(event.getReactionEmote().getName()).complete();

                    MessageEmbed embed = msg.getEmbeds().get(0);
                    EmbedBuilder builder = new EmbedBuilder(embed);
                    builder.getFields().remove(2);
                    builder.addField("Status:", "Paused", false);
                    msg.editMessage(builder.build()).queue(message -> message.addReaction("▶️").queue());

                }
            } else if (event.getReactionEmote().getName().equals("▶️")) {
                if (stuffyBot.getGuildAudioPlayer(event.getGuild()).player.isPaused()) {

                    stuffyBot.getGuildAudioPlayer(event.getGuild()).player.setPaused(false);
                    msg.removeReaction(event.getReactionEmote().getName(), event.getUser()).complete();
                    msg.removeReaction(event.getReactionEmote().getName()).complete();

                    MessageEmbed embed = msg.getEmbeds().get(0);
                    EmbedBuilder builder = new EmbedBuilder(embed);
                    builder.getFields().remove(2);
                    builder.addField("Status:", "Playing", false);
                    msg.editMessage(builder.build()).queue(message -> message.addReaction("⏸").queue());
                }
            } else if (event.getReactionEmote().getName().equals("⏭")) {
                if (!stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.getQueue().isEmpty()) {
                    stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.nextTrack();

                    msg.removeReaction(event.getReactionEmote().getName(), event.getUser()).complete();
                    MessageEmbed embed = msg.getEmbeds().get(0);
                    EmbedBuilder builder = new EmbedBuilder(embed);
                    builder.getFields().clear();
                    if (stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() != null) {
                        builder.addField("Now playing:", stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack().getInfo().title, true);
                        builder.addField("# of queued songs:", stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.getQueue().size() + "", true);
                    } else {
                        builder.addField("Now playing:", "Empty", true);
                        builder.addField("# of queued songs:", "Empty", true);
                    }
                    if (stuffyBot.getGuildAudioPlayer(event.getGuild()).player.isPaused()) {
                        if (stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() == null) {
                            builder.addField("Status:", "No music playing.", false);
                        } else {
                            builder.addField("Status:", "Paused", false);
                        }
                    } else {
                        if (stuffyBot.getGuildAudioPlayer(event.getGuild()).player.getPlayingTrack() == null) {
                            builder.addField("Status:", "No music playing", false);
                        } else {
                            builder.addField("Status:", "Playing", false);
                        }
                    }
                    msg.editMessage(builder.build()).queue(message -> {
                        if(stuffyBot.getGuildAudioPlayer(event.getGuild()).scheduler.getQueue().isEmpty()) {
                            message.removeReaction("⏭").queue();
                        }
                    });
                }
            }
        }
    }
}
