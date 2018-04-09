package com.media.player.MusicPlayer;

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

class ServerMusicPlayer implements MusicPlayerInterface
{
    private final String baseUrl;


    ServerMusicPlayer(String baseUrl)
    {
        this.baseUrl = baseUrl;
    }


    @Override
    public void play(Song song) throws IOException
    {
        try
        {
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.setParameter("command", "play_song")
                    .setParameter("song_name", song.getSongName())
                    .setParameter("artist", song.getArtist());
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


    @Override
    public void togglePlay() throws IOException
    {
        try
        {
            URIBuilder builder = new URIBuilder(baseUrl);
            builder.setParameter("command", "toggle_play");
            HttpGet request = new HttpGet(builder.build());
            HttpClient client = HttpClientBuilder.create().build();
            client.execute(request);
        }
        catch (URISyntaxException e)
        {
            e.printStackTrace();
        }
    }
}
