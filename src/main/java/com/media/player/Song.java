package com.media.player;

import javafx.beans.property.SimpleStringProperty;

public class Song {
    private final SimpleStringProperty artist;
    private final SimpleStringProperty songName;

    public Song(String artist, String songName)
    {
        this.artist = new SimpleStringProperty(artist);
        this.songName = new SimpleStringProperty(songName);
    }

    public String getArtist() {
        return artist.get();
    }

    public SimpleStringProperty artistProperty() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist.set(artist);
    }

    public String getSongName() {
        return songName.get();
    }

    public SimpleStringProperty songNameProperty() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName.set(songName);
    }

    @Override
    public String toString() {
        return String.format("%s by %s", songName.get(), artist.get());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;

        return artist.equals(song.artist) && songName.equals(song.songName);
    }

    @Override
    public int hashCode() {
        int result = artist.hashCode();
        result = 31 * result + songName.hashCode();
        return result;
    }
}
