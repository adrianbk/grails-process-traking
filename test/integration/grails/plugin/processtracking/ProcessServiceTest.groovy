package grails.plugin.processtracking

import org.joda.time.DateTime
import org.junit.Before
import org.junit.Test

import java.util.concurrent.Callable
import java.util.concurrent.CompletionService
import java.util.concurrent.ExecutorCompletionService
import java.util.concurrent.Executors

import static grails.plugin.processtracking.Process.ProcessStatus.*
import static grails.plugin.processtracking.ProcessEvent.EventLevel.DEBUG
import static grails.plugin.processtracking.ProcessEvent.EventLevel.ERROR

class ProcessServiceTest extends GroovyTestCase {

    static transactional = false
    public static final int THREAD_POOL_SIZE = 80
    public static final Long SLEEP_DURATION = 100
    def sessionFactory
    //Using sleeps to simulate timing  of processes - may need to tweak TIMING_INACCURACY_THRESHOLD - threads never guaranteed
    public static final Long TIMING_INACCURACY_THRESHOLD = 40
    ProcessService processService
    Long pid

    @Before
    void clean(){
//        Process.executeUpdate("delete from ProcessEvent")
//        Process.executeUpdate("delete from Process")
//        Process.executeUpdate("delete from ProcessGroup")
        pid = processService.createProcess(new CreateProcessRequest())
    }

    @Test
    void shouldCreateAProcess(){
        Process process = processService.fetchProcess(pid)
        assert process.complete == null
        assert process.status == QUEUED
        assert process.progress == 0F
    }

    @Test
    void shouldHaveNoGroupWhenProcessCreatedWithout(){
        CreateProcessRequest cpr = new CreateProcessRequest()
        pid = processService.createProcess(cpr)
        Process process = processService.fetchProcess(pid)
        assert process.processGroup == null
    }

    @Test
    void shouldCorrectlyCreateANewProcessGroupWhenGroupNameIsSpecified()
    {
        CreateProcessRequest cpr = new CreateProcessRequest().withGroupName("Test Group")
        pid = processService.createProcess(cpr)
        Process process = processService.fetchProcess(pid)
        assert process.processGroup.total == 1L
        assert process.processGroup.name == cpr.groupName
        assert process.processGroup.averageDuration == 0L
    }

    @Test
    void firstProcessGroupShouldHaveAverageOfDurationOfFirstProcess()
    {
        CreateProcessRequest cpr = new CreateProcessRequest()
                .withProcessName('firstProcessGroupShouldHaveAverageOfDurationOfFirstProcess')
                .withGroupName("Test Group")
        pid = processService.createProcess(cpr)
        sleep()
        processService.completeProcess(pid)
        Process process = processService.fetchProcess(pid)

        assertEquals(true, isAverageDurationSomewhatCorrect(process.processGroup, SLEEP_DURATION, 1))
    }

    @Test
    /**
     * A concurrency test
     * Optimistic locking is turned off for Process to remedy StaleObjectStateExceptions.
     * "org.hibernate.StaleObjectStateException: Row was updated or deleted by another transaction"
     * Such exceptions can occur when multiple threads/persistence contexts add process events to the same process, the
     * reason is that gorm updates the version on the parent object after a child has been added.
     */
    void shouldAddEventsToProcessConcurrentlyWithoutGettingAnyStaleObjectExceptions(){
        CompletionService taskCompletionService = new ExecutorCompletionService( Executors.newFixedThreadPool(THREAD_POOL_SIZE));

        THREAD_POOL_SIZE.times{i ->
            def callable = {
                    processService.addProcessEvent(pid, "some message ${i}", DEBUG )
                } as Callable
            taskCompletionService.submit(callable)
        }

        //Block until all tasks have complete
        THREAD_POOL_SIZE.times{i ->
            def result = taskCompletionService.take()
            result.get()
        }
        Process process = processService.fetchProcess(pid)
        def events = ProcessEvent.findAllByProcess(process)
        assert events.size() == THREAD_POOL_SIZE + 1
    }

    @Test
    void shouldHaveProcessingStateWhenInitiated(){
        processService.initiateProcess(pid)
        Process process = processService.fetchProcess(pid)
        assert process.status == PROCESSING
    }

    @Test
    void shouldHaveInitiatedEventWhenInitiated(){
        DateTime now = DateTime.now()
        processService.initiateProcess(pid)
        Process process = processService.fetchProcess(pid)
        def processEvent = ProcessEvent.findByProcessAndTimestampGreaterThanEquals(process, now)
        assert processEvent != null
    }

    @Test
    void shouldHaveCorrectStateWhenCompletedSuccessfully(){
        processService.completeProcess(pid)
        Process process = processService.fetchProcess(pid)
        assert process.progress == 100F
        assert process.complete != null
        assert process.status == SUCCESS
    }

    @Test
    void shouldHaveCompletionEventWhenCompletedSuccessfully(){
        DateTime now = DateTime.now()
        processService.completeProcess(pid)
        Process process = processService.fetchProcess(pid)
        def processEvent = ProcessEvent.findByProcessAndTimestampGreaterThanEquals(process, now)
        assert processEvent != null
    }

    @Test
    void shouldHaveFailedStatusWhenCompletedWithErrorEvent()
    {
        processService.addProcessEvent(pid, "Something went wrong", ERROR)
        processService.completeProcess(pid)
        Process process = processService.fetchProcess(pid)
        assert process.status == FAILED
    }

    @Test
    void shouldIncrementGroupAverageWhenCompleted(){

        Process p1 = processService.fetchProcess(processService.createProcess(new CreateProcessRequest().withGroupName("Test 1")))

        sleep()
        processService.completeProcess(p1.id)

        CreateProcessRequest createProcessRequest = new CreateProcessRequest()
            .withProcessGroupId(p1.processGroup.id)

        Process p2 = processService.fetchProcess(processService.createProcess(createProcessRequest))
        sleep()
        processService.completeProcess(p2.id)

        p2 = processService.fetchProcess(p2.id)

        assert p2.processGroup.total == 2
        assertEquals(true, isAverageDurationSomewhatCorrect(p2.processGroup, SLEEP_DURATION, 2))
    }

    @Test
    void shouldIncrementProgress(){
        processService.incrementProcessProgress(pid, 2L)
        Process process = processService.fetchProcess(pid)
        assert process.progress == 2L
    }

    @Test
    void shouldHaveCorrectStateWhenProcessIsFailedWithMessage(){
        DateTime now = DateTime.now()
        processService.failProcess(pid, "I failed")
        Process process = processService.fetchProcess(pid)
        assert process.status == FAILED
        //TODO - remove the reset of millis as this was done since mysql datetime does not store millis
        assert process.complete.compareTo(now.withMillisOfSecond(0)) >= 0
    }

    @Test
    void shouldHaveCorrectStateWhenProcessIsFailedWithException(){
        DateTime now = DateTime.now()
        Throwable t = new RuntimeException("Problem!")
        processService.failProcess(pid, t)
        Process process = processService.fetchProcess(pid)
        assert process.status == FAILED
        //TODO - remove the reset of millis as this was done since mysql datetime does not store millis
        assert process.complete.compareTo(now.withMillisOfSecond(0)) >= 0
    }

    private isAverageDurationSomewhatCorrect(ProcessGroup processGroup, Long sleep, int numberOfSleeps ){
        processGroup.averageDuration >= ((sleep.multiply(numberOfSleeps)) / processGroup.total)
    }

    public void sleep() {
        Thread.sleep(SLEEP_DURATION)
    }
}