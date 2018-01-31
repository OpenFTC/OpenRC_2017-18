package org.openftc.ftcplugin

import com.android.annotations.NonNull
import com.android.ddmlib.AdbCommandRejectedException
import com.android.ddmlib.Client
import com.android.ddmlib.FileListingService
import com.android.ddmlib.IDevice
import com.android.ddmlib.IShellOutputReceiver
import com.android.ddmlib.InstallException
import com.android.ddmlib.RawImage
import com.android.ddmlib.ScreenRecorderOptions
import com.android.ddmlib.ShellCommandUnresponsiveException
import com.android.ddmlib.SyncException
import com.android.ddmlib.SyncService
import com.android.ddmlib.TimeoutException
import com.android.ddmlib.log.LogReceiver
import com.android.sdklib.AndroidVersion
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

import java.util.concurrent.Future
import java.util.concurrent.TimeUnit

//@CompileStatic
@TypeChecked
class WrappedDevice implements IDevice {
    IDevice device
    private String externalStorageDirectory = ''
    private String firstDirectory = ''
    private boolean isInitialized = false

    WrappedDevice(IDevice device) {
        this.device = device
    }

    static WrappedDevice[] wrapDevices(IDevice[] devices) {
        WrappedDevice[] wrappedDevices = new WrappedDevice[devices.length]
        for(int i = 0; i < devices.length; i++) {
            wrappedDevices[i] = new WrappedDevice(devices[i])
        }
        return wrappedDevices
    }

    /**
     * @return the device's external storage directory, with a trailing slash.
     */
    String getExternalStorageDirectory() {
        initIfNecessary()
        return externalStorageDirectory
    }

    /**
     * @return the device's FIRST directory, with a trailing slash
     */
    String getFirstDirectory() {
        initIfNecessary()
        return firstDirectory
    }

    private void initIfNecessary() {
        if(!isInitialized) {
            initExternalStorageDir()
            firstDirectory = "${externalStorageDirectory}FIRST/"
            isInitialized = true
        }
    }

    private void initExternalStorageDir() {
        externalStorageDirectory = device.getMountPoint(IDevice.MNT_EXTERNAL_STORAGE)
        if(!externalStorageDirectory.endsWith("/")) {
            externalStorageDirectory = "${externalStorageDirectory}/"
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////
    //                        Passthrough IDevice definitions                         //
    ///////////////////////////////////////////////////////////////////////////////////

    /** Returns the serial number of the device. */
    @Override
    String getSerialNumber() {
        return device.getSerialNumber()
    }

    /**
     * Returns the name of the AVD the emulator is running.
     * <p>This is only valid if {@link #isEmulator()} returns true.
     * <p>If the emulator is not running any AVD (for instance it's running from an Android source
     * tree build), this method will return "<code>&lt;build&gt;</code>".
     *
     * @return the name of the AVD or <code>null</code> if there isn't any.
     */
    @Override
    String getAvdName() {
        return device.getAvdName()
    }

    /**
     * Returns the state of the device.
     */
    @Override
    IDevice.DeviceState getState() {
        return device.getState()
    }

    /** @deprecated */
    @Deprecated
    @Override
    Map<String, String> getProperties() {
        return device.getProperties()
    }

    /** @deprecated */
    @Deprecated
    @Override
    int getPropertyCount() {
        return device.getPropertyCount()
    }

    /**
     * Convenience method that attempts to retrieve a property via
     * {@link #getSystemProperty(String)} with a very short wait time, and swallows exceptions.
     *
     * <p><em>Note: Prefer using {@link #getSystemProperty(String)} if you want control over the
     * timeout.</em>
     *
     * @param name the name of the value to return.
     * @return the value or <code>null</code> if the property value was not immediately available
     */
    @Override
    String getProperty(@NonNull String name) {
        return device.getProperty(name)
    }

    /**
     * Returns <code>true</code> if properties have been cached
     */
    @Override
    boolean arePropertiesSet() {
        return device.arePropertiesSet()
    }

    /** @deprecated */
    @Deprecated
    @Override
    String getPropertySync(String name) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        return device.getPropertySync(name)
    }

    /** @deprecated */
    @Deprecated
    @Override
    String getPropertyCacheOrSync(String name) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        return device.getPropertyCacheOrSync(name)
    }

