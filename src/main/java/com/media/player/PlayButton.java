package com.media.player;

import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class PlayButton extends Button
{
    private final String STYLE_NORMAL = "-fx-background-color: transparent; -fx-padding: 5, 5, 5, 5;";
    private final String STYLE_PRESSED = "-fx-background-color: transparent; -fx-padding: 6 4 4 6;";

    private Image mainIcon;
    private Image selectedIcon;



    private boolean selected = false;

    public PlayButton(Image mainIcon, Image selectedIcon)
    {
        this.mainIcon = mainIcon;
        this.selectedIcon = selectedIcon;

        setGraphic(new ImageView(mainIcon));
        setStyle(STYLE_NORMAL);

        setOnMouseReleased(event -> setStyle(STYLE_NORMAL));

        setOnMousePressed(event -> {
            setStyle(STYLE_PRESSED);
//            if (selected)
//            {
//                setGraphic(new ImageView(mainIcon));
//                selected = false;
//            }
//            else
//            {
//                setGraphic(new ImageView(selectedIcon));
//                selected = true;
//            }
        });
    }

    public void select(boolean selected)
    {
        if (selected)
        {
            setGraphic(new ImageView(selectedIcon));
            this.selected = true;
        }
        else
            {
            setGraphic(new ImageView(mainIcon));
            this.selected = false;
        }
    }



    @Override
    public boolean equals(Object o) {
        return this == o;
    }

    @Override
    public int hashCode() {
        return (selected ? 1 : 0);
    }
}
