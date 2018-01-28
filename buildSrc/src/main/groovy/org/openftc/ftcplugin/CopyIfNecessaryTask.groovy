package org.openftc.ftcplugin

import org.gradle.api.DefaultTask
import org.gradle.api.PathValidation
import org.gradle.api.tasks.TaskAction

import com.android.ddmlib.*

import java.util.function.Consumer

// TODO: Ensure that the folder exists, and check the filesizes.

/**
 * For every connected device, this task will check to see if the file already exists, and will copy it if it's not there.
 */
class CopyIfNecessaryTask extends DefaultTask {
    def resourcePath

    ExternalResource externalResource

    private AndroidDebugBridge bridge
    private IDevice[] devices

    @TaskAction
    void runTask() throws InterruptedException{
        bridge = SetupFtcResourcesPlugin.bridge
        try {
            waitForAdb()

            devices = bridge.getDevices()

            logDevices()

            setupFirstFolder();

            if(resourceNeedsSending()) {
                sendResource()
            }

        } catch (TimeoutException e) {
            getLogger().warn(e.getMessage())
        }
    }

    private void logDevices() {
        getLogger().info("\n${devices.length} device(s) found:\n")
        for(IDevice device: devices) {
            getLogger().info(device.getProperty(IDevice.PROP_DEVICE_MODEL))
            getLogger().info("-----------------------------")
            getLogger().info("EXTERNAL_STORAGE: ${getExternalStorageDirectory(device)}")
            getLogger().info('')
        }
    }

    // We shouldn't be waiting long at all here, since we started the (asynchronous) process during the configuration phase.
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

    private void setupFirstFolder() {
        runOnAllDevices({ device ->
            FileListingService lister = device.getFileListingService()
            getExternalStorageDirectory(device)
            // TODO: finish this

            // I like the idea of using FileListingService. The problem with it is that we'd have to parse the external
            // storage link ourselves. Better to use something that lets us pass it in directly, especially if it also
            // does checks to make sure the path is good.

            // I take that back. I'd rather do a simple parse of a path than try to find a hacky way to check for a file.
            // There's even a StackOverflow post with code:
            // https://stackoverflow.com/questions/36963532/using-java-how-do-i-get-get-ddmlib-fileentry-objects-for-something-in-the-sdcar
        })
    }

    private void sendResource() {

    }

    private boolean resourceNeedsSending() {
        return false
    }

    private File getResourcePath() {
        project.file(resourcePath, PathValidation.FILE)
    }

    private static String getExternalStorageDirectory(IDevice device) {
        return device.getMountPoint(device.MNT_EXTERNAL_STORAGE)
    }

    private void runOnAllDevices(Consumer<IDevice> task) {
        for (IDevice device: devices) {
            task.accept(device);
        }
    }
}
