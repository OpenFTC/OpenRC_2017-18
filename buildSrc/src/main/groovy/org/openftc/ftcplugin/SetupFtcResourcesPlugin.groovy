package org.openftc.ftcplugin

import com.android.ddmlib.AndroidDebugBridge

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.Task
import org.gradle.api.logging.LogLevel

public class SetupFtcResourcesPlugin implements Plugin<Project> {
    static AndroidDebugBridge bridge

    @Override
    public void apply(Project project) {
        setupBridge(project)

        // For consistency's sake, all directories should have trailing slashes.

        ExternalResourcesExtension externalResourcesExtension
        project.extensions.create("externalResources", ExternalResourcesExtension)
        Task copyIfNecessaryTask = project.task("CINtest", type: CopyIfNecessaryTask)

        // TODO: Test when these aren't set
        copyIfNecessaryTask.localSourcePath = '../openrc.txt'
//        copyIfNecessaryTask.remoteDestinationPath = 'libs/'


//        project.getExtensions().getByName("android")
//        project.plugins.findPlugin('android').getSdkParser()
    }

    // TODO: Verify that this gets called before any tasks are created
    private static void setupBridge(Project project) {
        AndroidDebugBridge.initIfNeeded(false)
        String adbPath = project.android.getAdbExecutable().absolutePath
        bridge = AndroidDebugBridge.createBridge(adbPath, false)
    }
}
