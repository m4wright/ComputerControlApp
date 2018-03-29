package com.media.player;

import com.media.Listener.NetworkListener;
import javafx.scene.control.TableView;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URISyntaxException;

public class MusicPlayer
{
    private static MusicPlayer MusicPlayerInstance;

    private final TableView<Song> songTable;
    private MediaPlayer mediaPlayer;
    private NetworkListener networkListener = new NetworkListener();

    private final String baseUrl;
    private boolean server = true;

    private boolean autoPlay = true;
    private Song currentSong;



    static MusicPlayer createInstance(TableView<Song> songTable, String baseUrl) throws IOException, URISyntaxException
    {
        if (MusicPlayerInstance != null)
        {
            throw new IllegalStateException("Music Player already exists");
        }
        MusicPlayerInstance = new MusicPlayer(songTable, baseUrl);
        return MusicPlayerInstance;
    }

    public static MusicPlayer instance() { return MusicPlayerInstance; }



    private MusicPlayer(TableView<Song> songTable, String baseUrl) throws IOException, URISyntaxException
    {
        this.songTable = songTable;
        this.baseUrl = baseUrl;
        networkListener.register(baseUrl);
        networkListener.addHandler("done_song", () -> {
            if (autoPlay) playNext();
        });
    }


    public void playNext()
    {
        int currentSongIndex;
        try
        {
            currentSongIndex = songTable.getSelectionModel().getSelectedIndex();
        } catch (NullPointerException e)
        {
            currentSongIndex = -1;
        }
        int nextIndex = (currentSongIndex + 1) % songTable.getItems().size();
        songTable.getSelectionModel().select(nextIndex);
    }



    public void play(Song song) throws IOException, EncoderException
    {
        currentSong = song;
        if (server) playSongOnServer(song.getArtist(), song.getSongName());
        else playSongOnClient(song.getArtist(), song.getSongName());
    }




    private void playSongOnServer(String artist, String song) throws IOException, EncoderException
    {
        try
        {
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.setParameter("command", "play_song")
                    .setParameter("song_name", song)
                    .setParameter("artist", artist);
            System.out.println(builder.build());
            HttpGet request = new HttpGet(builder.build());
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            System.out.println(IOUtils.toString(entity.getContent()));
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    private void playSongOnClient(String artist, String song)
    {
        if (mediaPlayer != null && mediaPlayer.getStatus().equals(MediaPlayer.Status.PLAYING))
        {
            mediaPlayer.stop();
        }
        try {
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.setParameter("command", "get_song")
                    .setParameter("song_name", song)
                    .setParameter("artist", artist);

            String uri = builder.build().toString();

            System.out.println(uri);
            mediaPlayer = new MediaPlayer(new Media(uri));
            mediaPlayer.setOnError(() -> {
                System.out.println("Error in media :(");
                System.out.println(mediaPlayer.getError());
            });
            mediaPlayer.setOnReady(() -> {
                System.out.println("ready to play " + song);
                mediaPlayer.play();
            });
            mediaPlayer.play();
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }

    public void togglePlay()
    {
        if (server) togglePlayServer();
        else togglePlayClient();
    }

    private void togglePlayServer()
    {
        try
        {
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.setParameter("command", "toggle_play");
            HttpGet request = new HttpGet(builder.build());
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(request);
        }
        catch (URISyntaxException | IOException e)
        {
            e.printStackTrace();
        }
    }

    private void togglePlayClient()
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

    public void setToClient() { server = false; }
    public void setToServer() { server = true; }
}
