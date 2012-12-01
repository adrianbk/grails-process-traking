package grails.plugin.processtracking

import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.annotation.Propagation

import static grails.plugin.processtracking.Process.ProcessStatus.*
import static grails.plugin.processtracking.ProcessEvent.EventLevel.*
import org.codehaus.groovy.grails.support.PersistenceContextInterceptor
import groovy.time.TimeDuration
import groovy.time.TimeCategory
import grails.validation.ValidationException

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
        process.initiated = new Date()
        process.relatedDomainId = argRelatedDomainId;
        process.progress = 0F;
        process.name = argName
        process.status = QUEUED
        process.userId = argUserName
        persistenceInterceptor.init()
        process.save(failOnError: true)

        //One less db query than addTo.. hibernate updates the parents version after that addTo..
        ProcessEvent event = new ProcessEvent(
                message: "Process queued",
                eventLevel: INFO,
                timestamp: new Date(),
                process: process
        ).save(failOnError: true)
        persistenceInterceptor.flush()
        persistenceInterceptor.destroy()
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
                timestamp: new Date()
        )
        saveDomainWithPersistenceContext(processEvent)
    }

    /**
     * Initiate the process - sets the process to the PROCESSING state. Sets the initiated date to now.
     * @param argProcessId
     * @return a long - the unique identifier of the process details object
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    def initiateProcess(Long argProcessId) {
        Process process = Process.findById(argProcessId)
        process.initiated = new Date()
        process.status = Process.ProcessStatus.PROCESSING
        process.addToProcessEvents(new ProcessEvent(
                message: "Process initiated",
                eventLevel: INFO,
                timestamp: new Date()
        ))
        saveDomainWithPersistenceContext(process)
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
        process.progress = 100F
        process.complete = new Date()

        TimeDuration td = getTimeDuration(process)
        process.addToProcessEvents(new ProcessEvent(message: "Process complete. Duration: ${td}", eventLevel: INFO, timestamp: new Date()))


        int numErrorEvents = ProcessEvent.countByProcessAndEventLevel(process, ERROR)
        log.error("error Count: ${numErrorEvents}")
//        int numErrorEvents = ProcessEvent.executeQuery("select count(*) from ProcessEvent pe where pe.process.id = :pid and pe.eventLevel = :errorStatus", [pid: argProcessId, errorStatus: ERROR])[0]
        process.status = numErrorEvents ? FAILED : SUCCESS
        saveDomainWithPersistenceContext(process)
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
        saveDomainWithPersistenceContext(process)
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
        process.setComplete(new Date())
        process.setStatus(FAILED)

        TimeDuration td = getTimeDuration(process)
        process.addToProcessEvents(new ProcessEvent(message: "Process Failed. Duration: ${td}. ${argMessage}", eventLevel: ERROR, timestamp: new Date()))

        saveDomainWithPersistenceContext(process)

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
        process.setComplete(new Date())
        process.setStatus(FAILED)
        TimeDuration td = getTimeDuration(process)

        //If a grails validation exception occurs, resolve the validation error messages
        if (throwable instanceof ValidationException) {
            throwable.errors?.allErrors.each {
                process.addToProcessEvents(new ProcessEvent(message: "${messageSource.getMessage(it, null)}", eventLevel: ERROR, timestamp: new Date()))
            }
        }
        else {
            process.addToProcessEvents(new ProcessEvent(message: "Process Failed. Duration: ${td} ${throwable}", eventLevel: ERROR, timestamp: new Date()))
        }

        saveDomainWithPersistenceContext(process)
    }


    def TimeDuration getTimeDuration(Process process) {
        TimeDuration td = TimeCategory.minus(process.complete, process.initiated)
        td
    }

    def saveDomainWithPersistenceContext(domain) {
        persistenceInterceptor.init()
        if (!domain.save()) {
            domain.errors?.allErrors?.each {
                log.error(it)
            }
        }
        persistenceInterceptor.flush()
        persistenceInterceptor.destroy()
    }
}
