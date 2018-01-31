package org.openftc.ftcplugin

import org.gradle.api.DefaultTask
import org.gradle.api.PathValidation
import org.gradle.api.tasks.TaskAction

import com.android.ddmlib.*

import java.security.InvalidParameterException
import java.util.function.Consumer

// TODO: Test updated file (new filesize)
// TODO: Test with nonexistent FIRST folder

/**
 * For every connected device, this task will check to see if the file already exists, and will copy it if it's not there.
 */
class CopyIfNecessaryTask extends DefaultTask {
    def localSourcePath
    String remoteDestinationPath = ''

    private AndroidDebugBridge bridge
    private WrappedDevice[] devices

    @TaskAction
    void runTask() throws InterruptedException{

        if(getLocalSourceFile().isDirectory()) {
            throw new InvalidParameterException("Copying an entire directory is not supported at this time.")
        }

        if(!remoteDestinationPath.endsWith('/') && remoteDestinationPath.length() != 0) {
            throw new InvalidParameterException("Remote destination path must be blank or end with a slash (/).")
        }

        if(remoteDestinationPath.startsWith('/')) {
            throw new InvalidParameterException("Remote destination path must not begin with a slash.")
        }


        bridge = SetupFtcResourcesPlugin.bridge
        try {
            waitForAdb()

            devices = WrappedDevice.wrapDevices(bridge.getDevices())

            getLogger().info("\n${devices.length} device(s) found:\n")
            runOnAllDevices({device ->
                logDevice(device)
            })


            runOnAllDevices({ device ->
                if(resourceNeedsSending(device)) {
                    logger.info("${device.getName()}: Sending resouce")
                    sendResource(device)
                } else {
                    logger.info("${device.getName()}: Skipping resouce")
                }
            })

        } catch (TimeoutException e) {
            getLogger().warn(e.getMessage())
        }
    }

    // We shouldn't be waiting long at all here, since we started the process during the configuration phase.
    private void waitForAdb() throws InterruptedException, TimeoutException {
        long timeOut = 10000 // 10 sec
        int sleepTime = 500
        while (!bridge.hasInitialDeviceList() && timeOut > 0) {
            Thread.sleep(sleepTime)
            timeOut -= sleepTime
        }

        if(timeOut <= 0 && !bridge.hasInitialDeviceList()) {
            throw new TimeoutException("Timed out while getting device list.")
        }
    }

    protected void logDevice(WrappedDevice device) {
        getLogger().info(device.getName())
        getLogger().info("-----------------------------")
        getLogger().info("EXTERNAL_STORAGE: ${device.getExternalStorageDirectory()}")
        getLogger().info('')
    }

    protected boolean resourceNeedsSending(WrappedDevice device) {
        FileListingService listingService = device.getFileListingService()
        FileListingService.FileEntry result = listingService.root
        // We have verified that all of these values end with a slash.
        String fileToCheck = getCompleteRemotePath(device)

        String[] pathSegments = fileToCheck.split('/')

        for (String segment: pathSegments) {
            if(segment.length() == 0) {
                continue
            }
            listingService.getChildren(result, false, null)
            result = result.findChild(segment)
            if(result == null) {
                return true
            }
        }

        if(result.sizeValue == getLocalSourceFile().size()) {
            return false
        } else {
            return true
        }
    }

    protected String getCompleteRemotePath(WrappedDevice device) {
        return device.getFirstDirectory() + remoteDestinationPath + getLocalSourceFile().name
    }

    protected void sendResource(WrappedDevice device) {
        device.pushFile(getLocalSourceFile().toString(), getCompleteRemotePath(device))
    }

    protected File getLocalSourceFile() {
        project.file(localSourcePath, PathValidation.FILE)
    }

    private void runOnAllDevices(Consumer<WrappedDevice> task) {
        for (WrappedDevice device: devices) {
            task.accept(device)
        }
    }

    // TODO: reorganize methods

    private class SyncProgressMonitor implements SyncService.ISyncProgressMonitor {

        @Override
        void start(int totalWork) {

        }

        @Override
        void stop() {

        }

        @Override
        boolean isCanceled() {
            return false
        }

        @Override
        void startSubTask(String name) {

        }

        @Override
        void advance(int work) {

        }
    }
}
