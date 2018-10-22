package org.palladiosimulator.simulizar.power.evaluationscope;

import static java.util.stream.Collectors.toMap;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import javax.measure.Measure;
import javax.measure.quantity.Duration;

import org.apache.commons.collections15.IteratorUtils;
import org.eclipse.emf.ecore.EClass;
import org.palladiosimulator.commons.designpatterns.AbstractObservable;
import org.palladiosimulator.edp2.datastream.IDataStream;
import org.palladiosimulator.experimentanalysis.ISlidingWindowMoveOnStrategy;
import org.palladiosimulator.experimentanalysis.KeepLastElementPriorToLowerBoundStrategy;
import org.palladiosimulator.experimentanalysis.SlidingWindow;
import org.palladiosimulator.experimentanalysis.SlidingWindowRecorder;
import org.palladiosimulator.experimentanalysis.windowaggregators.SlidingWindowUtilizationAggregator;
import org.palladiosimulator.measurementframework.MeasuringValue;
import org.palladiosimulator.metricspec.MetricDescription;
import org.palladiosimulator.metricspec.constants.MetricDescriptionConstants;
import org.palladiosimulator.pcm.resourceenvironment.ProcessingResourceSpecification;
import org.palladiosimulator.pcmmeasuringpoint.ActiveResourceMeasuringPoint;
import org.palladiosimulator.pcmmeasuringpoint.PcmmeasuringpointFactory;
import org.palladiosimulator.pcmmeasuringpoint.PcmmeasuringpointPackage;
import org.palladiosimulator.probeframework.calculator.Calculator;
import org.palladiosimulator.probeframework.calculator.RegisterCalculatorFactoryDecorator;
import org.palladiosimulator.recorderframework.AbstractRecorder;
import org.palladiosimulator.recorderframework.config.IRecorderConfiguration;
import org.palladiosimulator.simulizar.power.calculators.SimulationTimePowerCalculator;
import org.palladiosimulator.simulizar.slidingwindow.impl.SimulizarSlidingWindow;

import de.fzi.power.infrastructure.PowerProvidingEntity;
import de.fzi.power.interpreter.AbstractEvaluationScope;
import de.fzi.power.interpreter.InterpreterUtils;
import de.uka.ipd.sdq.simucomframework.model.SimuComModel;

/**
 * This class is an implementation of an evaluation scope to gather utilization measurements
 * required for power consumption measurements at runtime, i.e., during runs of SimuLizar.<br>
 * In the current implementation, the utilization is evaluated for all
 * {@link ProcessingResourceSpecification}s that are supplied by a given
 * {@link PowerProvidingEntity}. That is, the utilization of a
 * {@code ProcessingResourceSpecification} is evaluated if it corresponds to
 * {@code PowerConsumingResource} subsumed by the given {@code PowerProvidingEntity}. In order to
 * keep track of new measurements (i.e., to obtain a new power consumption measurement) client have
 * to attach themselves by calling<br>
 * {@link #addListener(ISimulationEvaluationScopeListener)}.
 * 
 * @author Florian Rosenthal
 * @see SimulationTimePowerCalculator
 * @see ISimulationEvaluationScopeListener
 */
public class SimulationTimeEvaluationScope extends AbstractEvaluationScope {

    private final Collection<ProcessingResourceSpecification> processingResourceSpecs;
    private final SimuComModel simModel;
    private final UtilizationMeasurementsCollector collector;
    private final RegisterCalculatorFactoryDecorator calculatorFactory;

    private static final MetricDescription UTILIZATION_METRIC = MetricDescriptionConstants.UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE;
    private static final MetricDescription RESOURCE_STATE_METRIC = MetricDescriptionConstants.STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE;
    private static final EClass ACTIVE_RESOURCE_MP_ECLASS = PcmmeasuringpointPackage.Literals.ACTIVE_RESOURCE_MEASURING_POINT;

