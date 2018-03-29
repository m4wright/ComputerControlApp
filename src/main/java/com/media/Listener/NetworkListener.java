package com.media.Listener;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.HttpClientBuilder;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.HashMap;

import static spark.Spark.get;

public class NetworkListener
{
    private final HashMap<String, Runnable> commands = new HashMap<>();


    public NetworkListener()
    {
        try
        {
            get("/notify", this::handleMessage);
        } catch (Exception e)
        {
            System.out.println("Caught error");
            System.out.println(e);
        }

        commands.put("done_song", () -> {
            System.out.println("Done song");
        });
    }

    public void register(String location) throws URISyntaxException, IOException
    {
        URIBuilder builder = new URIBuilder(location);
        builder.setParameter("command", "register");
        HttpGet request = new HttpGet(builder.build());
        HttpClient client = HttpClientBuilder.create().build();
        HttpResponse response = client.execute(request);

        HttpEntity entity = response.getEntity();
        String result = IOUtils.toString(entity.getContent());

        // TODO: Handle registration confirmation/potential failure
    }

    private String handleMessage(Request request, Response response)
    {
        String message = request.queryParams("message");
        System.out.println(message);
        return message;
    }
}
