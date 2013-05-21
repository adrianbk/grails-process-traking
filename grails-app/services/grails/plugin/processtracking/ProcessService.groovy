package grails.plugin.processtracking

import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation

import static grails.plugin.processtracking.Process.ProcessStatus.*
import static grails.plugin.processtracking.ProcessEvent.EventLevel.*
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor
import groovy.time.TimeDuration
import groovy.time.TimeCategory
import grails.validation.ValidationException
import org.joda.time.LocalDateTime
import org.joda.time.DateTime

/**
 * A service class which should be used for all Process and ProcessEvent related CRUD
 *
 * Important: All persistence updates are wrapped in an new transaction with propagation as requires new so that
 * each operation is run in its own transaction and isolated from any surrounding transactions.
 * Consider process tracking as being synonymous with application logging - if a transaction fails in your application
 * you still want the process event to be stored i.e. not rolled back.
 *
 * The persistence context is managed using PersistenceContextInterceptor ensuring that any surrounding hibernate
 * session (or other persistence context) are isolated from the operations in this service. This facilitates process
 * tracking from concurrent threads.
 *
 * In short, every insert or update is completed in its own persistence context and transaction.
 *
 */
@Transactional(propagation = Propagation.REQUIRES_NEW)
class ProcessService {
    static transactional = false
    PersistenceContextInterceptor persistenceInterceptor
    def messageSource

    @Transactional(propagation = Propagation.REQUIRES_NEW)
        def Long createProcess(CreateProcessRequest createProcessRequest) {
        Process process = new Process()
        process.initiated = DateTime.now()

        if(null != createProcessRequest.processGroupId){
            process.processGroup = ProcessGroup.findById(createProcessRequest.processGroupId)
            process.processGroup.total += 1
        } else if(null != createProcessRequest.groupName){
            process.processGroup = new ProcessGroup(name: createProcessRequest.groupName, total: 1l)
            saveDomain(process.processGroup)
        }else{
//            process.processGroup = new ProcessGroup(name: createProcessRequest.processName)
//            saveDomain(process.processGroup)
        }


        process.relatedDomainId = createProcessRequest.relatedDomainId;
        process.progress = 0F;
        process.name = createProcessRequest.processName
        process.status = QUEUED
        process.userId = createProcessRequest.userId
        saveDomain(process)

        //One less db query than addTo.. hibernate updates the parents version after that addTo..
        ProcessEvent event = new ProcessEvent(
                message: createProcessRequest.queuedMessage,
                eventLevel: INFO,
                timestamp: DateTime.now(),
                process: process
        )
        saveDomain(event)
        return process.id
    }

