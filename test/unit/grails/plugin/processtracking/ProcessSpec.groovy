package grails.plugin.processtracking

import grails.plugin.spock.UnitSpec
import static grails.plugin.processtracking.Process.ProcessStatus.*
import org.joda.time.DateTime

class ProcessSpec extends UnitSpec {

    def "domain constraints"() {
        setup:
            mockForConstraintsTests(Process)

        when:
            def process = new Process(name: name,
                    relatedDomainId: relatedDomainId,
                    progress: progress,
                    complete: complete,
                    status: status,
                    userId: userId,
                    initiated: initiated)
            process.validate()

        then:
            UnitTestHelper.isValidDomain(process, valid) == valid

        where:
            name   | relatedDomainId | progress | complete | status | userId | initiated  | valid
            "test" | null            | 100      | null     | FAILED | null   | DateTime.now() | true
            ""     | null            | 100      | null     | FAILED | null   | DateTime.now() | false
            null   | null            | 100      | null     | FAILED | null   | DateTime.now() | false
            "test" | null            | 101      | null     | FAILED | null   | DateTime.now() | false
            "test" | null            | -1       | null     | FAILED | null   | DateTime.now() | false
            "test" | null            | 101      | null     | FAILED | null   | DateTime.now() | false
            "test" | null            | 100      | null     | FAILED | null   | null       | false
    }
}


