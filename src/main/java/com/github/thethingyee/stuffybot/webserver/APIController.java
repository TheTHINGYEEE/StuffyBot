package com.github.thethingyee.stuffybot.webserver;

import com.github.thethingyee.stuffybot.StuffyBot;
import net.dv8tion.jda.api.entities.Guild;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class APIController {

    private final AtomicLong counter = new AtomicLong();

    @GetMapping("/guilds")
    public TotalGuilds totalGuilds() {
        ArrayList<String> guildIds = new ArrayList<>();
        for(Guild guild : StuffyBot.jda.getGuilds()) {
            guildIds.add(guild.getId());
        }
        return new TotalGuilds(String.valueOf(counter.incrementAndGet()), guildIds.size(), guildIds);
    }

    @GetMapping("/guilds/all/queued")
    public TotalPlayingGuilds totalPlayingGuilds() {
        return new TotalPlayingGuilds(String.valueOf(counter.incrementAndGet()), StuffyBot.guildsPlaying);
    }

}
