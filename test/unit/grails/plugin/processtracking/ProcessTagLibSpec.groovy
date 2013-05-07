package grails.plugin.processtracking

import grails.test.mixin.TestFor
import spock.lang.Specification
import processtracking.ProcessTagLib
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import spock.lang.Unroll
import spock.lang.Shared
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import javax.lang.model.util.Elements

@TestFor(ProcessTagLib)
class ProcessTagLibSpec extends Specification {
    @Shared
    def defaultDisplayProperties = [
            name: 'Name',
            initiated: 'Initiated',
            status: 'Status',
            userId: 'User Id',
            progress: 'Progress',
            complete: 'Complete',
            processGroup: 'Process Group'
    ]

    def setup() {

        grailsApplication.config.processTracking.tableDisplayProperties = defaultDisplayProperties


    //[complete, dateCreated, initiated, lastUpdated, name, processEvents, processGroup, progress, relatedDomainId, status, userId]
    //[Complete, Date Created, Initiated, Last Updated, Name, Process Events, Process Group, Progress, Related Domain Id, Status, User Id]
    }

    def cleanup() {
    }

    @Unroll("displayProperties: #displayProperties | expectedProperties: #expectedProperties")
    def "create a process table"(displayProperties, expectedProperties) {
        given: "A List of processes"
            def processInstanceList =  (1 ..10).collect{
                new Process(complete: false,
                    name: "process name $it",
                    id:  it
                )
            }

        when: "I render a process table"
            def html = tagLib.processTable(processList: processInstanceList, tableDisplayProperties: displayProperties)
            def dom = jsoup(html)

        then: "The the rendered table should contain all the expected attributes"
            def table = dom.select('table')
            table.attr('class') == 'process-table'

            def header = table.select('tr').first()
            //Header plus n processes
            assert table.select('tr').size() == (processInstanceList.size() + 1)

            //Table Headers
            expectedProperties.eachWithIndex {prop, i ->
                assert header.child(i).text() == prop
            }

        where:
            displayProperties                                   | expectedProperties
            null                                                | defaultDisplayProperties.values()
            ['name': 'Name', 'progress': 'Progress']            | ['Name', 'Progress']
            ['sadasd': 'SomeWrongProp', 'progress': 'Progress'] | ['Progress']

    }

    @Unroll
    def "process table rows are classed with odd and even"(){
        given: "A List of processes"
        def processInstanceList =  (1 ..3).collect{
            new Process(complete: false,
                    name: 'process name $it',
                    id:  it
            )
        }

        when: "I render a process table"
            def html = tagLib.processTable(processList: processInstanceList)
            def dom = jsoup(html)
            Element table = dom.getElementsByTag('table').first()
            def rows = table.select('tr')
        then:
            rows.get(1).className() == 'even'
            rows.get(2).className() == 'odd'

    }

    Document jsoup(html) {
        Jsoup.parse(html.toString())
    }
}