package grails.plugin.processtracking

class UnitTestHelper {

    static isValidDomain(domain, expected) {
        def valid = !domain.hasErrors()
        if (expected == true && !valid) { domain.errors.allErrors.each {println it}}
        return valid
    }
}