    /**
     * Create a new Process supplying the name of the process
     * Process is set to the QUEUED state, progress to 0 and its unique identifier returned.
     * @param argName - the name of the process (as expected to appear to the user)
     * @param argUserName - the user executing/calling the process
     * @return a long - the unique identifier of the process details object
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def Long createProcess(String argName, String argUserName) {
        return createProcess(argName, null, argUserName)
    }

    /**
     * Create a new Process supplying the name and related domain id of the process
     * Process is set to the QUEUED state, progress to 0 and its unique identifier returned.
     * @param argName - the name of the process (as expected to appear to the user)
     * @param argUserName - the user executing/calling the process
     * @return a long - the unique identifier of the process details object
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def Long createProcess(String argName, argRelatedDomainId, String argUserName) {
        Process process = new Process()
        process.initiated = DateTime.now()
        process.relatedDomainId = argRelatedDomainId;
        process.progress = 0F;
        process.name = argName
        process.status = QUEUED
        process.userId = argUserName
        saveDomain(process)

        //One less db query than addTo.. hibernate updates the parents version after that addTo..
        ProcessEvent event = new ProcessEvent(
                message: "Process queued",
                eventLevel: INFO,
                timestamp: DateTime.now(),
                process: process
        )
        saveDomain(event)
        return process.id
    }

    /**
     * A method to add a ProcessEvent to a process details object
     * @param argProcessId - the unique identifier of the processDetails
     * @param argMessage - The message/text
     * @param argEventLevel - The EventLevel
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def void addProcessEvent(Long argProcessId, String argMessage, ProcessEvent.EventLevel argEventLevel) {
        Process process = Process.findById(argProcessId)
        ProcessEvent processEvent = new ProcessEvent(
                message: argMessage,
                eventLevel: argEventLevel,
                process: process,
                timestamp: DateTime.now()
        )
        saveDomain(processEvent)
    }

    /**
     * Initiate the process - sets the process to the PROCESSING state. Sets the initiated date to now.
     * @param argProcessId
     * @return a long - the unique identifier of the process details object
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def initiateProcess(Long argProcessId) {
        Process process = Process.findById(argProcessId)
        process.initiated = DateTime.now()
        process.status = Process.ProcessStatus.PROCESSING
        process.addToProcessEvents(new ProcessEvent(
                message: "Process initiated",
                eventLevel: INFO,
                timestamp: DateTime.now()
        ))
        saveDomain(process)
    }

    /**
     * Completes the process.
     * If the process has any events with an error status - the process details is set to FAILED
     * @param argProcessId - the process id(unique identifier)
     * @param argProcessStatus - the completion status.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def void completeProcess(Long argProcessId) {
        Process process = Process.findById(argProcessId)
        process.complete = DateTime.now()
        process.progress = 100F

        TimeDuration td = getTimeDuration(process)
        process.addToProcessEvents(new ProcessEvent(message: "Process complete. Duration: ${td}", eventLevel: INFO, timestamp: DateTime.now()))


        int numErrorEvents = ProcessEvent.countByProcessAndEventLevel(process, ERROR)
        process.status = numErrorEvents > 0 ? FAILED : SUCCESS
        if(process.status == SUCCESS){
            Long calculatedAverage = calculateAverageDuration(process, td)
            process.processGroup?.averageDuration =  calculatedAverage
        }
        saveDomain(process)
    }

    private Long calculateAverageDuration(Process process, TimeDuration td) {
        ProcessGroup processGroup = process.processGroup
        if (null != processGroup) {
            if (processGroup.averageDuration == 0) {
                return td.millis
            } else {
                Long sum = processGroup.averageDuration + td.millis
                return sum / 2
            }
        }
        return 0L
    }

    /**
     * Increments the process status
     * @param argProcessId - id of the Process
     * @param argProgressPercent - amount to increment by.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def void incrementProcessProgress(Long argProcessId, Long argProgressPercent) {
        Process process = Process.findById(argProcessId)
        process.progress = process.progress += argProgressPercent
        saveDomain(process)
    }

    /**
     * A method to fail a process with a message that the caller supplies
     * Sets the process to failed and adds an event with the supplied message
     * @param argProcessId - the process id
     * @param argMessage - message
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def void failProcess(Long argProcessId, String argMessage) {
        Process process = Process.findById(argProcessId)
        process.setComplete(DateTime.now())
        process.setStatus(FAILED)
        DateTime now = DateTime.now()
        TimeDuration td = getTimeDuration(process)
        process.addToProcessEvents(new ProcessEvent(message: "Process Failed. Duration: ${td}. ${argMessage}", eventLevel: ERROR, timestamp: now))

        saveDomain(process)

    }

    /**
     * A method to fail a process when an exception is thrown/
     * Sets the process to failed and adds an event with the details of the exception/
     * @param argProcessId - the process id
     * @param throwable - Throwable
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def void failProcess(Long argProcessId, Throwable throwable) {
        Process process = Process.findById(argProcessId)
        DateTime complete = DateTime.now()
        process.setComplete(complete)
        process.setStatus(FAILED)
        TimeDuration td = getTimeDuration(process)

        //If a grails validation exception occurs, resolve the validation error messages
        if (throwable instanceof ValidationException) {
            throwable.errors?.allErrors.each {
                process.addToProcessEvents(new ProcessEvent(message: "${messageSource.getMessage(it, null)}", eventLevel: ERROR, timestamp: DateTime.now()))
            }
        }
        else {
            process.addToProcessEvents(new ProcessEvent(message: "Process Failed. Duration: ${td} ${throwable}", eventLevel: ERROR, timestamp: DateTime.now()))
        }

        saveDomainWithPersistenceContext(process)
    }

    /**
     * @deprecated persistenceInterceptor does not work with multiple datasources
     * see: http://jira.grails.org/browse/GRAILS-9773
     * @param domain
     * @return
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveDomainWithPersistenceContext(domain) {
//        Wrap with persistenceInterceptor since this service may be called asynchronously (threads without a bound persistence session)
        persistenceInterceptor.init()
        if (!domain.save()) {
            domain.errors?.allErrors?.each {
                log.error(it)
            }
        }
        else {
            //Only flush and destroy if success otherwise ERROR hibernate.AssertionFailure  - an assertion failure
            // occured (this may indicate a bug in Hibernate, but is more likely due to unsafe use of the session)
            persistenceInterceptor.flush()
            persistenceInterceptor.destroy()
        }

    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly=true)
    public Process fetchProcess(Long pid){
        Process.findById(pid)
    }

    private void saveDomain(domain) {
        Process.withSession{session ->
            if (!domain.save()) {
                domain.errors?.allErrors?.each {
                    log.error(it)
                }
            }
        }
    }

    def TimeDuration getTimeDuration(Process process) {
        TimeDuration td = TimeCategory.minus(process.complete.toDate(), process.initiated.toDate())
        td
    }
}
