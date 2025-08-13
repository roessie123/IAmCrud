package com.crud.ithreeamcrud;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ListChangeListener;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.nio.file.Files;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;


public class MainScene {
    public Label fileNameLabel;
    public Label filePathLabel;
    public Label fileTypeLabel;
    public Label fileDateLabel;


    public MainScene() {

    }



    @FXML
    private Button sourceTabCreateBtn;

    private Stage getStage() {
        return (Stage) sourceTabCreateBtn.getScene().getWindow();
    }

    private void updateSourceTable() {

    }

    @FXML
    protected void onCreateBtnClick_sourceTab() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select a Folder");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedDir = dirChooser.showDialog(getStage());
        if (selectedDir != null) {
            folderData.add(new FolderEntry(selectedDir.getName(), selectedDir.getAbsolutePath(), selectedDir));

        }
    }

    private ArrayList<File> targetFolders;

    /// ////////////////////////////////////////////////////////////
    @FXML
    private TableView<FolderEntry> sourceTabTable;

    @FXML
    private TableColumn<FolderEntry, String> folderNameColumn;

    @FXML
    private TableColumn<FolderEntry, String> pathColumn;

    private final ObservableList<FolderEntry> folderData = FXCollections.observableArrayList();


    @FXML
    public void initialize() {

        // Link columns to FolderEntry getters
        folderNameColumn.setCellValueFactory(new PropertyValueFactory<>("folderName"));
        pathColumn.setCellValueFactory(new PropertyValueFactory<>("path"));
        folderNameColumnTarget.setCellValueFactory(new PropertyValueFactory<>("folderName"));
        pathColumnTarget.setCellValueFactory(new PropertyValueFactory<>("path"));
        // Set data list to table
        sourceTabTable.setItems(folderData);
        fileListContainer.setItems(fileData);

        targetTabTable.setItems(targetFolderData);

        folderData.addListener((ListChangeListener<FolderEntry>) change -> {
            while (change.next()) {
                saveSourceFolders(folderData);

                if (change.wasAdded()) {
                    updateFilesToSearch();
                }
                if (change.wasRemoved()) {
                    for (FolderEntry removedFolder : change.getRemoved()) {
                        deleteFilesToSearch(removedFolder.getFolder());
                    }
                }
            }
        });
        targetFolderData.addListener((ListChangeListener<FolderEntry>) change -> {
            while (change.next()) {
                saveTargetFolders(targetFolderData);
            }
        });

        fileListContainer.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fileNameLabel.setText(newVal.getName());
                filePathLabel.setText(newVal.getPath());
                try {
                    fileTypeLabel.setText(Files.probeContentType(newVal.toPath()));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                long lastModifiedMillis= newVal.lastModified();
                LocalDateTime lastModifiedDate = Instant.ofEpochMilli(lastModifiedMillis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDateTime();
                fileDateLabel.setText(String.valueOf(lastModifiedDate));
            }
        });


        sourceTabTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        loadInto(folderData);
        loadIntoTarget(targetFolderData);
    }

    private static final File SAVE_FILE = new File("folderData.json");
    private static final ObjectMapper mapper = new ObjectMapper();

    public static void saveSourceFolders(ObservableList<FolderEntry> data) {
        try {
            mapper.writerWithDefaultPrettyPrinter().writeValue(SAVE_FILE, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void loadInto(ObservableList<FolderEntry> list) {
        if (SAVE_FILE.exists()) {
            try {
                List<FolderEntry> loaded = mapper.readValue(
                        SAVE_FILE,
                        new TypeReference<>() {}
                );
                list.setAll(loaded);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    Button deleteBtnSourceFolder;

    @FXML
    private void onRemoveSelectedClick() {
        FolderEntry selected = sourceTabTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            folderData.remove(selected);
        }
    }

    @FXML
    Button ReadBtnSourceFolder;

    @FXML
    private void onClickReadBtn() throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(CRUD_Application.class.getResource("read-meme-scene.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1280,720);
        Stage memeStage = new Stage();
        memeStage.setTitle("READ THE DAMN TITLE");
        memeStage.setScene(scene);
        memeStage.show();
    }

    @FXML
    Button updateBtnSourceFolder;

    @FXML
    private void onUpdateSelectedClick() {
        FolderEntry selected = sourceTabTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select a Folder");
            dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            File selectedDir = dirChooser.showDialog(getStage());
            if (selectedDir != null) {
                folderData.add(new FolderEntry(selectedDir.getName(), selectedDir.getAbsolutePath(), selectedDir));

            }
            folderData.remove(selected);
        }
    }
    /// ///////////////////////////////////////////////////////////
    /// Source tab
    /// ///////////////////////////////////////////////////////////

    private final ObservableList<File> fileData = FXCollections.observableArrayList();

    @FXML
    private ListView<File> fileListContainer;


    void updateFilesToSearch(){
        folderData.forEach(folder->{
            File tmpFolder = folder.getFolder();
            Stream<File> files = Arrays.stream(tmpFolder.listFiles()).filter(file -> file.isFile());

            files.forEach(fileToAdd -> {
                if (fileData.stream().anyMatch(fileIs -> fileIs==fileToAdd)){
                    return ;
                }
                fileData.add(fileToAdd);
            });
        });
    }

    void deleteFilesToSearch(File Folder){
        Stream<File> files = Arrays.stream(Folder.listFiles()).filter(file -> file.isFile());

        files.forEach(fileToRemove -> {
            if (fileData.stream().anyMatch(fileIs -> fileIs.equals(fileToRemove))){
                fileData.remove(fileToRemove); 
            }
        });

    }

    /// /////////////////////////////////////////////////////////////////
    /// target folder tab
    /// //////////////////////////////////////////////////////////////
    public Button targetTabCreateBtn;
    public Button ReadBtnTargetFolder;
    public Button updateBtnTargetFolder;
    public Button deleteBtnTargetFolder;
    public TableView<FolderEntry> targetTabTable;
    public TableColumn<FolderEntry, String> folderNameColumnTarget;
    public TableColumn<FolderEntry, String> pathColumnTarget;


    private final ObservableList<FolderEntry> targetFolderData = FXCollections.observableArrayList();

    @FXML
    private void onUpdateSelectedClickTargetTab() {
        FolderEntry selected = targetTabTable.getSelectionModel().getSelectedItem();

        if (selected != null) {
            DirectoryChooser dirChooser = new DirectoryChooser();
            dirChooser.setTitle("Select a Folder");
            dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));

            File selectedDir = dirChooser.showDialog(getStage());
            if (selectedDir != null) {
                targetFolderData.add(new FolderEntry(selectedDir.getName(), selectedDir.getAbsolutePath(), selectedDir));

            }
            targetFolderData.remove(selected);
        }
    }
    @FXML
    private void onRemoveSelectedClickTargetTab() {
        FolderEntry selected = targetTabTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            targetFolderData.remove(selected);
        }
    }
    @FXML
    protected void onCreateBtnClick_targetTab() {
        DirectoryChooser dirChooser = new DirectoryChooser();
        dirChooser.setTitle("Select a Folder");
        dirChooser.setInitialDirectory(new File(System.getProperty("user.home")));

        File selectedDir = dirChooser.showDialog(getStage());
        if (selectedDir != null) {
            targetFolderData.add(new FolderEntry(selectedDir.getName(), selectedDir.getAbsolutePath(), selectedDir));

        }
    }

    private static final File TARGET_SAVE_FILE = new File("targetFolderData.json");
    private static final ObjectMapper mapper2 = new ObjectMapper();

    public static void saveTargetFolders(ObservableList<FolderEntry> data) {
        try {
            mapper2.writerWithDefaultPrettyPrinter().writeValue(TARGET_SAVE_FILE, data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void loadIntoTarget(ObservableList<FolderEntry> list) {
        if (TARGET_SAVE_FILE.exists()) {
            try {
                List<FolderEntry> loaded = mapper2.readValue(
                        TARGET_SAVE_FILE,
                        new TypeReference<>() {}
                );
                list.setAll(loaded);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


}
