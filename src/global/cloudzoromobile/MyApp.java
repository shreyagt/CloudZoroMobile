package global.cloudzoromobile;

import android.app.Application;

public class MyApp extends Application {

private String fileToDownload;

public String getfileToDownload() {
    return fileToDownload;
}

public void setfileToDownload(String fileToDownload) {
    this.fileToDownload = fileToDownload;
}
}