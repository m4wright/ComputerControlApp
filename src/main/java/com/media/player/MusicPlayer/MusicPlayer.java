package com.media.player.MusicPlayer;

import com.media.Listener.NetworkListener;
import com.media.helper.TrayNotification;
import com.media.player.ServerMusicPlayer;
import com.media.player.Song;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.concurrent.ThreadLocalRandom;

public class MusicPlayer implements MusicPlayerInterface
{
    private static final int MAX_PREVIOUS_SONGS = 50;
    private static MusicPlayer musicPlayerInstance;
    private final TableView<Song> songTable;
    private final NetworkListener networkListener;
    private boolean server = true;
    private boolean shuffle = false;
    private boolean autoPlay = true;

    private final TrayNotification trayNotification = new TrayNotification();

    private final ServerMusicPlayer serverMusicPlayer;
    private final ClientMusicPlayer clientMusicPlayer;

    private final LinkedList<Song> previousSongs = new LinkedList<>();
    private long timeCurrentSongStarted;
    private final int MAX_ELAPSED_MS_PREV_SONG = 5000;


    public static MusicPlayer createInstance(TableView<Song> songTable, String baseUrl) throws IOException, URISyntaxException
    {
        if (musicPlayerInstance != null)
        {
            throw new IllegalStateException("Music Player already exists");
        }
        musicPlayerInstance = new MusicPlayer(songTable, baseUrl);
        return musicPlayerInstance;
    }

    public static MusicPlayer instance() { return musicPlayerInstance; }



    private MusicPlayer(TableView<Song> songTable, String baseUrl) throws IOException, URISyntaxException
    {
        this.songTable = songTable;

        serverMusicPlayer = new ServerMusicPlayer(baseUrl);
        clientMusicPlayer = new ClientMusicPlayer(baseUrl);


        networkListener = new NetworkListener();
        networkListener.register(baseUrl);
        networkListener.addHandler("done_song", this::donePlaying);
    }

    void donePlaying()
    {
        if (autoPlay) playNext();
    }




    @Override
    public void play(Song song) throws IOException
    {
        if (server) serverMusicPlayer.play(song);
        else clientMusicPlayer.play(song);

        previousSongs.add(song);
        timeCurrentSongStarted = System.currentTimeMillis();
        while (previousSongs.size() > MAX_PREVIOUS_SONGS)
        {
            previousSongs.removeFirst();
        }
        trayNotification.notifyChangedSong(song);
    }

    public void playPrevious()
    {
        if (!previousSongs.isEmpty())
        {
            Song previousSong = previousSongs.removeLast();
            if (System.currentTimeMillis() - timeCurrentSongStarted < MAX_ELAPSED_MS_PREV_SONG)
            {
                if (!previousSongs.isEmpty())
                {
                    previousSong = previousSongs.removeLast();
                }

            }
            try
            {
                play(previousSong);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
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

        int nextIndex;
        if (shuffle)
        {
            while ((nextIndex = ThreadLocalRandom.current().nextInt(songTable.getItems().size())) == currentSongIndex) { }
        }
        else
        {
            nextIndex = (currentSongIndex + 1) % songTable.getItems().size();
        }

        songTable.getSelectionModel().select(nextIndex);
    }

    @Override
    public void togglePlay() throws IOException
    {
        if (server) serverMusicPlayer.togglePlay();
        else clientMusicPlayer.togglePlay();
    }



    public void setServer(boolean server)
    {
        this.server = server;
    }

    public void setShuffle(boolean shuffle)
    {
        this.shuffle = shuffle;
    }

    public void setAutoPlay(boolean autoPlay)
    {
        this.autoPlay = autoPlay;
    }
}
