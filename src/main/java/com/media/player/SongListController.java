package com.media.player;

import com.media.NetworkConnection.Network;
import com.media.player.Music.MusicInitializer;
import com.media.player.MusicPlayer.MusicPlayer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class SongListController
{
    @FXML
    private TableView<Song> songTable;
    @FXML
    private TableColumn<Song, String> songTitleColumn;
    @FXML
    private TableColumn<Song, String> artistColumn;
    @FXML
    private TableColumn<Song, PlayButton> playSongButtonColumn;

    @FXML
    private Button togglePlayButton;

    @FXML
    private CheckMenuItem server_selector;

    @FXML
    private CheckMenuItem autoplay_selector;

    @FXML
    private CheckMenuItem shuffle_selector;

    @FXML
    private MenuItem close_selector;

    @FXML
    private Label song_name_id;




    private ObservableList<Song> songs;
    private MusicPlayer musicPlayer;
    private MusicInitializer music;







    private Comparator<String> stringComparator()
    {
        return (song1, song2) -> {
            song1 = song1.toLowerCase();
            song2 = song2.toLowerCase();
            List<String> prefixes = Arrays.asList("the", "a", "les", "le");
            for (String prefix: prefixes)
            {
                if (song1.startsWith(prefix + " ")) {
                    song1 = song1.substring(prefix.length() + 1);
                }
                if (song2.startsWith(prefix + " ")) {
                    song2 = song2.substring(prefix.length() + 1);
                }
            }
            return song1.compareTo(song2);
        };
    }



    @FXML
    void initialize() throws ExecutionException, InterruptedException, IOException
    {
        final Network network = new Network();

        initializeColumns();

        String baseUrl = network.getServerUrl();
        System.out.println("Base url is " + baseUrl);

        try
        {
            musicPlayer = MusicPlayer.createInstance(songTable, song_name_id);
        }
        catch (IOException | URISyntaxException e)
        {
            e.printStackTrace();
            // TODO: handle failure to register
        }

        initializeActionListeners();
        setKeyCommands();

        boolean isRemoteServer = network.isRemoteServer();
        setParametersBasedOnServerLocation(isRemoteServer);

        initializeDropdowns();
        initializeMusic();
    }





    private void initializeColumns()
    {
        songTitleColumn.setCellValueFactory(cell -> cell.getValue().songNameProperty());
        songTitleColumn.setComparator(stringComparator());
        songTitleColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        artistColumn.setCellValueFactory(cell -> cell.getValue().artistProperty());
        artistColumn.setComparator(stringComparator());
        artistColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        playSongButtonColumn.setCellFactory(cell -> CreatePlaySongCell.createPlaySongCell(cell.getTableView()));
    }

    private void initializeActionListeners()
    {
        togglePlayButton.setOnAction((event) -> {
            try {
                musicPlayer.togglePlay();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });

        shuffle_selector.setOnAction(event -> musicPlayer.setShuffle(shuffle_selector.isSelected()));
        server_selector.setOnAction(event -> musicPlayer.setServer(server_selector.isSelected()));
        autoplay_selector.setOnAction(event -> musicPlayer.setAutoPlay(autoplay_selector.isSelected()));
        close_selector.setOnAction(event -> System.exit(0));
    }


    private void setKeyCommands()
    {
        songTable.addEventFilter(KeyEvent.KEY_RELEASED, event -> {
            switch (event.getCode()) {
                case PLAY:
                case SPACE:
                case ENTER:
                    try
                    {
                        musicPlayer.togglePlay();
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    break;
                case TRACK_NEXT:
                case RIGHT:
                    musicPlayer.playNext();
                    break;
                case TRACK_PREV:
                case LEFT:
                    musicPlayer.playPrevious();
                    break;
            }
        });
    }

    private void setParametersBasedOnServerLocation(boolean isRemoteServer)
    {
        server_selector.setSelected(!isRemoteServer);
        musicPlayer.setServer(!isRemoteServer);
    }

    private void initializeDropdowns()
    {
        autoplay_selector.setSelected(true);
        shuffle_selector.setSelected(true);
        musicPlayer.setShuffle(true);
    }

    private void initializeMusic()
    {
        CompletableFuture.runAsync(() ->
        {
            try
            {
                music = new MusicInitializer();
                songs = FXCollections.observableList(music.getMusic());
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }

            songs.sort((song1, song2) -> stringComparator().compare(song1.getArtist(), song2.getArtist()));
            songTable.setItems(songs);
        });
    }
}



