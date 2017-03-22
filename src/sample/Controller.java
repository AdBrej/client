package sample;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.ProgressBarTableCell;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.ForkJoinPool;
import static java.lang.String.format;

public class Controller implements Initializable {

    @FXML
    TableView<Job> pathsTable;
    @FXML
    TableColumn<Job, String> pathColumn;
    @FXML
    TableColumn<Job, Double> progressColumn;
    @FXML
    TableColumn<Job, String> statusColumn;

    private ObservableList<Job> jobs = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pathColumn.setCellValueFactory(job -> new SimpleStringProperty(job.getValue().getPath()));

        statusColumn.setCellValueFactory(job -> job.getValue().getStatusProperty());

        progressColumn.setCellFactory(ProgressBarTableCell.forTableColumn());
        progressColumn.setCellValueFactory(job -> job.getValue().getProgressProperty().asObject());

        pathsTable.setItems(jobs);
    }

    @FXML
    void addFile(ActionEvent event) {
        Window window = pathsTable.getScene().getWindow();


        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Resource File");
        File chosenFile = fileChooser.showOpenDialog(window);
        jobs.add(Job.of(chosenFile.getAbsolutePath()));

    }

    @FXML
    void send(ActionEvent event) {

        ForkJoinPool forkJoinPool = new ForkJoinPool(1);

        forkJoinPool.submit(() -> jobs.parallelStream().forEach(this::send));

    }

    private void send(Job job) {
        Platform.runLater(() -> job.setStatus(Job.STATUS_INIT));


        String path = job.getPath();
        String filename = path.substring(path.lastIndexOf('/') + 1);
        System.out.println(filename);

        try (
                Socket clientSocket = new Socket("localhost", 6666);
                //DataInputStream input = new DataInputStream(clientSocket.getInputStream());
                DataOutputStream output = new DataOutputStream(clientSocket.getOutputStream())
        ){
            output.writeUTF(job.getFile().getName());
            output.flush();
            sendFile(clientSocket, job);

        } catch (IOException e) {
            e.printStackTrace();
        }

        Platform.runLater(() -> job.setStatus(Job.STATUS_DONE));
    }

    private void sendFile(Socket socket, Job job) throws IOException {
        long totalSize = job.getFile().length();

        try (InputStream in = new FileInputStream(job.getFile());
             OutputStream out = socket.getOutputStream()){

            byte[] buffer = new byte[4096];
            int readBytes;
            long totalRead = 0;

            while ((readBytes = in.read(buffer)) != -1) {
                out.write(buffer, 0, readBytes);
                totalRead += readBytes;

                double progress = (double) totalRead / totalSize;
                Platform.runLater(() -> {
                    job.setProgress(progress);
                    job.setStatus(format("%.1f%%", progress * 100));
                });
            }
        }
    }
}
