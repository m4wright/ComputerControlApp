package com.media.player;

import com.media.player.MusicPlayer.MusicPlayer;
import javafx.geometry.Pos;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableView;

import java.io.IOException;


public class PlaySongCell extends TableCell<Song, PlayButton>
{
    private final PlayButton button;
    private final TableView<Song> tableView;


    PlaySongCell(final TableView<Song> tableView, PlayButton button)
    {
        this.button = button;
        this.tableView = tableView;


        button.getStyleClass().add("-fx-background-color: transparent");
        button.setOnAction(event -> {
            System.out.println(getSong());
            try {
                MusicPlayer.instance().togglePlay(getSong());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void updateItem(PlayButton button, boolean empty)
    {
        super.updateItem(button, empty);
        this.setAlignment(Pos.CENTER);
        if (!empty) {
            setGraphic(this.button);
        }
    }

    public PlayButton getButton()
    {
        return button;
    }

    private Song getSong()
    {
        int index = getTableRow().getIndex();
        return tableView.getItems().get(index);
    }
}
