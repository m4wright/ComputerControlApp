package com.media.player.MusicPlayer;

import com.media.Listener.NetworkListener;
import com.media.NetworkConnection.Network;
import com.media.helper.TrayNotification;
import com.media.player.Song;
import javafx.application.Platform;
import javafx.scene.control.Label;
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
    private static final int MAX_ELAPSED_MS_PREV_SONG = 5000;

    private final Label songLabel;

    private int currentSongIndex = -1;




    public static MusicPlayer createInstance(TableView<Song> songTable, Label songLabel) throws IOException, URISyntaxException
    {
        if (musicPlayerInstance != null)
        {
            throw new IllegalStateException("MusicInitializer Player already exists");
        }
   
        musicPlayerInstance = new MusicPlayer(songTable, new Network().getServerUrl(), songLabel);
        return musicPlayerInstance;
    }

    public static MusicPlayer instance() { return musicPlayerInstance; }



    private MusicPlayer(TableView<Song> songTable, String baseUrl, Label songLabel) throws IOException, URISyntaxException
    {
        this.songLabel = songLabel;
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

        Platform.runLater(() -> songLabel.setText(song.toString()));

//
//
//        if (!previousSongs.isEmpty())
//        {
//            System.out.println("Previous songs is not empty");
//
//            try
//            {
//                PlayButton songCell = (PlayButton) songTable.getColumns().get(0).getCellData(0);
//                System.out.println("Deselecting " + previousSongs.getLast());
//
//            }
//            catch (Exception ignored) {
//                System.out.println("Exception :(");
//                ignored.printStackTrace();
//            }
//        }

        if (previousSongs.isEmpty() || !previousSongs.getLast().equals(song))
        {
            previousSongs.add(song);
        }

        timeCurrentSongStarted = System.currentTimeMillis();
        while (previousSongs.size() > MAX_PREVIOUS_SONGS)
        {
            previousSongs.removeFirst();
        }
        trayNotification.notifyChangedSong(song);


        currentSongIndex = songTable.getItems().indexOf(song);
    }

    public void togglePlay(Song song) throws IOException
    {
        if (previousSongs.isEmpty() || !previousSongs.getLast().equals(song))
        {
            play(song);
        }
        else
        {
            togglePlay();
        }
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
        int nextIndex;
        if (shuffle)
        {
            while ((nextIndex = ThreadLocalRandom.current().nextInt(songTable.getItems().size())) == currentSongIndex) { }
        }
        else
        {
            nextIndex = (currentSongIndex + 1) % songTable.getItems().size();
        }

        Song nextSong = songTable.getItems().get(nextIndex);
        try
        {
            play(nextSong);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
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
