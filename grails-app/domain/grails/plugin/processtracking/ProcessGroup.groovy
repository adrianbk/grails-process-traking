package grails.plugin.processtracking

class ProcessGroup {
    String name
    Long total = 0L
    Long averageDuration = 0L

    static constraints = {
        name(maxSize: 256, nullable: false, blank: false)
        total(nullable: false, blank:false)
        averageDuration(nullable: false, required:true)
    }

    static mapping = {
        table "PROCESS_GROUP"
    }

}
