package com.example.metrics.micrometerregistryambari.implementation;

import io.micrometer.core.instrument.*;
import io.micrometer.core.instrument.distribution.HistogramSnapshot;
import io.micrometer.core.instrument.distribution.ValueAtPercentile;
import io.micrometer.core.instrument.step.StepMeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.StreamSupport.stream;

@Slf4j
public class AmbariMeterRegistry extends StepMeterRegistry {

    private static final ThreadFactory DEFAULT_THREAD_FACTORY = new CustomizableThreadFactory("ambari-metrics-publisher");
    private static final DecimalFormat PERCENTILE_FORMAT = new DecimalFormat("#.####");

    private final AmbariConfig config;
    private final AmbariMetricPublisher ambariMetricPublisher;


    public AmbariMeterRegistry(AmbariConfig config, AmbariMetricPublisher ambariMetricPublisher) {
        super(config, Clock.SYSTEM);
        this.config = config;
        this.ambariMetricPublisher = ambariMetricPublisher;
        start(DEFAULT_THREAD_FACTORY);
    }

    @Override
    public void start(ThreadFactory threadFactory) {
        if (config.enabled()) {
            log.info("Publishing metrics to ambari every " + config.step());
        }
        super.start(threadFactory);
    }

    @Override
    protected void publish() {
        List<Metric> metrics = getMeters().stream()
                .peek(m -> log.debug(m.getId().toString()))
                .flatMap(meter -> {
                    if (meter instanceof DistributionSummary) {
                        return getMetrics((DistributionSummary) meter);
                    } else if (meter instanceof FunctionTimer) {
                        return getMetrics((FunctionTimer) meter);
                    } else if (meter instanceof Timer) {
                        return getMetrics((Timer) meter);
                    } else {
                        return getMetrics(meter);
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

//        for (Metric metric : metrics) {
//            log.info(metric.toString());
//        }

        ambariMetricPublisher.publish(metrics);
    }


    @Override
    protected TimeUnit getBaseTimeUnit() {
        return TimeUnit.SECONDS;
    }

    private Stream<Metric> getMetrics(DistributionSummary meter) {
        return getMetrics(meter, meter.takeSnapshot());
    }

    private Stream<Metric> getMetrics(FunctionTimer meter) {
        return Stream.of(
                toMetric(withStatistic(meter, "count"), meter.count()),
                toMetric(withStatistic(meter, "mean"), meter.mean(getBaseTimeUnit())),
                toMetric(withStatistic(meter, "totalTime"), meter.totalTime(getBaseTimeUnit()))
        );
    }

    private Stream<Metric> getMetrics(Timer meter) {
        return getMetrics(meter, meter.takeSnapshot());
    }

    private Stream<Metric> getMetrics(Meter meter, HistogramSnapshot snapshot) {
        return Stream.concat(
                Stream.of(
                        toMetric(withStatistic(meter, "count"), snapshot.count()),
                        toMetric(withStatistic(meter, "max"), snapshot.max(getBaseTimeUnit())),
                        toMetric(withStatistic(meter, "mean"), snapshot.mean(getBaseTimeUnit())),
                        toMetric(withStatistic(meter, "totalTime"), snapshot.total(getBaseTimeUnit()))
                ),
                getMetrics(meter, snapshot.percentileValues())
        );
    }

    private Stream<Metric> getMetrics(Meter meter, ValueAtPercentile[] percentiles) {
        return Arrays.stream(percentiles)
                .map(percentile -> toMetric(withPercentile(meter, percentile), percentile.value(getBaseTimeUnit())));
    }

    private Stream<Metric> getMetrics(Meter meter) {
        return stream(meter.measure().spliterator(), false)
                .map(measurement -> toMetric(meter.getId().withTag(measurement.getStatistic()), measurement.getValue()));
    }

    private Metric toMetric(Meter.Id id, double value) {
        if (Double.isNaN(value)) {
            return null;
        }

        Map<String, String> tags = id.getTags().stream()
                .collect(Collectors.toMap(Tag::getKey, Tag::getValue));

        return new Metric(id.getName(), tags, this.clock.wallTime(), Type.GAUGE, id.getBaseUnit(), BigDecimal.valueOf(value));
    }

    private Meter.Id withPercentile(Meter meter, ValueAtPercentile percentile) {
        return withStatistic(meter, String.format("%spercentile", PERCENTILE_FORMAT.format(percentile.percentile() * 100)));
    }

    private Meter.Id withStatistic(Meter meter, String type) {
        return meter.getId().withTag(Tag.of("statistic", type));
    }
}
