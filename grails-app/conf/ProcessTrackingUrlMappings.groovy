class UrlMappings {

	static mappings = {
        "/test/table-test" (view: '/test/table-test')
        "/tile"(controller: "process", action: 'tile')


        "/"(view:"/index")
        "500"(view:'/error')

	}
}
