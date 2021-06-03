package com.github.thethingyee.stuffybot.cleancode;

import net.dv8tion.jda.api.*;
import net.dv8tion.jda.api.entities.*;

import java.io.*;
import java.net.*;

public class NetworkTester {

    private final Guild guild;

    public NetworkTester(Guild guild) { this.guild = guild; }

    public double testPing() {
        StringBuilder region = new StringBuilder();

        region.append(guild.getRegion().getKey());

        switch(guild.getRegion().getKey()) {
            case "hongkong": {
                region.append("3191");
            }
            case "brazil": {
                region.append("10035");
            }
            case "eu-central": {
                region.append("6831");
            }
            case "india": {
                region.append("6099");
            }
            case "japan": {
                region.append("9132");
            }
            case "russia": {
                region.append("1203");
            }
            case "singapore": {
                region.append("9213");
            }
            case "southafrica": {
                region.append("411");
            }
            case "sydney": {
                region.append("9729");
            }
            case "us-central": {
                region.append("1881");
            }
            case "us-east": {
                region.append("4339");
            }
            case "us-south": {
                region.append("2459");
            }
            case "us-west": {
                region.append("5893");
            }
        }
        region.append(".discord.gg");

        double ms = 0;

        try {
            long startTime = System.nanoTime();
            new URL("https://" + region.toString()).openConnection().connect();
            ms = (System.nanoTime() - startTime) / 15e5;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return ms;
    }
    public Region getRegion() {
        return guild.getRegion();
    }
}
