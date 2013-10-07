package grails.plugin.processtracking

import grails.converters.deep.JSON
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import org.joda.time.DateTime
import org.junit.Ignore
import org.junit.Test

import static grails.plugin.processtracking.Process.ProcessStatus.QUEUED
import static grails.plugin.processtracking.ProcessEvent.EventLevel.WARN

@TestFor(ProcessController)
@Mock([Process, ProcessEvent])
class ProcessControllerTests {

    @Test
    public void shouldRedirectToListOnCallingIndex(){
        controller.index()
        assert response.redirectedUrl.endsWith('list')
    }

    @Test
    void shouldDisplayMaximumNumberOfProcesses(){
        21.times {i->
            new Process(name: "Test${i}", progress: 0F, status: QUEUED, initiated: new DateTime().now()).save(failOnError: true)
        }

        controller.params.max = 20;
        controller.list()

        assert model.processList.size() == 20
        assert model.processTotal == 21
    }

    @Ignore //TODO FIX local date issues
    void shouldReturnCorrectlyFormattedJsonForASingleProcessEvent() {
        def process1 = new Process(name: "Test Process", progress: 0F, status: QUEUED, initiated: new DateTime().now()).save(failOnError: true)
        def process2 = new Process(name: "Test Process2", progress: 0F, status: QUEUED, initiated: new DateTime().now()).save(failOnError: true)
        def event1 = new ProcessEvent(process: process1, message: "Some evnet", eventLevel: WARN, timestamp: new DateTime().now()).save(failOnError: true)
        def event2 = new ProcessEvent(process: process2, message: "Some evnet", eventLevel: WARN, timestamp: new DateTime().now()).save(failOnError: true)
        controller.params.processIds = [process1.id, process2.id]

        controller.jsonList()
        String generatedJson = response.contentAsString

        def result = JSON.parse(generatedJson)

        assert result['processes'].size() == 2
        result['processes'].eachWithIndex {jsonNode, i ->
            assert jsonNode['process']['processEvents'].size() == 1
        }
    }
}