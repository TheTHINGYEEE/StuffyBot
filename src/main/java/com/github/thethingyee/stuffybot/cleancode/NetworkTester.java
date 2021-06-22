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

        // A list of some available discord servers. (not guilds lmao)
        // format: key1234.discord.gg
        switch(guild.getRegion().getKey()) {
            case "hongkong": {
                region.append("3191");
                break;
            }
            case "brazil": {
                region.append("10035");
                break;
            }
            case "eu-central": {
                region.append("6831");
                break;
            }
            case "india": {
                region.append("6099");
                break;

            }
            case "japan": {
                region.append("9132");
                break;

            }
            case "russia": {
                region.append("1203");
                break;

            }
            case "singapore": {
                region.append("9213");
                break;

            }
            case "southafrica": {
                region.append("411");
                break;

            }
            case "sydney": {
                region.append("9729");
                break;

            }
            case "us-central": {
                region.append("1881");
                break;

            }
            case "us-east": {
                region.append("4339");
                break;

            }
            case "us-south": {
                region.append("2459");
                break;

            }
            case "us-west": {
                region.append("5893");
                break;

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
