package com.media.player;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.media.player.MusicPlayer.MusicPlayer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
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




    private ObservableList<Song> songs;

    private static String baseUrl;


    private MusicPlayer musicPlayer;




    private static String getBaseUrl()
    {
        final String localServerAddress = "http://192.168.2.25:8080/control_app";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet("http://checkip.amazonaws.com/");
        try
        {
            HttpResponse response = client.execute(request);
            String result = IOUtils.toString(response.getEntity().getContent()).replaceAll("\\s+", "");


            final String serverAddress = "69.157.191.25";
            if (serverAddress.equals(result))
            {
                return localServerAddress;
            }
            else
            {
                return String.format("http://%s:8080/control_app", serverAddress);
            }
        }
        catch (IOException e)
        {
            return localServerAddress;
        }
    }



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




    @FXML
    void initialize() throws ExecutionException, InterruptedException, IOException
    {
        Future<String> getBaseUrlFuture = CompletableFuture.supplyAsync(SongListController::getBaseUrl);



        songTitleColumn.setCellValueFactory(cell -> cell.getValue().songNameProperty());
        songTitleColumn.setComparator(stringComparator());
        songTitleColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        artistColumn.setCellValueFactory(cell -> cell.getValue().artistProperty());
        artistColumn.setComparator(stringComparator());
        artistColumn.setStyle("-fx-alignment: CENTER-LEFT;");

        playSongButtonColumn.setCellFactory(cell -> CreatePlaySongCell.createPlaySongCell(cell.getTableView()));


        baseUrl = getBaseUrlFuture.get();
        System.out.println("Base url is " + baseUrl);

        try
        {
            musicPlayer = MusicPlayer.createInstance(songTable, baseUrl);
        }
        catch (IOException | URISyntaxException e)
        {
            e.printStackTrace();
            // TODO: handle failure to register
        }


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


        server_selector.setSelected(true);
        autoplay_selector.setSelected(true);
        shuffle_selector.setSelected(true);
        musicPlayer.setShuffle(true);



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



