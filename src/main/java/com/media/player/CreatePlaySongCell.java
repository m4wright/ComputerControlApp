package com.media.player;

import javafx.scene.control.TableView;
import javafx.scene.image.Image;

class CreatePlaySongCell
{
    private static final Image mainIcon;
    private static final Image selectedIcon;


    static
    {
        mainIcon = new Image("/Images/table_play_icon.png");
        selectedIcon = new Image("/Images/table_paused_icon.png");
    }

    public static PlaySongCell createPlaySongCell(final TableView<Song> tableView)
    {
        PlayButton button = new PlayButton(mainIcon, selectedIcon);
        return new PlaySongCell(tableView, button);
    }
}
