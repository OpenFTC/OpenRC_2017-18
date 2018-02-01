package org.openftc.ftcplugin;

public class FtcExternalResource {
    private final String name;
    private String fileToSend;
    private String remoteSubfolder = "";

    public FtcExternalResource(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public String getFileToSend() {
        return fileToSend;
    }

    public void setFileToSend(String fileToSend) {
        this.fileToSend = fileToSend;
    }

    public String getRemoteSubfolder() {
        return remoteSubfolder;
    }

    public void setRemoteSubfolder(String remoteSubfolder) {
        this.remoteSubfolder = remoteSubfolder;
    }
}
