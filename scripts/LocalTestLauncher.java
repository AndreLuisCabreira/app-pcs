import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;

import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.Set;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClasspathRoots;

public class LocalTestLauncher {

    public static void main(String[] args) {
        LauncherDiscoveryRequest request = LauncherDiscoveryRequestBuilder.request()
                .selectors(selectClasspathRoots(Set.of(Path.of(args[0]))))
                .build();
        SummaryGeneratingListener summary = new SummaryGeneratingListener();
        Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(summary);
        launcher.execute(request);
        PrintWriter output = new PrintWriter(System.out, true);
        summary.getSummary().printTo(output);
        summary.getSummary().printFailuresTo(output);

        if (summary.getSummary().getTestsFailedCount() > 0) {
            System.exit(1);
        }
    }
}
