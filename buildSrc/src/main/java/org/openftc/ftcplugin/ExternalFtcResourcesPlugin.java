package org.openftc.ftcplugin;

import com.android.build.gradle.BaseExtension;
import com.android.ddmlib.AndroidDebugBridge;

import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.jetbrains.annotations.NotNull;

// TODO: Run the tasks automatically
// TODO: Clean up openrc-tasks
// TODO: Move this to its own repo, replace with jar

public class ExternalFtcResourcesPlugin implements Plugin<Project> {

    // For consistency's sake, all directories should have trailing slashes.

    @Override
    public void apply(@NotNull Project project) {
        setupBridge(project);

        NamedDomainObjectContainer<FtcExternalResource> resourceContainer
                = project.container(FtcExternalResource.class);

        project.getExtensions().add("externalResources", resourceContainer);

        resourceContainer.all(resource -> {
            String resourceName = resource.getName();
            String capitalizedResourceName = resourceName.substring(0, 1).toUpperCase() + resourceName.substring(1);
            String taskName = "push" + capitalizedResourceName + "IfNecessary";
            CopyIfNecessary copyTask = project.getTasks().create(taskName, CopyIfNecessary.class);
            copyTask.setGroup("resources");

            project.afterEvaluate(project1 -> {
                copyTask.setGroup("resources");
                copyTask.getLocalSourcePath().set(resource.getFileToSend());
                copyTask.getRemoteDestinationPath().set(resource.getRemoteSubfolder());
            });
        });
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
