package grails.plugin.processtracking

import org.joda.time.DateTime
import org.joda.time.contrib.hibernate.PersistentDateTime

class Process {

    public enum ProcessStatus {
        QUEUED, PROCESSING, SUCCESS, FAILED
    }
    //Use hibernate bags for performance - order managed elsewhere
    Collection processEvents
    ProcessGroup processGroup
    static hasMany = [processEvents: ProcessEvent]
    static fetchMode = [processEvents: 'eager']

    //Persisted members
    String name
    Long relatedDomainId //An optional domain id to relate a process to another domain object
    DateTime initiated
    DateTime complete
    Float progress //Progress percentage
    ProcessStatus status
    String userId
    DateTime dateCreated
    DateTime lastUpdated



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
        datasource 'processTracking'
        table "PROCESS"
        version false //Turn off Optimistic Locking
        processEvents cascade: "all-delete-orphan"
        processGroup lazy: false
        dateCreated type: PersistentDateTime
        lastUpdated type: PersistentDateTime
        initiated type: PersistentDateTime
        complete type: PersistentDateTime
    }
}
