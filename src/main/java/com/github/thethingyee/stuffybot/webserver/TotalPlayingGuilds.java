package com.github.thethingyee.stuffybot.webserver;

public class TotalPlayingGuilds {

    private final String id;
    private final int guildsPlaying;

    public TotalPlayingGuilds(String id, int guildsPlaying) {
        this.id = id;
        this.guildsPlaying = guildsPlaying;
    }

    public String getId() {
        return id;
    }

    public int getGuildsPlaying() {
        return guildsPlaying;
    }
}
