package grails.plugin.processtracking

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
    ProcessService processService
    Long pid

    @Before
    void clean(){
        Process.executeUpdate("delete from ProcessEvent ")
        Process.executeUpdate("delete from Process")
        pid = processService.createProcess("My Process", null)
    }


    @Test
    void shouldCreateAProcess(){
        Process process = Process.findById(pid)
        assert process.complete == null
        assert process.status == QUEUED
        assert process.progress == 0F
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
        Process process = Process.findById(pid)
        def events = ProcessEvent.findAllByProcess(process)
        assert events.size() == THREAD_POOL_SIZE + 1
    }

    @Test
    void shouldHaveProcessingStateWhenInitiated(){
        processService.initiateProcess(pid)
        Process process = Process.findById(pid)
        assert process.status == PROCESSING
    }

    @Test
    void shouldHaveInitiatedEventWhenInitiated(){
        Date now = new Date()
        processService.initiateProcess(pid)
        Process process = Process.findById(pid)
        def processEvent = ProcessEvent.findByProcessAndTimestampGreaterThanEquals(process, now)
        assert processEvent != null
    }

    @Test
    void shouldHaveCorrectStateWhenCompletedSuccessfully(){
        processService.completeProcess(pid)
        Process process = Process.findById(pid)
        assert process.progress == 100F
        assert process.complete != null
        assert process.status == SUCCESS
    }

    @Test
    void shouldCompletionEventWhenCompletedSuccessfully(){
        Date now = new Date()
        processService.completeProcess(pid)
        Process process = Process.findById(pid)
        def processEvent = ProcessEvent.findByProcessAndTimestampGreaterThanEquals(process, now)
        assert processEvent != null
    }

    @Test
    void shouldHaveFailedStatusWhenCompletedWithErrorEvent()
    {
        processService.addProcessEvent(pid, "Something went wrong", ERROR)
        processService.completeProcess(pid)
        Process process = Process.findById(pid)
        assert process.status == FAILED
    }

    @Test
    void shouldIncrementProgress(){
        processService.incrementProcessProgress(pid, 2L)
        Process process = Process.findById(pid)
        assert process.progress == 2L
    }

    @Test
    void shouldHaveCorrectStateWhenProcessIsFailedWithMessage(){
        Date now = new Date()
        processService.failProcess(pid, "I failed")
        Process process = Process.findById(pid)
        assert process.status == FAILED
        assert process.complete.compareTo(now) >= 0
    }

    @Test
    void shouldHaveCorrectStateWhenProcessIsFailedWithException(){
        Date now = new Date()
        Throwable t = new RuntimeException("Problem!")
        processService.failProcess(pid, t)
        Process process = Process.findById(pid)
        assert process.status == FAILED
        assert process.complete.compareTo(now) >= 0
    }

}