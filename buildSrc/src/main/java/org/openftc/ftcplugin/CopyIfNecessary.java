package org.openftc.ftcplugin;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.FileListingService;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;

import org.gradle.api.DefaultTask;
import org.gradle.api.PathValidation;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.Consumer;

/**
 * For every connected device, this task will check to see if the file already exists, and will copy it if it's not there.
 */
public class CopyIfNecessary extends DefaultTask {
    private final Property<String> localSourcePath;
    private final Property<String> remoteDestinationPath; // TODO: set default value of "" somehow
    private AndroidDebugBridge bridge;
    private WrappedDevice[] devices;

    public CopyIfNecessary() {
        localSourcePath = getProject().getObjects().property(String.class);
        remoteDestinationPath = getProject().getObjects().property(String.class);
    }

    @TaskAction
    public void runTask() throws InterruptedException {

        if (getLocalSourceFile().isDirectory()) {
            throw new InvalidParameterException("Copying an entire directory is not supported at this time.");
        }


        if (!remoteDestinationPath.get().endsWith("/") && remoteDestinationPath.get().length() != 0) {
            throw new InvalidParameterException("Remote subfolder must be blank or end with a slash (/).");
        }


        if (remoteDestinationPath.get().startsWith("/")) {
            throw new InvalidParameterException("Remote subfolder must not begin with a slash.");
        }


        bridge = ExternalFtcResourcesPlugin.getBridge();
        try {
            waitForAdb();

            devices = WrappedDevice.wrapDevices(bridge.getDevices());

            getLogger().info("\n" + String.valueOf(devices.length) + " device(s) found:\n");
            runOnAllDevices(this::logDevice);


            runOnAllDevices(device -> {
                if (resourceNeedsSending(device)) {
                    getLogger().warn(device.getName() + ": Sending resource");
                    sendResource(device);
                } else {
                    getLogger().warn(device.getName() + ": Skipping resource");
                }
            });

        } catch (TimeoutException e) {
            getLogger().warn(e.getMessage());
        }

    }

    // We shouldn't be waiting long at all here, since we started the process during the configuration phase.
    private void waitForAdb() throws InterruptedException, TimeoutException {
        long timeOut = 10000;// 10 sec
        int sleepTime = 500;
        while (!bridge.hasInitialDeviceList() && timeOut > 0) {
            Thread.sleep(sleepTime);
            timeOut -= sleepTime;
        }


        if (timeOut <= 0 && !bridge.hasInitialDeviceList()) {
            throw new TimeoutException("Timed out while getting device list.");
        }

    }

    private void logDevice(final WrappedDevice device) {
        getLogger().info(device.getName());
        getLogger().info("-----------------------------");
        getLogger().info("EXTERNAL_STORAGE: " + device.getExternalStorageDirectory());
        getLogger().info("");
    }

    private boolean resourceNeedsSending(WrappedDevice device) {
        FileListingService listingService = device.getFileListingService();
        FileListingService.FileEntry result = listingService.getRoot();
        // We have verified that all of these values end with a slash.
        String fileToCheck = getCompleteRemotePath(device);

        String[] pathSegments = fileToCheck.split("/");

        for (String segment : pathSegments) {
            if (segment.length() == 0) {
                continue;
            }

            listingService.getChildren(result, false, null);
            result = result.findChild(segment);
            if (result == null) {
                return true; // Send the file if it doesn't exist
            }
        }

        if (result.getSizeValue() != getLocalSourceFile().length()) {
            return true; // Send the file if the sizes don't match
        }

        // If the phone's time is set sufficiently far back, this will cause the file to be pushed every single
        // time. If the computer's time is set sufficiently far back, the file may never get pushed unless the
        // filesizes don't match (which should be pretty rare when a push is warranted anyway).
        LocalDateTime remoteFileTime = LocalDateTime.parse(result.getDate() + "T" + result.getTime());
        LocalDateTime localFileTime = Instant.ofEpochMilli(getLocalSourceFile().lastModified())
                .atZone(ZoneId.systemDefault()).toLocalDateTime();

        return localFileTime.isAfter(remoteFileTime);

        //It would be nice to do a checksum here, but you can't run md5sum from the ZTE Speed shell.
    }

    private String getCompleteRemotePath(WrappedDevice device) {
        return device.getFirstDirectory() + remoteDestinationPath.get() + getLocalSourceFile().getName();
    }

    private void sendResource(WrappedDevice device) {
        try {
            device.pushFile(getLocalSourceFile().toString(), getCompleteRemotePath(device));
        } catch (IOException e1) { // TODO: Handle these
            e1.printStackTrace();
        } catch (AdbCommandRejectedException e1) {
            e1.printStackTrace();
        } catch (TimeoutException e1) {
            e1.printStackTrace();
        } catch (SyncException e1) {
            e1.printStackTrace();
        }
    }

    private File getLocalSourceFile() {
        return getProject().file(localSourcePath.get(), PathValidation.FILE);
    }

    private void runOnAllDevices(Consumer<WrappedDevice> task) {
        for (WrappedDevice device : devices) {
            task.accept(device);
        }

    }

    public Property<String> getLocalSourcePath() {
        return localSourcePath;
    }

    public Property<String> getRemoteDestinationPath() {
        return remoteDestinationPath;
    }

    // TODO: reorganize methods
}
