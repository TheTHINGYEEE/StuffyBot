package com.github.thethingyee.stuffybot.webserver;

import java.util.ArrayList;
import java.util.UUID;

public class TotalGuilds {

    private final String requestId;
    private final int totalGuilds;
    private final ArrayList<String> guildIds;

    public TotalGuilds(String requestId, int totalGuilds, ArrayList<String> guildIds) {
        this.requestId = requestId;
        this.totalGuilds = totalGuilds;
        this.guildIds = guildIds;
    }

    public String getRequestId() {
        return requestId;
    }

    public int getTotalGuilds() {
        return totalGuilds;
    }

    public ArrayList<String> getGuildIds() {
        return guildIds;
    }
}