    /** Returns whether this device supports the given software feature. */
    @Override
    boolean supportsFeature(IDevice.Feature feature) {
        return device.supportsFeature(feature)
    }

    /** Returns whether this device supports the given hardware feature. */
    @Override
    boolean supportsFeature(IDevice.HardwareFeature feature) {
        return device.supportsFeature(feature)
    }

    /**
     * Returns a mount point.
     *
     * @param name the name of the mount point to return
     *
     * @see #MNT_EXTERNAL_STORAGE
     * @see #MNT_ROOT
     * @see #MNT_DATA
     */
    @Override
    String getMountPoint(String name) {
        return device.getMountPoint(name)
    }

    /**
     * Returns if the device is ready.
     *
     * @return <code>true</code> if {@link #getState()} returns {@link DeviceState#ONLINE}.
     */
    @Override
    boolean isOnline() {
        return device.isOnline()
    }

    /**
     * Returns <code>true</code> if the device is an emulator.
     */
    @Override
    boolean isEmulator() {
        return device.isEmulator()
    }

    /**
     * Returns if the device is offline.
     *
     * @return <code>true</code> if {@link #getState()} returns {@link DeviceState#OFFLINE}.
     */
    @Override
    boolean isOffline() {
        return device.isOffline()
    }

    /**
     * Returns if the device is in bootloader mode.
     *
     * @return <code>true</code> if {@link #getState()} returns {@link DeviceState#BOOTLOADER}.
     */
    @Override
    boolean isBootLoader() {
        return device.isBootLoader()
    }

    /**
     * Returns whether the {@link Device} has {@link Client}s.
     */
    @Override
    boolean hasClients() {
        return device.hasClients()
    }

    /**
     * Returns the array of clients.
     */
    @Override
    Client[] getClients() {
        return device.getClients()
    }

    /**
     * Returns a {@link Client} by its application name.
     *
     * @param applicationName the name of the application
     * @return the <code>Client</code> object or <code>null</code> if no match was found.
     */
    @Override
    Client getClient(String applicationName) {
        return device.getClient(applicationName)
    }

    /**
     * Returns a {@link SyncService} object to push / pull files to and from the device.
     *
     * @return <code>null</code> if the SyncService couldn't be created. This can happen if adb
     *            refuse to open the connection because the {@link IDevice} is invalid
     *            (or got disconnected).
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws IOException if the connection with adb failed.
     */
    @Override
    SyncService getSyncService() throws TimeoutException, AdbCommandRejectedException, IOException {
        return device.syncService
    }

    /**
     * Returns a {@link FileListingService} for this device.
     */
    @Override
    FileListingService getFileListingService() {
        return device.fileListingService
    }

    /**
     * Takes a screen shot of the device and returns it as a {@link RawImage}.
     *
     * @return the screenshot as a <code>RawImage</code> or <code>null</code> if something
     *            went wrong.
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws IOException in case of I/O error on the connection.
     */
    @Override
    RawImage getScreenshot() throws TimeoutException, AdbCommandRejectedException, IOException {
        return device.getScreenshot()
    }

    @Override
    RawImage getScreenshot(long timeout, TimeUnit unit) throws TimeoutException, AdbCommandRejectedException, IOException {
        return device.getScreenshot(timeout, unit)
    }