    /**
     * Gets a {@link SimulationTimeEvaluationScope} instance initialized with the given parameters.
     * 
     * @param entityUnderMeasurement
     *            The {@link PowerProvidingEntity} that shall be evaluated.
     * @param model
     *            A reference indicating the {@link SimuComModel} that is used for the current
     *            simulation run.
     * @param windowLength
     *            The length of the underlying sliding window, given in any arbitrary
     *            {@link Duration}.
     * @param windowIncrement
     *            This {@link Measure} indicates the increment by what the underlying sliding window
     *            is moved on, given in any arbitrary {@link Duration}.
     * @return A valid {@link SimulationTimeEvaluationScope} instance with the given properties.
     * @throws NullPointerException
     *             If {@code entityUnderMeasurement} or {@code model} are {@code null}.
     * @throws IllegalArgumentException
     *             In case {@code windowLength} or {@code windowIncrement} are {@code null} or
     *             denote a negative duration.
     * @throws IllegalStateException
     *             This exception is thrown, if any of the {@link ProcessingResourceSpecification}s
     *             subsumed by the given {@code entityUnderMeasurement} is not associated with
     *             {@link MetricDescriptionConstants#STATE_OF_ACTIVE_RESOURCE_METRIC_TUPLE}
     *             measurements.
     */
    public static SimulationTimeEvaluationScope createScope(final PowerProvidingEntity entityUnderMeasurement,
            final SimuComModel model, final Measure<Double, Duration> windowLength,
            final Measure<Double, Duration> windowIncrement) {

        SimulationTimeEvaluationScope scope = new SimulationTimeEvaluationScope(entityUnderMeasurement, model);
        scope.initialize(windowLength, windowIncrement);

        return scope;
    }

    /**
     * Initializes a new instance of the {@link SimulationTimeEvaluationScope} with the given
     * properties.
     * 
     * @param entityUnderMeasurement
     *            The {@link PowerProvidingEntity} that shall be evaluated.
     * @param model
     *            A reference indicating the {@link SimuComModel} that is used for the current
     *            simulation run.
     * @throws NullPointerException
     *             If either of the arguments is {@code null}, an {@link NullPointerException} is
     *             thrown.
     * @see #createScope(PowerProvidingEntity, SimuComModel, Measure, Measure)
     * @see #initialize(Measure, Measure)
     */
    protected SimulationTimeEvaluationScope(final PowerProvidingEntity entityUnderMeasurement,
            final SimuComModel model) {
        this.simModel = Objects.requireNonNull(model, "Given SimuComModel must not be null.");
        this.processingResourceSpecs = InterpreterUtils.getProcessingResourceSpecsFromInfrastructureElement(
                Objects.requireNonNull(entityUnderMeasurement, "Given PowerProvidingEntity must not be null."));
        this.collector = new UtilizationMeasurementsCollector(this.processingResourceSpecs.size());

        this.calculatorFactory = RegisterCalculatorFactoryDecorator.class
                .cast(this.simModel.getProbeFrameworkContext().getCalculatorFactory());

        this.processingResourceSpecs
                .forEach(spec -> this.resourceMeasurements.put(spec, Collections.singleton(new SingletonDataStream())));
    }

    /**
     * Initializes the current instance by instantiating the underlying {@link SlidingWindow} and
     * respective aggregator and recorder.
     * 
     * @param windowLength
     *            The length of the underlying sliding window, given in any arbitrary
     *            {@link Duration}.
     * @param windowIncrement
     *            This {@link Measure} indicates the increment by what the underlying sliding window
     *            is moved on, given in any arbitrary {@link Duration}.
     * @see #createScope(PowerProvidingEntity, SimuComModel, Measure, Measure)
     * @see #SimulationTimeEvaluationScope(PowerProvidingEntity, SimuComModel)
     */
    private void initialize(final Measure<Double, Duration> windowLength,
            final Measure<Double, Duration> windowIncrement) {
        ISlidingWindowMoveOnStrategy moveOnStrategy = new KeepLastElementPriorToLowerBoundStrategy();
        PcmmeasuringpointFactory pcmMeasuringpointFactory = PcmmeasuringpointFactory.eINSTANCE;

        Map<String, Calculator> availableOverallUtilizationCalculators = getAvailableOverallUtilizationCalculators();

        for (ProcessingResourceSpecification proc : this.processingResourceSpecs) {
            Optional<Calculator> resourceStateCalculator = null;
            MetricDescription resourceStateMetric = null;

            // in case of a multi-core resource, always use the "overall" state which is
            // automatically measured
            // confer ResourceEnvironmentSyncer in Simulizar plugin
            if (proc.getNumberOfReplicas() > 1) {
                resourceStateMetric = UTILIZATION_METRIC;
                resourceStateCalculator = findOverallUtilizationCalculatorForProcessingResource(proc,
                        availableOverallUtilizationCalculators);
            } else {
                ActiveResourceMeasuringPoint mp = pcmMeasuringpointFactory.createActiveResourceMeasuringPoint();
                mp.setActiveResource(proc);
                mp.setReplicaID(0);

                resourceStateCalculator = Optional.ofNullable(this.calculatorFactory
                        .getCalculatorByMeasuringPointAndMetricDescription(mp, RESOURCE_STATE_METRIC));
                resourceStateMetric = RESOURCE_STATE_METRIC;
            }

            Calculator baseCalculator = resourceStateCalculator.orElseThrow(() ->

            new IllegalStateException(
                    "Simulation time evaluation scope (sliding window based) cannot" + " be initialized.\n"
                            + ((proc.getNumberOfReplicas() == 1)
                                    ? "No 'state of active resource calculator' available for resource: " + proc + "\n"
                                    : "No 'overall utilization of active resource' calculator available for multi-core"
                                            + " resource: " + proc + "\n")
                            + "Ensure that initializeModelSyncers() in SimulizarRuntimeState is called prior "
                            + "to initializeInterpreterListeners()!"));

            SlidingWindow slidingWindow = new SimulizarSlidingWindow(windowLength, windowIncrement, resourceStateMetric,
                    moveOnStrategy, this.simModel);
            SlidingWindowRecorder windowRecorder = new SlidingWindowRecorder(slidingWindow,
                    new SlidingWindowUtilizationAggregator(resourceStateMetric, new ScopeRecorder(proc)));

            baseCalculator.addObserver(windowRecorder);
        }
    }

