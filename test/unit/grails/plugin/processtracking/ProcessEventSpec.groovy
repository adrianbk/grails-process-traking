package grails.plugin.processtracking

import grails.plugin.spock.UnitSpec
import org.apache.commons.lang.RandomStringUtils

class ProcessEventSpec extends UnitSpec {
    def "domain constraints"() {
        setup:
        mockForConstraintsTests(ProcessEvent)

        when:
        def event = new ProcessEvent(process: process, message: message, eventLevel: eventLevel, timestamp: timestamp)
        event.validate()

        then:
        UnitTestHelper.isValidDomain(event, valid) == valid

        where:
        process       | message                        | eventLevel                    | timestamp  | valid
        new Process() | "Event message"                | ProcessEvent.EventLevel.DEBUG | new Date() | true
        new Process() | ""                             | ProcessEvent.EventLevel.DEBUG | new Date() | false
        new Process() | RandomStringUtils.random(3002) | ProcessEvent.EventLevel.DEBUG | new Date() | true
        new Process() | "Event message"                | ProcessEvent.EventLevel.DEBUG | null       | false
    }


}
