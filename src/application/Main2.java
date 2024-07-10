package application;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;

import javafx.util.Callback;
import javafx.application.Application;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.stage.Stage;
import javafx.util.Duration;

import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;

public class Main2 extends Application {
	Connection conn;

    private MediaPlayer mediaPlayer;
    private String currentPlayingPath = "";
    private MediaView mediaView;
    private Slider timeSlider;
    private Label timeLabel;
    private Label currentPlayingLabel;

    @Override
    public void start(Stage primaryStage) {
        ListView<MusicData> lv = new ListView<>();
        ObservableList<MusicData> ov = FXCollections.observableArrayList();
        lv.setItems(ov);
        
        lv.setCellFactory(new Callback<ListView<MusicData>, ListCell<MusicData>>() {
            @Override
            public ListCell<MusicData> call(ListView<MusicData> listView) {
                return new MusicDataCell();
            }
        });
        
        
        TextField addTf = new TextField();
        addTf.setPromptText("Drag and Drop mp3 file here");
        addTf.setPrefWidth(200);
        //drag and drop 
        addTf.setOnDragOver(event -> {
            if (event.getGestureSource() != addTf && event.getDragboard().hasFiles()) {
                event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
            }
            event.consume();
        });
        
        addTf.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;
            if (db.hasFiles()) {
                File file = db.getFiles().get(0);
                if (file.getAbsolutePath().endsWith(".mp3")|| file.getAbsolutePath().endsWith(".wav")) {
                    addTf.setText(file.getAbsolutePath());
                    success = true;
                }
            }
            event.setDropCompleted(success);
            event.consume();
        });


        Button addbt = new Button("Add");
        Button removebt = new Button("Remove");

        mediaView = new MediaView();
        mediaView.setFitWidth(100);
        mediaView.setFitHeight(50);

        timeLabel = new Label("00:00 / 00:00");
        timeLabel.setAlignment(Pos.CENTER);

        timeSlider = new Slider();
        timeSlider.setMin(0);
        timeSlider.setValue(0);
        timeSlider.setMax(100);

        timeSlider.setOnMouseClicked(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.seek(Duration.seconds(timeSlider.getValue()));
            }
        });
        //music playing display
        currentPlayingLabel = new Label("No music playing");
        currentPlayingLabel.setAlignment(Pos.CENTER);
        
      //Database section
        conn = dbConnector();
        ov.addAll(new MusicPlayerDb(conn).loadData());

        //button
        Button playButton = new Button("▶");
        Button stopButton = new Button("■");
        Button pauseButton = new Button("⏸");
        Button nextButton = new Button("⏭");
        Button prevButton = new Button("⏮");
        
        addbt.setOnAction(e -> {
            String filePath = addTf.getText().trim();
            File file = new File(filePath);
            if (file.exists()) {
                String name = file.getName();
                String path = file.getAbsolutePath();

                MusicData data = new MusicData(name, path);
                ov.add(data);
                addTf.clear();
                new MusicPlayerDb(conn).insertData(path);
            } else {
                System.out.println("File does not exist: " + filePath);
            }
        });

        removebt.setOnAction(e -> {
            MusicData selected = lv.getSelectionModel().getSelectedItem();
            if (selected != null) {
                ov.remove(selected);
                
                new MusicPlayerDb(conn).deleteData(selected.getPath());
            }
        });

        playButton.setOnAction(e -> {
            MusicData selected = lv.getSelectionModel().getSelectedItem();
            if (selected != null) {
                String path = selected.getPath();
                String name = selected.getname();
                if (!path.equals(currentPlayingPath)) {
                    initializeMediaPlayer(path,name);
                }
                mediaPlayer.play();
            }
        });

        pauseButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.pause();
            }
        });

        stopButton.setOnAction(e -> {
            if (mediaPlayer != null) {
                mediaPlayer.stop();
            }
        });
        
        nextButton.setOnAction(e -> {
            int currentIndex = lv.getSelectionModel().getSelectedIndex();
            if (currentIndex < ov.size() - 1) {
                lv.getSelectionModel().select(currentIndex + 1);
                MusicData nextMusic = lv.getSelectionModel().getSelectedItem();
                if (nextMusic != null) {
                    initializeMediaPlayer(nextMusic.getPath(), nextMusic.getname());
                    mediaPlayer.play();
                }
            }
        });

        prevButton.setOnAction(e -> {
            int currentIndex = lv.getSelectionModel().getSelectedIndex();
            if (currentIndex > 0) {
                lv.getSelectionModel().select(currentIndex - 1);
                MusicData prevMusic = lv.getSelectionModel().getSelectedItem();
                if (prevMusic != null) {
                    initializeMediaPlayer(prevMusic.getPath(),prevMusic.getname());
                    mediaPlayer.play();
                }
            }
        });
        
        

        HBox pathBox = new HBox(addTf, addbt, removebt);
        pathBox.setSpacing(10);
        pathBox.setAlignment(Pos.CENTER);

        HBox controlBox = new HBox(prevButton,playButton, pauseButton, stopButton,nextButton);
        controlBox.setSpacing(10);
        controlBox.setAlignment(Pos.CENTER);

        VBox root = new VBox();
        root.getChildren().addAll(pathBox,lv, currentPlayingLabel, mediaView, timeLabel, timeSlider, controlBox);
        root.setSpacing(10);
        root.setPadding(new Insets(10));
        lv.setPrefHeight(300);
        root.setAlignment(Pos.CENTER); 

        Scene scene = new Scene(root, 400, 400);
        scene.getStylesheets().add(getClass().getResource("application.css").toExternalForm());
        primaryStage.setScene(scene);
        primaryStage.setTitle("Ryan's Music Player");
        primaryStage.show();
    }

    private void initializeMediaPlayer(String path,String name) {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
        Media media = new Media(new File(path).toURI().toString());
        mediaPlayer = new MediaPlayer(media);

        mediaPlayer.setOnReady(() -> {
            timeSlider.setMax(mediaPlayer.getTotalDuration().toSeconds());
            updateTimeLabel();
            currentPlayingLabel.setText("Now Playing: " + name);
        });

        mediaPlayer.currentTimeProperty().addListener(new ChangeListener<Duration>() {
            @Override
            public void changed(ObservableValue<? extends Duration> observable, Duration oldValue, Duration newValue) {
                timeSlider.setValue(newValue.toSeconds());
                updateTimeLabel();
            }
        });

        currentPlayingPath = path;
    }

    private void updateTimeLabel() {
        if (mediaPlayer != null) {
            Duration currentTime = mediaPlayer.getCurrentTime();
            Duration totalDuration = mediaPlayer.getTotalDuration();

            String timeLabelString = String.format("%02d:%02d / %02d:%02d",
                    (int) currentTime.toMinutes(),
                    (int) currentTime.toSeconds() % 60,
                    (int) totalDuration.toMinutes(),
                    (int) totalDuration.toSeconds() % 60);

            timeLabel.setText(timeLabelString);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
    
    public Connection dbConnector() {
    	
    	try {
    		if(conn==null) {
    			Class.forName("org.sqlite.JDBC");
    			conn = DriverManager.getConnection("jdbc:sqlite:sql/MusicPlayer.sqlite");
    			return conn;
    		}else {
    			return conn;
    		}
    	}catch (Exception e) {
    		System.out.println();
			return null;
		}
    }
}