    /**
     * Retrieve the all calculators that are compatible with the
     * {@link MetricDescriptionConstants#UTILIZATION_OF_ACTIVE_RESOURCE_TUPLE} metric and associated
     * with an {@link ProcessingResourceSpecification} (i.e., an active resource). These are the
     * calculators which compute the 'overall utilization' of multi-core resources.
     * 
     * @return A {@link Map} with the ids of the respective resources mapped onto the found
     *         calculators
     */
    private Map<String, Calculator> getAvailableOverallUtilizationCalculators() {
        return this.calculatorFactory.getRegisteredCalculators().stream()
                .filter(calc -> calc.isCompatibleWith(UTILIZATION_METRIC)
                        && ACTIVE_RESOURCE_MP_ECLASS.isInstance(calc.getMeasuringPoint()))
                .collect(toMap(
                        calc -> ((ActiveResourceMeasuringPoint) calc.getMeasuringPoint()).getActiveResource().getId(),
                        Function.identity()));
    }

    private static Optional<Calculator> findOverallUtilizationCalculatorForProcessingResource(
            final ProcessingResourceSpecification proc,
            final Map<String, Calculator> availableOverallUtilizationCalculators) {
        return Optional.ofNullable(availableOverallUtilizationCalculators.get(proc.getId()));
    }

    @Override
    public void reset() {
        super.reset();
        this.iterator = iterator();
    }

    /**
     * Adds the given listener to collection of scope observers.
     * 
     * @param listener
     *            The {@link ISimulationEvaluationScopeListener} to observe this scope.
     * @throws IllegalArgumentException
     *             In case the given listener is {@code null} or already attached.
     * @see #removeListener(ISimulationEvaluationScopeListener)
     * @see #removeAllListeners()
     */
    public void addListener(final ISimulationEvaluationScopeListener listener) {
        this.collector.addObserver(listener);
    }

    /**
     * Detaches the given listener from the scope. Prior to that, the listener's
     * {@link ISimulationEvaluationScopeListener#preUnregister()} callback implementation is
     * invoked.
     * 
     * @param listener
     *            The {@link ISimulationEvaluationScopeListener} to detach.
     * @throws IllegalArgumentException
     *             In case the given listener has not been attached or is {@code null}.
     * @see #addListener(ISimulationEvaluationScopeListener)
     * @see #removeAllListeners()
     */
    public void removeListener(final ISimulationEvaluationScopeListener listener) {
        listener.preUnregister();
        this.collector.removeObserver(listener);
    }

    /**
     * Removes all currently attached listeners, i.e., call is equivalent to invocation of
     * {@link #removeListener(ISimulationEvaluationScopeListener)} once per attached listener.
     */
    public void removeAllListeners() {
        this.collector.getObservers().forEach(this::removeListener);
    }

    /**
     * This implementation does nothing.
     */
    @Override
    public void setResourceMetricsToEvaluate(
            final Map<ProcessingResourceSpecification, Set<MetricDescription>> metricsMap) {
        // implementation is not required here
    }

    /**
     * Implementation of the {@link IDataStream} interface that is internally used to manage the
     * collected output data per resource. This stream is exceptional in that it does contain at
     * most one element at a time.
     * 
     * @author Florian Rosenthal
     *
     */
    private static final class SingletonDataStream implements IDataStream<MeasuringValue> {
        private Optional<MeasuringValue> innerElement;
        private boolean isClosed;

