package com.media.NetworkConnection;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class Network
{
    private static CompletableFuture<String> serverUrlFuture = null;
    private static CompletableFuture<Boolean> isRemoteAddressFuture = new CompletableFuture<>();

    public Network()
    {
        if (serverUrlFuture == null)
        {
            serverUrlFuture = CompletableFuture.supplyAsync(() -> {
                try
                {
                    return computeBaseUrl();
                }
                catch (IOException e)
                {
                    throw new CompletionException(e);
                }
            });
        }
    }

    public String getServerUrl() throws IOException {
        return getServerUrl(8080);
    }


    public String getServerUrl(int port) throws IOException
    {
        try
        {
            return String.format("http://%s:%d/control_app/", serverUrlFuture.get(), port);
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new IOException(e);
        }
    }

    public boolean isRemoteServer() throws IOException
    {
        try
        {
            return isRemoteAddressFuture.get();
        }
        catch (InterruptedException | ExecutionException e)
        {
            throw new IOException(e);
        }
    }





    private static String computeBaseUrl() throws IOException
    {
        final String remoteServerAddress = "69.157.191.25";
        final String localServerAddress = "192.168.2.25";

        final String getExternalAddressUrl = "http://checkip.amazonaws.com/";
        HttpClient client = HttpClientBuilder.create().build();
        HttpGet request = new HttpGet(getExternalAddressUrl);
        HttpResponse response = client.execute(request);
        String remoteClientAddress = IOUtils.toString(response.getEntity().getContent()).replaceAll("\\s+", "");

        String serverAddress;

        if (remoteServerAddress.equals(remoteClientAddress))
        {
            serverAddress = localServerAddress;
            isRemoteAddressFuture.complete(false);
        }
        else
        {
            serverAddress = remoteServerAddress;
            isRemoteAddressFuture.complete(true);
        }

        return serverAddress;
    }
}
