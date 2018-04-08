package com.media.player.MusicPlayer;

import com.media.player.Song;

import java.io.IOException;

public interface MusicPlayerInterface
{
    void play(Song song) throws IOException;
    void togglePlay() throws IOException;
}
