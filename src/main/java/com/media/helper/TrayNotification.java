package com.media.helper;

import com.media.player.Song;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class TrayNotification
{
    private static String[] notifyCommandFormat;

    static
    {
        File image = new File("src/main/resources/Images/play-song-icon.png");
        notifyCommandFormat = new String[] {"/usr/bin/notify-send", "%s", "%s", "-i", image.getAbsolutePath(), "-t", "5000"};
    }


    public static void notifyChangedSong(Song song) {
        String[] command = Arrays.copyOf(notifyCommandFormat, notifyCommandFormat.length);
        command[1] = String.format(command[1], song.getArtist());
        command[2] = String.format(command[2], song.getSongName());
        System.out.println(Arrays.toString(command));

        ProcessBuilder processBuilder = new ProcessBuilder(command);

        try
        {
            Process process = processBuilder.start();
            process.waitFor();
        }
        catch (IOException | InterruptedException e)
        {
            e.printStackTrace();
        }
    }

    public static void main(String[] args)
    {
        notifyChangedSong(new Song("Atmosphere", "Sound Is Vibration"));
    }
}
