package grails.plugin.processtracking

import grails.test.mixin.TestFor
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import processtracking.ProcessTagLib
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll
import org.codehaus.groovy.grails.plugins.web.taglib.ApplicationTagLib

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
    }

    @Unroll("displayProperties: #displayProperties | expectedProperties: #expectedProperties")
    def "create a process table"(displayProperties, expectedProperties) {
        given: "A List of processes"
            def processInstanceList =  (1 ..10).collect{
                new Process(complete: new Date(),
                    name: "process name $it",
                    id:  it,
                    status: Process.ProcessStatus.QUEUED,
                    userId: 'username',
                    progress: 0L
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
            new Process(complete: new Date(),
                    name: "process name $it",
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

    def "Process rows match the process list"(){
        given: "A List of processes"
        def processInstanceList =  (1 ..3).collect{
            new Process(complete: new Date(),
                    name: "process name $it",
                    id:  it,
                    status: Process.ProcessStatus.QUEUED,
                    userId: 'username',
                    progress: 0L
            )
        }
        when: "I render a process table"
            String html = tagLib.processTable(processList: processInstanceList)
            def dom = jsoup(html)
            Element table = dom.getElementsByTag('table').first()
            def rows = table.select('tr')

        then: "The expected process valuse should appear in the table rows"
           processInstanceList.eachWithIndex {Process process, i ->
               Element row = rows.get(i+1)
               defaultDisplayProperties.eachWithIndex {entry, j->
                   //Need asserts inside closures
                   Element cell = row.child(j)
                   String expected = ""
                   //?: does not work for floats equal 0 - groovy truth...
                   if(null != process."${entry.key}"){
                       expected = process."${entry.key}"
                   }
                   assert cell.text() == expected
               }
           }

    }

    def tableCellGenerators() {
        expect:
            tagLib.processStatus(process:defaultProcess()) ==  '<td class="failed"></td>'
    }

    Process defaultProcess(){
        new Process(name: 'my process', status: Process.ProcessStatus.FAILED, id: 1L)
    }
    Document jsoup(html) {
        Jsoup.parse(html.toString())
    }
}