package grails.plugin.processtracking

class ProcessController {
    def processService

    def index() {
        redirect(action: 'list')
    }

    def list(){
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        render(view: 'list', model: [processList: Process.list(params) , processTotal: Process.count()])
    }

    def tile(){
        log.error("called")
        render(view: '/processtracking/tile')
    }

    def jsonList() {
        def processIds = params.list('processIds')
        def idsAsLongs = processIds*.asType(Long)
        def processList = Process.findAllByIdInList(idsAsLongs, [fetch: [processEvents: 'eager']])

        def json = new groovy.json.JsonBuilder()
        json {
            processes(
                processList.collect { Process p ->
                    [process: [
                                id: p.id,
                                name: p.name,
                                relatedDomainId: p.relatedDomainId,
                                initiated: toJsDate(p.initiated),
                                complete: toJsDate(p.complete),
                                status: p.status.toString(),
                                userId: p.userId,
                                dateCreated: toJsDate(p.dateCreated),
                                lastUpdated: toJsDate(p.lastUpdated),
                                processEvents: (
                                    p.processEvents.collect {ProcessEvent pe ->
                                        [processEnent: [
                                                id: pe.id,
                                                message: pe.message,
                                                eventLevel: pe.eventLevel.toString(),
                                                dateCreated: toJsDate(pe.dateCreated),
                                                lastUpdated: toJsDate(pe.lastUpdated),
                                                timestamp: toJsDate(pe.timestamp)
                                            ]
                                        ]
                                    }
                                )
                            ]
                    ]
                }
            )
        }
        render(json.toPrettyString())
    }

    private String toJsDate(Date date){
        date?.getTime()
    }

}
