package com.media.helper;

import com.media.player.Song;
import org.apache.commons.io.IOUtils;

import java.io.*;

public class TrayNotification
{
    private String[] notifyCommandFormat;


    public TrayNotification()
    {
        try
        {
            ClassLoader loader = getClass().getClassLoader();

            try (InputStream inputStream = loader.getResourceAsStream("Images/play-song-icon.png");)
            {
                File temp = File.createTempFile("temp_image", ".tmp");
                temp.deleteOnExit();
                OutputStream outputStream = new FileOutputStream(temp);
                IOUtils.copy(inputStream, outputStream);
                notifyCommandFormat = new String[] {"/usr/bin/notify-send", "", "", "-i", temp.getAbsolutePath(), "-t", "5000"};
            }
            catch (IOException e)
            {
                e.printStackTrace();
                System.out.println("Can't find icon for notification");
                notifyCommandFormat = new String[] {"/usr/bin/notify-send", "", "", "-t", "5000"};
            }
        }
        catch (NullPointerException e)
        {
            System.out.println("Can't find icon for notification");
            notifyCommandFormat = new String[] {"/usr/bin/notify-send", "", "", "-t", "5000"};
        }


    }


    public void notifyChangedSong(Song song)
    {
        notifyCommandFormat[1] = song.getArtist();
        notifyCommandFormat[2] = song.getSongName();

        killPreviousNotifications();

        ProcessBuilder processBuilder = new ProcessBuilder(notifyCommandFormat);

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

    private void killPreviousNotifications()
    {
        ProcessBuilder pgrepBuilder = new ProcessBuilder("pgrep", "^notify-osd$");
        try
        {
            Process pgrepProcess = pgrepBuilder.start();
            pgrepProcess.waitFor();
            String pid = IOUtils.toString(pgrepProcess.getInputStream()).replaceAll("\\s+", "");

            new ProcessBuilder("/bin/kill", pid).start();
        }
        catch (InterruptedException | IOException e)
        {
            e.printStackTrace();
        }
    }
}
