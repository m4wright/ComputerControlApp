package com.media.player.MusicPlayer;

import com.media.player.Song;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

public class ClientMusicPlayer implements MusicPlayerInterface
{
    private MediaPlayer mediaPlayer;
    private final String baseUrl;

    ClientMusicPlayer(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }


    @Override
    public synchronized void play(Song song) throws IOException
    {
        if (mediaPlayer != null)
        {
            mediaPlayer.stop();
        }

        try
        {
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.setParameter("command", "get_song")
                    .setParameter("song_name", song.getSongName())
                    .setParameter("artist", song.getArtist());

            String uri = builder.build().toString();
            System.out.println(uri);
            try {
                Media media = new Media(uri);
                mediaPlayer = new MediaPlayer(media);
                mediaPlayer.setOnError(() -> {
                System.out.println("Error in media :(");
                mediaPlayer.getError().printStackTrace();
            });
            mediaPlayer.setOnReady(() -> {
                System.out.println("Playing " + song);
                mediaPlayer.play();
            });

            mediaPlayer.setOnEndOfMedia(() -> {
                System.out.println("Done playing");
                MusicPlayer.instance().donePlaying();
            });

            } catch (Exception e)
            {
                System.out.println("Caught exception when trying to play song");
                e.printStackTrace();
            }
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }




    @Override
    public void togglePlay() throws IOException
    {
        if (mediaPlayer != null)
        {
            if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING))
            {
                mediaPlayer.pause();
            }
            else if (mediaPlayer.getStatus().equals(MediaPlayer.Status.PAUSED))
            {
                mediaPlayer.play();
            }
        }
    }
}
