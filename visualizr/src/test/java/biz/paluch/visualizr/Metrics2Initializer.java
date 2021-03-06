package biz.paluch.visualizr;

import java.lang.management.ManagementFactory;
import java.util.concurrent.TimeUnit;

import javax.servlet.ServletContextEvent;

import biz.paluch.visualizr.metrics.SnapshotsHolder;
import biz.paluch.visualizr.metrics.VisualizrReporter;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.FileDescriptorRatioGauge;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadDeadlockDetector;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.servlet.InstrumentedFilterContextListener;
import com.codahale.metrics.servlets.MetricsServlet;

/**
 * @author <a href="mailto:mpaluch@paluch.biz">Mark Paluch</a>
 * @since 02.07.14 08:25
 */
public class Metrics2Initializer extends InstrumentedFilterContextListener {
    private MetricRegistry metricRegistry;

    @Override
    protected MetricRegistry getMetricRegistry() {

        if (metricRegistry == null) {
            metricRegistry = new MetricRegistry();

            metricRegistry.registerAll(new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()));
            metricRegistry.register("fd", new FileDescriptorRatioGauge(ManagementFactory.getOperatingSystemMXBean()));
            metricRegistry.registerAll(new GarbageCollectorMetricSet(ManagementFactory.getGarbageCollectorMXBeans()));
            metricRegistry.registerAll(new ThreadStatesGaugeSet(ManagementFactory.getThreadMXBean(),
                    new ThreadDeadlockDetector()));

            metricRegistry.registerAll(new MemoryUsageGaugeSet(ManagementFactory.getMemoryMXBean(), ManagementFactory
                    .getMemoryPoolMXBeans()));
        }
        return metricRegistry;
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        super.contextInitialized(sce);
        sce.getServletContext().setAttribute(MetricsServlet.METRICS_REGISTRY, getMetricRegistry());

        VisualizrReporter.forRegistry(getMetricRegistry()).build(SnapshotsHolder.snapshots).start(1, TimeUnit.SECONDS);

    }
}
