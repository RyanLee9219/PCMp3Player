package application;

import javafx.scene.control.ListCell;
import javafx.scene.input.MouseEvent;

public class MusicDataCell extends ListCell<MusicData> {

    public MusicDataCell() {
        super();
        
        // hovering
        setOnMouseEntered((MouseEvent event) -> {
            if (!isEmpty()) {
                setStyle("-fx-background-color: #f0f0f0; -fx-text-fill: black;");
            }
        });

        setOnMouseExited((MouseEvent event) -> {
            if (!isEmpty()) {
                setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white;");
            }
        });
    }

    @Override
    protected void updateItem(MusicData item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setStyle(""); // Reset the style
        } else {
            setText(item.getname());
            setStyle("-fx-background-color: #3c3c3c; -fx-text-fill: white;"); // Apply custom style
        }
    }
}
