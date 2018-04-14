package com.media.player.Music;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.media.NetworkConnection.Network;
import com.media.player.Song;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MusicInitializer
{
    private final List<Song> songs = Collections.synchronizedList(new ArrayList<>());
    
    

    public MusicInitializer() throws IOException
    {
        getSongsFromServer();
    }
    
    public List<Song> getMusic() 
    {
        return songs;
    }
    
    
    

    private void getSongsFromServer() throws IOException
    {
        try
        {
            String url = new Network().getServerUrl();
            
            URIBuilder builder = new URIBuilder(url);
            builder.setParameter("command", "get_songs");

            HttpGet request = new HttpGet(builder.build());
            HttpClient client = HttpClientBuilder.create().build();
            HttpResponse response = client.execute(request);
            HttpEntity entity = response.getEntity();

            String result = IOUtils.toString(entity.getContent());
            parseSongInput(result);
        }
        catch (URISyntaxException e)
        {
            throw new IOException(e);
        }
    }
    
    private void parseSongInput(String jsonInput)
    {
        JsonParser parser = new JsonParser();
        JsonObject artists = (JsonObject) parser.parse(jsonInput);

        artists.entrySet().stream().flatMap((Map.Entry<String, JsonElement> artist) ->
        {
            String artistName = artist.getKey();

            return artist.getValue().getAsJsonObject().get("songs")
                    .getAsJsonObject().keySet()
                    .parallelStream().map(song -> new Song(artistName, song));
        }).collect(Collectors.toCollection(() -> songs));
    }
}
