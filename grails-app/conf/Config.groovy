import org.codehaus.groovy.grails.commons.DefaultGrailsDomainClass
import grails.plugin.processtracking.Process
import spock.lang.Shared

// configuration for plugin testing - will not be included in the plugin zip
//grails.json.legacy.builder = false
//grails.converters.encoding = "ISO-8859-1"
////TODO - turn off
//grails.converters.json.pretty.print = true
//grails.converters.json.date = "javascript"
//grails.converters.json.domain.include.version = false

processTracking {
    tableDisplayProperties = [
            name: 'Name',
            initiated: 'Iinitiated',
            status: 'Status',
            userId: 'User',
            progress: 'Progress',
            complete: 'Complete',
            processGroup: 'ProcessGroup'
    ]
}

log4j = {
    // Example of changing the log pattern for the default console
    // appender:
    //
    appenders {
        console name: 'stdout', layout: pattern(conversionPattern: '%d [%t] %-5p %c{2} %x - %m%n')

    }
    root { info 'stdout' }
    warn 'org.codehaus.groovy.grails.web.servlet',  //  controllers
            'org.codehaus.groovy.grails.web.pages', //  GSP
            'org.codehaus.groovy.grails.web.sitemesh', //  layouts
            'org.codehaus.groovy.grails.web.mapping.filter', // URL mapping
            'org.codehaus.groovy.grails.web.mapping', // URL mapping
            'org.codehaus.groovy.grails.commons', // core / classloading
            'org.codehaus.groovy.grails.plugins', // plugins
            'org.springframework',
            'org.hibernate',
            'net.sf.ehcache.hibernate'

    warn 'org.mortbay.log'
//    debug    'org.hibernate.SQL'
    //Logs the actual params passed in the sql statements
//    trace 'org.hibernate.type'
}

