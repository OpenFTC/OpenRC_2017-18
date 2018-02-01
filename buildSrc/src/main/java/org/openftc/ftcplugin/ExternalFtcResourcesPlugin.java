package org.openftc.ftcplugin;

import com.android.build.gradle.BaseExtension;
import com.android.ddmlib.AndroidDebugBridge;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

public class ExternalFtcResourcesPlugin implements Plugin<Project> {
    @Override
    public void apply(@NotNull Project project) {
        setupBridge(project);

        // For consistency's sake, all directories should have trailing slashes.

        ExternalFtcResourcesExtension externalFtcResourcesExtension;
        project.getExtensions().create("externalResources", ExternalFtcResourcesExtension.class);
        project.getTasks().create("CINtest", CopyIfNecessaryTask.class, copyIfNecessaryTask -> {
//                copyIfNecessaryTask.
        });

        // TODO: Test when these aren't set
//        copyIfNecessaryTask.setProperty("localSourcePath", "../openrc.txt");
//        copyIfNecessaryTask.remoteDestinationPath = 'libs/'


//        project.getExtensions().getByName("android")
//        project.plugins.findPlugin('android').getSdkParser()
    }

    private static void setupBridge(Project project) {
        AndroidDebugBridge.initIfNeeded(false);
        String adbPath = ((BaseExtension)project.getExtensions().getByName("android")).getAdbExecutable().getAbsolutePath();
        bridge = AndroidDebugBridge.createBridge(adbPath, false);
    }

    public static AndroidDebugBridge getBridge() {
        return bridge;
    }

    private static AndroidDebugBridge bridge;
}
