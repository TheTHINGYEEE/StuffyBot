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
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import oshi.SystemInfo;
import oshi.hardware.CentralProcessor;
import oshi.hardware.GlobalMemory;
import oshi.hardware.GraphicsCard;
import oshi.hardware.HardwareAbstractionLayer;

import java.awt.*;

import static com.github.thethingyee.stuffybot.StuffyBot.*;

public class AdditionalCommands extends ListenerAdapter {

    private final StuffyBot stuffyBot;

    public AdditionalCommands(StuffyBot stuffyBot) {
        this.stuffyBot = stuffyBot;
    }

    @Override
    public void onGuildMessageReceived(@NotNull GuildMessageReceivedEvent event) {
        String[] command = event.getMessage().getContentRaw().split(" ");

        if (botChannels.containsKey(event.getGuild())) {
            if (botChannels.get(event.getGuild()).contains(event.getChannel())) {
                if (command[0].equalsIgnoreCase(prefix + "usage")) {
                    if (event.getMember().hasPermission(Permission.ADMINISTRATOR)) {
                        SystemInfo systemInfo = new SystemInfo();
                        HardwareAbstractionLayer layer = systemInfo.getHardware();
                        CentralProcessor proc = layer.getProcessor();

                        GlobalMemory mem = layer.getMemory();

                        CentralProcessor.ProcessorIdentifier identifier = proc.getProcessorIdentifier();

                        GraphicsCard graphics = null;

                        for(GraphicsCard gCard : layer.getGraphicsCards()) {
                            if(gCard != null) {
                                graphics = gCard;
                            }
                        }

                        EmbedBuilder embed = new EmbedBuilder();

                        embed.setColor(Color.CYAN);

                        embed.setTitle(stuffyBot.botName + "'s Hoster Statistics");
                        embed.setDescription("See the statistics of " + stuffyBot.botName + "'s bot hoster.\n \nThe CPU usage can be mostly at 0.0% \nbecause bots don't usually tend \nto use CPUs that much.");

                        embed.addField("Processor Name:", identifier.getName(), false);
                        embed.addField("Frequency:", (identifier.getVendorFreq() / 1000000000.0) + " GHz", true);
                        embed.addField("# of CPUs:", proc.getLogicalProcessorCount() + "", true);

                        embed.addField(" ", " ", false);

                        embed.addField("Graphics:", graphics.getName(), false);
                        embed.addField("Virtual RAM:", (graphics.getVRam() / (1024.0*1024.0) / 1024.0) + " GB", true);
                        embed.addField("Device ID:", graphics.getDeviceId(), true);

                        embed.addField(" ", " ", false);

                        double cpu = proc.getSystemCpuLoadBetweenTicks(proc.getSystemCpuLoadTicks()) * 1000;

                        embed.addField("OS:", System.getProperty("os.name") + " " + System.getProperty("os.version") + " / " + System.getProperty("os.arch"), false);
                        embed.addField("CPU Usage:", cpu + "%", true);
                        embed.addField("RAM Usage: ", ((mem.getTotal() - mem.getAvailable())) + " GB", true);


                        embed.setFooter(stuffyBot.getVersion() + " / TheTHINGYEEEEE#1859");

                        event.getChannel().sendMessage(embed.build()).queue();
                    } else {
                        event.getChannel().sendMessage("Sorry but you don't have permission to do this.").queue();
                    }
                }
            }
        }
    }
}
