package grails.plugin.processtracking

import spock.lang.Specification
import geb.Page
import geb.spock.GebSpec

class SanitySpec extends GebSpec{

    def "open gogle"(){
      when:
       to GoogleHomePage
      then:
       at GoogleHomePage
    }
 }

class GoogleHomePage extends Page {
    static url = 'http://google.com'
    static at = { title.contains("oogle") }
}