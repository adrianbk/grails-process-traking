package grails.plugin.processtracking

class Process {

    public enum ProcessStatus {
        QUEUED, PROCESSING, SUCCESS, FAILED
    }
    //Use hibernate bags for performance - order managed elsewhere
    Collection processEvents
    static hasMany = [processEvents: ProcessEvent]
    //Persisted members
    String name
    Long relatedDomainId //An optional domain id to relate a process to another domain object
    Date initiated
    Date complete
    Float progress //Progress percentage
    ProcessStatus status
    String userId
    String processGroup
    Date dateCreated
    Date lastUpdated


    static constraints = {
        name(blank: false, nullable: false)
        relatedDomainId(blank: true, nullable: true, required:false)
        initiated(blank: false, nullable: false)
        complete(blank: true, nullable: true)
        progress(blank: false, nullable: false, max: 100F, min:  0F)
        status(nullable: false, required:true, blank:false)
        userId(blank: true, nullable: true, required:false)
        processGroup(blank: true, nullable: true, required:false)
        dateCreated(editable: false, required: true)
        lastUpdated(editable: false, required: true)
    }

    static mapping = {
        table "PROCESS"
        version false //Turn off Optimistic Locking
        processEvents cascade: "all-delete-orphan"
    }
}