    /**
     * Initiates screen recording on the device if the device supports {@link Feature#SCREEN_RECORD}.
     *
     * @param remoteFilePath
     * @param options
     * @param receiver
     */
    @Override
    void startScreenRecorder(String remoteFilePath, ScreenRecorderOptions options, IShellOutputReceiver receiver) throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        device.startScreenRecorder(remoteFilePath, options, receiver)
    }

    /** @deprecated */
    @Deprecated
    @Override
    void executeShellCommand(String command, IShellOutputReceiver receiver, int maxTimeToOutputResponse) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        device.executeShellCommand(command, receiver, maxTimeToOutputResponse)
    }

    /**
     * Executes a shell command on the device, and sends the result to a <var>receiver</var>
     * <p>This is similar to calling
     * <code>executeShellCommand(command, receiver, DdmPreferences.getTimeOut())</code>.
     *
     * @param command the shell command to execute
     * @param receiver the {@link IShellOutputReceiver} that will receives the output of the shell
     *            command
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws ShellCommandUnresponsiveException in case the shell command doesn't send output
     *            for a given time.
     * @throws IOException in case of I/O error on the connection.
     *
     * @see #executeShellCommand(String, IShellOutputReceiver, int)
     * @see DdmPreferences#getTimeOut()
     */
    @Override
    void executeShellCommand(String command, IShellOutputReceiver receiver) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        device.executeShellCommand(command, receiver)
    }

    /**
     * Runs the event log service and outputs the event log to the {@link LogReceiver}.
     * <p>This call is blocking until {@link LogReceiver#isCancelled()} returns true.
     * @param receiver the receiver to receive the event log entries.
     * @throws TimeoutException in case of timeout on the connection. This can only be thrown if the
     * timeout happens during setup. Once logs start being received, no timeout will occur as it's
     * not possible to detect a difference between no log and timeout.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws IOException in case of I/O error on the connection.
     */
    @Override
    void runEventLogService(LogReceiver receiver) throws TimeoutException, AdbCommandRejectedException, IOException {
        device.runEventLogService(receiver)
    }

    /**
     * Runs the log service for the given log and outputs the log to the {@link LogReceiver}.
     * <p>This call is blocking until {@link LogReceiver#isCancelled()} returns true.
     *
     * @param logname the logname of the log to read from.
     * @param receiver the receiver to receive the event log entries.
     * @throws TimeoutException in case of timeout on the connection. This can only be thrown if the
     *            timeout happens during setup. Once logs start being received, no timeout will
     *            occur as it's not possible to detect a difference between no log and timeout.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws IOException in case of I/O error on the connection.
     */
    @Override
    void runLogService(String logname, LogReceiver receiver) throws TimeoutException, AdbCommandRejectedException, IOException {
        device.runLogService(logname, receiver)
    }

    /**
     * Creates a port forwarding between a local and a remote port.
     *
     * @param localPort the local port to forward
     * @param remotePort the remote port.
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws IOException in case of I/O error on the connection.
     */
    @Override
    void createForward(int localPort, int remotePort) throws TimeoutException, AdbCommandRejectedException, IOException {
        device.createForward(localPort, remotePort)
    }

    /**
     * Creates a port forwarding between a local TCP port and a remote Unix Domain Socket.
     *
     * @param localPort the local port to forward
     * @param remoteSocketName name of the unix domain socket created on the device
     * @param namespace namespace in which the unix domain socket was created
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws IOException in case of I/O error on the connection.
     */
    @Override
    void createForward(int localPort, String remoteSocketName, IDevice.DeviceUnixSocketNamespace namespace) throws TimeoutException, AdbCommandRejectedException, IOException {
        device.createForward(localPort, remoteSocketName, namespace)
    }

    /**
     * Removes a port forwarding between a local and a remote port.
     *
     * @param localPort the local port to forward
     * @param remotePort the remote port.
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws IOException in case of I/O error on the connection.
     */
    @Override
    void removeForward(int localPort, int remotePort) throws TimeoutException, AdbCommandRejectedException, IOException {
        device.removeForward(localPort, remotePort)
    }

    /**
     * Removes an existing port forwarding between a local and a remote port.
     *
     * @param localPort the local port to forward
     * @param remoteSocketName the remote unix domain socket name.
     * @param namespace namespace in which the unix domain socket was created
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws IOException in case of I/O error on the connection.
     */
    @Override
    void removeForward(int localPort, String remoteSocketName, IDevice.DeviceUnixSocketNamespace namespace) throws TimeoutException, AdbCommandRejectedException, IOException {
        device.removeForward(localPort, remoteSocketName, namespace)
    }

    /**
     * Returns the name of the client by pid or <code>null</code> if pid is unknown
     * @param pid the pid of the client.
     */
    @Override
    String getClientName(int pid) {
        return device.getClientName(pid)
    }

    /**
     * Push a single file.
     * @param local the local filepath.
     * @param remote The remote filepath.
     *
     * @throws IOException in case of I/O error on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws TimeoutException in case of a timeout reading responses from the device.
     * @throws SyncException if file could not be pushed
     */
    @Override
    void pushFile(String local, String remote) throws IOException, AdbCommandRejectedException, TimeoutException, SyncException {
        device.pushFile(local, remote)
    }

    /**
     * Pulls a single file.
     *
     * @param remote the full path to the remote file
     * @param local The local destination.
     *
     * @throws IOException in case of an IO exception.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws TimeoutException in case of a timeout reading responses from the device.
     * @throws SyncException in case of a sync exception.
     */
    @Override
    void pullFile(String remote, String local) throws IOException, AdbCommandRejectedException, TimeoutException, SyncException {
        device.pullFile(remote, local)
    }

    /**
     * Installs an Android application on device. This is a helper method that combines the
     * syncPackageToDevice, installRemotePackage, and removePackage steps
     *
     * @param packageFilePath the absolute file system path to file on local host to install
     * @param reinstall set to <code>true</code> if re-install of app should be performed
     * @param extraArgs optional extra arguments to pass. See 'adb shell pm install --help' for
     *            available options.
     * @throws InstallException if the installation fails.
     */
    @Override
    void installPackage(String packageFilePath, boolean reinstall, String... extraArgs) throws InstallException {
        device.installPackage(packageFilePath, reinstall, extraArgs)
    }

    /**
     * Installs an Android application made of several APK files (one main and 0..n split packages)
     *
     * @param apks list of apks to install (1 main APK + 0..n split apks)
     * @param reinstall set to <code>true</code> if re-install of app should be performed
     * @param installOptions optional extra arguments to pass. See 'adb shell pm install --help' for
     *            available options.
     * @param timeout installation timeout
     * @param timeoutUnit {@link TimeUnit} corresponding to the timeout parameter
     * @throws InstallException if the installation fails.
     */
    @Override
    void installPackages(List<File> apks, boolean reinstall, List<String> installOptions, long timeout, TimeUnit timeoutUnit) throws InstallException {
        device.installPackages(apks, reinstall, installOptions, timeout, timeoutUnit)
    }

    /**
     * Pushes a file to device
     *
     * @param localFilePath the absolute path to file on local host
     * @return {@link String} destination path on device for file
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws IOException in case of I/O error on the connection.
     * @throws SyncException if an error happens during the push of the package on the device.
     */
    @Override
    String syncPackageToDevice(String localFilePath) throws TimeoutException, AdbCommandRejectedException, IOException, SyncException {
        return device.syncPackageToDevice(localFilePath)
    }

    /**
     * Installs the application package that was pushed to a temporary location on the device.
     *
     * @param remoteFilePath absolute file path to package file on device
     * @param reinstall set to <code>true</code> if re-install of app should be performed
     * @param extraArgs optional extra arguments to pass. See 'adb shell pm install --help' for
     *            available options.
     * @throws InstallException if the installation fails.
     */
    @Override
    void installRemotePackage(String remoteFilePath, boolean reinstall, String... extraArgs) throws InstallException {
        device.installRemotePackage(remoteFilePath, reinstall, extraArgs)
    }

    /**
     * Removes a file from device.
     *
     * @param remoteFilePath path on device of file to remove
     * @throws InstallException if the installation fails.
     */
    @Override
    void removeRemotePackage(String remoteFilePath) throws InstallException {
        device.removeRemotePackage(remoteFilePath)
    }

    /**
     * Uninstalls an package from the device.
     *
     * @param packageName the Android application package name to uninstall
     * @return a {@link String} with an error code, or <code>null</code> if success.
     * @throws InstallException if the uninstallation fails.
     */
    @Override
    String uninstallPackage(String packageName) throws InstallException {
        return device.uninstallPackage(packageName)
    }

    /**
     * Reboot the device.
     *
     * @param into the bootloader name to reboot into, or null to just reboot the device.
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command
     * @throws IOException
     */
    @Override
    void reboot(String into) throws TimeoutException, AdbCommandRejectedException, IOException {
        device.reboot(into)
    }

    /**
     * Ask the adb daemon to become root on the device.
     * This may silently fail, and can only succeed on developer builds.
     * See "adb root" for more information.
     *
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command.
     * @throws ShellCommandUnresponsiveException if the root status cannot be queried.
     * @throws IOException
     * @return true if the adb daemon is running as root, otherwise false.
     */
    @Override
    boolean root() throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return device.root()
    }

    /**
     * Queries the current root-status of the device.
     * See "adb root" for more information.
     *
     * @throws TimeoutException in case of timeout on the connection.
     * @throws AdbCommandRejectedException if adb rejects the command.
     * @return true if the adb daemon is running as root, otherwise false.
     */
    @Override
    boolean isRoot() throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return device.isRoot()
    }

    /** @deprecated */
    @Deprecated
    @Override
    Integer getBatteryLevel() throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return device.getBatteryLevel()
    }

    /** @deprecated */
    @Deprecated
    @Override
    Integer getBatteryLevel(long freshnessMs) throws TimeoutException, AdbCommandRejectedException, IOException, ShellCommandUnresponsiveException {
        return device.getBatteryLevel(freshnessMs)
    }

    /**
     * Return the device's battery level, from 0 to 100 percent.
     * <p>
     * The battery level may be cached. Only queries the device for its
     * battery level if 5 minutes have expired since the last successful query.
     *
     * @return a {@link Future} that can be used to query the battery level. The Future will return
     * a {@link ExecutionException} if battery level could not be retrieved.
     */
    @Override
    Future<Integer> getBattery() {
        return device.getBattery()
    }

    /**
     * Return the device's battery level, from 0 to 100 percent.
     * <p>
     * The battery level may be cached. Only queries the device for its
     * battery level if <code>freshnessTime</code> has expired since the last successful query.
     *
     * @param freshnessTime the desired recency of battery level
     * @param timeUnit the {@link TimeUnit} of freshnessTime
     * @return a {@link Future} that can be used to query the battery level. The Future will return
     * a {@link ExecutionException} if battery level could not be retrieved.
     */
    @Override
    Future<Integer> getBattery(long freshnessTime, TimeUnit timeUnit) {
        return device.getBattery(freshnessTime, timeUnit)
    }

    /**
     * Returns the ABIs supported by this device. The ABIs are sorted in preferred order, with the
     * first ABI being the most preferred.
     * @return the list of ABIs.
     */
    @Override
    List<String> getAbis() {
        return device.getAbis()
    }

    /**
     * Returns the density bucket of the device screen by reading the value for system property
     * {@link #PROP_DEVICE_DENSITY}.
     *
     * @return the density, or -1 if it cannot be determined.
     */
    @Override
    int getDensity() {
        return device.getDensity()
    }

    /**
     * Returns the user's language.
     *
     * @return the user's language, or null if it's unknown
     */
    @Override
    String getLanguage() {
        return device.getLanguage()
    }

    /**
     * Returns the user's region.
     *
     * @return the user's region, or null if it's unknown
     */
    @Override
    String getRegion() {
        return device.getRegion()
    }

    /**
     * Returns the API level of the device.
     *
     * @return the API level if it can be determined, {@link AndroidVersion#DEFAULT} otherwise.
     */
    @Override
    AndroidVersion getVersion() {
        return device.getVersion()
    }

    /**
     * Returns a (humanized) name for this device. Typically this is the AVD name for AVD's, and
     * a combination of the manufacturer name, model name &amp; serial number for devices.
     */
    @Override
    String getName() {
        String model = getProperty(IDevice.PROP_DEVICE_MODEL)
        String serialNumber = getSerialNumber()
        return "${model} (${serialNumber})"
    }

    /**
     * Executes a shell command on the device, and sends the result to a <var>receiver</var>.
     * <p><var>maxTimeToOutputResponse</var> is used as a maximum waiting time when expecting the
     * command output from the device.<br>
     * At any time, if the shell command does not output anything for a period longer than
     * <var>maxTimeToOutputResponse</var>, then the method will throw
     * {@link ShellCommandUnresponsiveException}.
     * <p>For commands like log output, a <var>maxTimeToOutputResponse</var> value of 0, meaning
     * that the method will never throw and will block until the receiver's
     * {@link IShellOutputReceiver#isCancelled()} returns <code>true</code>, should be
     * used.
     *
     * @param command the shell command to execute
     * @param receiver the {@link IShellOutputReceiver} that will receives the output of the shell
     *            command
     * @param maxTimeToOutputResponse the maximum amount of time during which the command is allowed
     *            to not output any response. A value of 0 means the method will wait forever
     *            (until the <var>receiver</var> cancels the execution) for command output and
     *            never throw.
     * @param maxTimeUnits Units for non-zero {@code maxTimeToOutputResponse} values.
     * @throws TimeoutException in case of timeout on the connection when sending the command.
     * @throws AdbCommandRejectedException if adb rejects the command.
     * @throws ShellCommandUnresponsiveException in case the shell command doesn't send any output
     *            for a period longer than <var>maxTimeToOutputResponse</var>.
     * @throws IOException in case of I/O error on the connection.
     *
     * @see DdmPreferences#getTimeOut()
     */
    @Override
    void executeShellCommand(String command, IShellOutputReceiver receiver, long maxTimeToOutputResponse, TimeUnit maxTimeUnits) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        device.executeShellCommand(command, receiver, maxTimeToOutputResponse, maxTimeUnits)
    }

    /**
     * Executes a shell command on the device, and sends the result to a <var>receiver</var>.
     *
     * <p><var>maxTimeToOutputResponse</var> is used as a maximum waiting time when expecting the
     * command output from the device.<br>
     * At any time, if the shell command does not output anything for a period longer than
     * <var>maxTimeToOutputResponse</var>, then the method will throw {@link
     * ShellCommandUnresponsiveException}.
     *
     * <p>For commands like log output, a <var>maxTimeToOutputResponse</var> value of 0, meaning
     * that the method will never throw and will block until the receiver's {@link
     * IShellOutputReceiver # isCancelled ( )} returns <code>true</code>, should be used.
     *
     * @param command the shell command to execute
     * @param receiver the {@link IShellOutputReceiver} that will receives the output of the shell
     *     command
     * @param maxTimeout the maximum timeout for the command to return. A value of 0 means no max
     *     timeout will be applied.
     * @param maxTimeToOutputResponse the maximum amount of time during which the command is allowed
     *     to not output any response. A value of 0 means the method will wait forever (until the
     *     <var>receiver</var> cancels the execution) for command output and never throw.
     * @param maxTimeUnits Units for non-zero {@code maxTimeout} and {@code maxTimeToOutputResponse}
     *     values.
     * @throws TimeoutException in case of timeout on the connection when sending the command.
     * @throws AdbCommandRejectedException if adb rejects the command.
     * @throws ShellCommandUnresponsiveException in case the shell command doesn't send any output
     *     for a period longer than <var>maxTimeToOutputResponse</var>.
     * @throws IOException in case of I/O error on the connection.
     * @see DdmPreferences#getTimeOut()
     */
    @Override
    void executeShellCommand(String command, IShellOutputReceiver receiver, long maxTimeout, long maxTimeToOutputResponse, TimeUnit maxTimeUnits) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        device.executeShellCommand(command, receiver, maxTimeout, maxTimeToOutputResponse, maxTimeUnits)
    }

    /**
     * Do a potential asynchronous query for a system property.
     *
     * @param name the name of the value to return.
     * @return a {@link java.util.concurrent.Future} which can be used to retrieve value of property. Future#get() can
     *         return null if property can not be retrieved.
     */
    @Override
    Future<String> getSystemProperty(String name) {
        return device.getSystemProperty(name)
    }
}