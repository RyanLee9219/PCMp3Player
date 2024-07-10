module MusicPlayer {
	requires javafx.controls;
	requires javafx.graphics;
	requires javafx.media;
	requires java.sql;
	
	opens application to javafx.graphics, javafx.fxml;
}
