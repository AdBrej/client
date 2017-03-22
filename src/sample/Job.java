package sample;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;

import java.io.File;


/**
 * Created by Brej on 22.03.2017.
 */
public class Job {

        public static final String STATUS_WAITING = "waiting";
        public static final String STATUS_INIT = "initializing";
        public static final String STATUS_DONE = "done";

        final String path;
        final SimpleStringProperty status;
        final DoubleProperty progress;

        public File getFile() {
            return file;
        }

        final File file;

        public Job(String path) {
            this.path = path;
            this.file = new File(path);
            this.status = new SimpleStringProperty(STATUS_WAITING);
            this.progress = new SimpleDoubleProperty(0);
        }

        public static Job of(String path) {
            try {
                Job job = new Job(path);
                return job;

            } catch (Exception e) {
                return null;
            }
        }

        public String getPath() {
            return path;
        }

        public Double getProgress() {
            return progress.get();
        }

        public void setProgress(Double progress) {
            this.progress.set(progress);
        }

        public String getStatus() {
            return this.status.get();
        }

        public void setStatus(String status) {
            this.status.set(status);
        }

        public DoubleProperty getProgressProperty() {
            return progress;
        }

        public SimpleStringProperty getStatusProperty() {
            return status;
        }
}
