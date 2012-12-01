package grails.plugin.processtracking

class ProcessEvent {

    public enum EventLevel {
        DEBUG, INFO, WARN, ERROR
    }

    static belongsTo = [process: Process]
    String message;
    EventLevel eventLevel
    Date dateCreated
    Date lastUpdated
    Date timestamp

    static constraints = {
        message(maxSize: 3000, nullable: false, blank: false)
        process(nullable: false)
        dateCreated(editable: false, required: true)
        lastUpdated(editable: false, required: true)
        timestamp(editable: false, required: true)
    }

    /** table mappings */
    static mapping = {
        table "PROCESS_EVENT"
        sort id: "asc"
    }

    /* Trim the message in case a stacktrace is the message */
    void setMessage(String d) {
        message = d?.length() > 3000 ? d.substring(0, 3000) : d
    }
}
