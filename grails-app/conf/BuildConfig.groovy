grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"

grails.project.dependency.resolution = {
    // inherit Grails' default dependencies
    inherits("global") {
        // uncomment to disable ehcache
        // excludes 'ehcache'
    }
    log "error" // log level of Ivy resolver, either 'error', 'warn', 'info', 'debug' or 'verbose'
    repositories {
        grailsCentral()
        grailsPlugins()
        grailsHome()
//        mavenLocal()
        mavenCentral()

        // uncomment these (or add new ones) to enable remote dependency resolution from public Maven repositories
//        mavenRepo "http://snapshots.repository.codehaus.org"
//        mavenRepo "http://repository.codehaus.org"
//        mavenRepo "http://download.java.net/maven/2/"
//        mavenRepo "http://repository.jboss.com/maven2/"
//        mavenRepo "http://repo.jenkins-ci.org/public/"
//        mavenRepo "http://download.eclipse.org/jgit/maven/"
    }
    dependencies {
        runtime 'mysql:mysql-connector-java:5.1.20'
        test ("org.jsoup:jsoup:1.7.2")
        compile("joda-time:joda-time-hibernate:1.3") {
            excludes "joda-time", "hibernate"
        }
//        compile 'org.jadira.usertype:usertype.jodatime:1.9'
//        compile 'joda-time:joda-time:2.1'
    }

    plugins {
        build(":tomcat:$grailsVersion")
        build(":release:2.0.3")
        {
            export = false
            excludes 'groovy' //Prevent trying to downloading all groovy versions
            excludes 'httpclient'
            excludes 'xml-apis'
        }
        build(":rest-client-builder:1.0.2")
        {
            export = false
            excludes 'groovy'
            excludes 'httpclient'
            excludes 'xml-apis'
        }
        compile (":lesscss-resources:1.3.3")
        compile (":joda-time:1.4")
        test ":spock:0.7"

        runtime (":hibernate:$grailsVersion") {
            export = false
        }
        runtime (":resources:1.2.RC2") {
            export = false
        }
        compile (":jquery:1.8.3"){
            export = false
        }
    }
}
