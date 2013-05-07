package grails.plugin.processtracking

class CreateProcessRequest {

    String processName = "Default Process"
    String userId = null
    Long relatedDomainId = null
    String groupName = null
    String queuedMessage = "Process queued"
    ProcessGroup processGroup = null

    /**
     * The process name/label
     * @param processName
     * @return
     */
    def withProcessName(processName){
        this.processName = processName
        this
    }

    /**
     * The userId to assign to the process
     * @param userId
     * @return
     */
    def withUserId(userId){
        this.userId = userId
        this
    }

    /**
     *
     * An optional domain object Id to associate with the process
     * @param relatedDomainId
     * @return
     */
    def withRelatedDomainId(relatedDomainId){
        this.relatedDomainId = relatedDomainId
        this
    }

    /**
     * The group to associate this process with - a new ProcessGroup will be created and persisted.
     * If you wish to use an existing group - pass the ProcessGroup using the withProcessGroup method
     * @param groupName
     * @return
     */
    def withGroupName(groupName){
        this.groupName = groupName
        this
    }

    /**
     * Associated the process with and existing ProcessGroup. Calling this method and hence specifying a ProcessGroup
     * will always associate the process with the group - the groupName will be ignored.
     * @param processGroup
     * @return
     */
    def withProcessGroup(processGroup){
        this.processGroup = processGroup
        this
    }

    /**
     * A new process is created with a single child ProcessEvent to represent that the process is queued once its created.
     * Specify this if you wish to override the default queued message.
     * @param queuedMessage
     * @return
     */
    def withQueuedMessage(queuedMessage){
        this.queuedMessage = queuedMessage
        this
    }
}
