package com.media.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.media.Listener.NetworkListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class SongListController
{
    @FXML
    private TableView<Song> songTable;
    @FXML
    private TableColumn<Song, String> songTitleColumn;
    @FXML
    private TableColumn<Song, String> artistColumn;

    @FXML
    private Button togglePlayButton;

    @FXML
    private MenuItem server_selector;

    @FXML
    private MenuItem client_selector;

    private ObservableList<Song> songs;


    private static final String baseUrl = "http://192.168.2.25:10000/control_app";
    private boolean server = true;

    private MediaPlayer mediaPlayer;

    private NetworkListener networkListener = new NetworkListener();







    private void getSongs() throws IOException
    {
        try
        {
            URIBuilder builder = new URIBuilder(baseUrl);

            builder.setParameter("command", "get_songs");
            HttpGet request = new HttpGet(builder.build());
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();
            String result = IOUtils.toString(entity.getContent());

            JsonParser parser = new JsonParser();
            JsonObject artists = (JsonObject) parser.parse(result);
            List<Song> songs = Collections.synchronizedList(new ArrayList<>());

            artists.entrySet().stream().flatMap((Map.Entry<String, JsonElement> artist) ->
            {
                String artistName = artist.getKey();

                return artist.getValue().getAsJsonObject().get("songs")
                        .getAsJsonObject().keySet()
                        .parallelStream().map(song -> new Song(artistName, song));
            }).collect(Collectors.toCollection(() -> songs));

            this.songs = FXCollections.observableList(songs);

        } catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
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


    private Comparator<String> stringComparator()
    {
        return (song1, song2) -> {
            song1 = song1.toLowerCase();
            song2 = song2.toLowerCase();
            List<String> prefixes = Arrays.asList("the", "a");
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

    private ChangeListener<Song> getStringChangeListener()
    {
        return (ObservableValue<? extends Song> observable, Song oldValue, Song newValue) -> {
            try {
                play(newValue);
            } catch (IOException | EncoderException e) {
                e.printStackTrace();
            }
        };
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

    private void togglePlay()
    {
        if (server) togglePlayServer();
        else togglePlayClient();
    }

    public void playNext()
    {
        Song nextSong = this.songs.get(125);
        try {
            play(nextSong);
        } catch (IOException | EncoderException e) {
            e.printStackTrace();
        }
    }

    private void play(Song song) throws IOException, EncoderException
    {
        if (server) playSongOnServer(song.getArtist(), song.getSongName());
        else playSongOnClient(song.getArtist(), song.getSongName());
    }


    @FXML
    void initialize()
    {

        songTitleColumn.setCellValueFactory(cell -> cell.getValue().songNameProperty());
        songTitleColumn.setComparator(stringComparator());

        artistColumn.setCellValueFactory(cell -> cell.getValue().artistProperty());
        artistColumn.setComparator(stringComparator());

        songTable.getSelectionModel().selectedItemProperty().addListener(getStringChangeListener());


        togglePlayButton.setOnAction((event) -> togglePlay());

        server_selector.setOnAction(event -> server = true);
        client_selector.setOnAction(event -> server = false);

        CompletableFuture.runAsync(() ->
        {
            try
            {
                networkListener.register(baseUrl);
                getSongs();
            }
            catch (IOException | URISyntaxException e)
            {
                e.printStackTrace();
            }

            songs.sort((song1, song2) -> stringComparator().compare(song1.getArtist(), song2.getArtist()));
            songTable.setItems(songs);
        });
    }
}
