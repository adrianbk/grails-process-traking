package grails.plugin.processtracking

import grails.test.mixin.TestFor
import spock.lang.Specification
import org.apache.commons.lang.RandomStringUtils
import grails.plugin.spock.UnitSpec

/**
 * See the API for {@link grails.test.mixin.domain.DomainClassUnitTestMixin} for usage instructions
 */
@TestFor(ProcessGroup)
class ProcessGroupSpec extends UnitSpec {
    def "domain constraints"() {
        setup:
        mockForConstraintsTests(ProcessGroup)

        when:
        def processGroup = new ProcessGroup(name: name, total: total, averageDuration: averageDuration)
        processGroup.validate()

        then:
        UnitTestHelper.isValidDomain(processGroup, valid) == valid

        where:
        name      | total | averageDuration | valid
        "Group 1" | 1L    | 1L              | true
        null      | 1L    | 1L              | false
        "Group 1" | null  | 1L              | false
        "Group 1" | 2L    | null            | false
    }
}