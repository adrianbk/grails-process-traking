package grails.plugin.processtracking

import grails.plugin.processtracking.Process
import groovy.xml.MarkupBuilder
import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass

class ProcessTagLib {
    final static namespace = "processTracking"
    static final String TEMPLATE_DIR = '/processtracking/templates/'
    static final processDomainAttributes = new DefaultGrailsDomainClass(Process.class).persistantProperties*.name

    /**
     * Renders a table with from a list of processes
     * @attr processList REQUIRED the list of processes to display on the table
     * @attr displayProperties OPTIONAL if specified only these properties are rendered
     */
    def processTable = {attr, body ->
        List<Process> processList = attr.processList
        def tableDisplayProperties = attr.tableDisplayProperties?: grailsApplication.config.processTracking.tableDisplayProperties
        def tableCellGenerators = grailsApplication.config.processTracking.tableCellGenerators
        tableDisplayProperties = cleanse(tableDisplayProperties)

        out << g.render(template: "/processtracking/templates/processTable",
                model: [
                    displayProperties: tableDisplayProperties,
                    processList: processList,
                    tableCellGenerators: tableCellGenerators
                    ]
                )
    }

    def processRow = {attr, body ->
        Process process = attr.process
    }

    private Map cleanse(map){
        map.findAll { key, value -> processDomainAttributes.contains(key)}
    }

}