        private static final ListIterator<MeasuringValue> EMPTY_ITERATOR = Collections.emptyListIterator();

        /**
         * Initializes a new instance of the class.
         */
        private SingletonDataStream() {
            this.isClosed = false;
            this.innerElement = Optional.empty();
        }

        @Override
        public Iterator<MeasuringValue> iterator() {
            throwExceptionIfClosed();
            return this.innerElement.map(IteratorUtils::singletonListIterator).orElse(EMPTY_ITERATOR);
        }

        @Override
        public MetricDescription getMetricDesciption() {
            return UTILIZATION_METRIC;
        }

        @Override
        public boolean isCompatibleWith(final MetricDescription other) {
            return getMetricDesciption().equals(other);
        }

        @Override
        public void close() {
            throwExceptionIfClosed();
            this.isClosed = true;
            this.innerElement = null;
        }

        @Override
        public int size() {
            throwExceptionIfClosed();
            return this.innerElement.map(el -> 1).orElse(0);
        }

        /**
         * Exchanges the currently contained sole element by the given {@link MeasuringValue}.
         * 
         * @param m
         *            The {@link MeasuringValue} to be stored in the stream.
         */
        public void exchangeElement(final MeasuringValue m) {
            assert m != null;

            throwExceptionIfClosed();
            this.innerElement = Optional.of(m);
        }

        /**
         * Convenience method to check whether instance is closed (i.e., close() was called) and
         * throw exception if so.
         */
        private void throwExceptionIfClosed() {
            if (this.isClosed) {
                throw new IllegalStateException("This stream is already closed.");
            }
        }
    }

    private class UtilizationMeasurementsCollector extends AbstractObservable<ISimulationEvaluationScopeListener> {

        private final Map<ProcessingResourceSpecification, MeasuringValue> collectedMeasurements;
        private final int measurementsToCollect;

        public UtilizationMeasurementsCollector(final int measurementsToCollect) {
            assert measurementsToCollect > 0;
            this.collectedMeasurements = new HashMap<ProcessingResourceSpecification, MeasuringValue>(
                    measurementsToCollect);
            this.measurementsToCollect = measurementsToCollect;
        }

        private void addUtilizationMeasurementForProcessingResource(final ProcessingResourceSpecification spec,
                final MeasuringValue utilMeasurement) {
            assert spec != null && utilMeasurement != null;

            if (this.collectedMeasurements.put(spec, utilMeasurement) == null
                    || !SimulationTimeEvaluationScope.this.simModel.getSimulationControl().isRunning()) {
                if (this.collectedMeasurements.size() == this.measurementsToCollect) {
                    // one "round" is complete: windows of all specs have
                    // produced their utilization measurement
                    // so forward data to listeners (e.g., power calculators,
                    // consumption contexts), then clear
                    for (ProcessingResourceSpecification proc : SimulationTimeEvaluationScope.this.processingResourceSpecs) {
                        Set<IDataStream<MeasuringValue>> dataset = SimulationTimeEvaluationScope.this.resourceMeasurements
                                .get(proc);
                        assert dataset.size() == 1;
                        // this cast is safe as we insert only
                        // SingletonDataStream instances (cf. ctor)
                        SingletonDataStream procMeasurements = (SingletonDataStream) dataset.iterator().next();
                        procMeasurements.exchangeElement(this.collectedMeasurements.get(proc));
                    }
                    resetScope();
                    informScopeListeners();
                    // start anew
                    this.collectedMeasurements.clear();
                }
            } else {
                throw new AssertionError("This should not happen");
            }
        }

        private void resetScope() {
            SimulationTimeEvaluationScope.this.reset();
        }

        private void informScopeListeners() {
            this.getEventDispatcher().newElementAvailable();
        }
    }

    private class ScopeRecorder extends AbstractRecorder {

        private final ProcessingResourceSpecification spec;

        public ScopeRecorder(final ProcessingResourceSpecification spec) {
            this.spec = spec;
        }

        @Override
        public void initialize(final IRecorderConfiguration recorderConfiguration) {
            // implementation is not required
        }

        @Override
        public void writeData(final MeasuringValue measurement) {
            // we receive a new utilization measurement now
            if (Objects.requireNonNull(measurement, "Somehow 'null' measurement was passed to recorder.")
                    .isCompatibleWith(UTILIZATION_METRIC)) {
                SimulationTimeEvaluationScope.this.collector.addUtilizationMeasurementForProcessingResource(spec,
                        measurement);
            }
        }

        @Override
        public void flush() {
            // implementation is not required
        }
    }
}
