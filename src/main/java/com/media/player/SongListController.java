package com.media.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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

    private static final String baseUrl = "http://192.168.2.25:8080/control_app";


    private MusicPlayer musicPlayer;







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
                musicPlayer.play(newValue);
            } catch (IOException | EncoderException e) {
                e.printStackTrace();
            }
        };
    }



    @FXML
    void initialize()
    {

        songTitleColumn.setCellValueFactory(cell -> cell.getValue().songNameProperty());
        songTitleColumn.setComparator(stringComparator());

        artistColumn.setCellValueFactory(cell -> cell.getValue().artistProperty());
        artistColumn.setComparator(stringComparator());

        songTable.getSelectionModel().selectedItemProperty().addListener(getStringChangeListener());

        try
        {
            musicPlayer = MusicPlayer.createInstance(songTable, baseUrl);
        }
        catch (IOException | URISyntaxException e)
        {
            e.printStackTrace();
            // TODO: handle failure to register
        }


        togglePlayButton.setOnAction((event) -> musicPlayer.togglePlay());

        server_selector.setOnAction(event -> musicPlayer.setToServer());
        client_selector.setOnAction(event -> musicPlayer.setToClient());

        CompletableFuture.runAsync(() ->
        {
            try
            {
                getSongs();
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
