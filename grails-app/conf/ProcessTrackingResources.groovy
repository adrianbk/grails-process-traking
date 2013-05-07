modules = {

    core {
        defaultBundle "app"
    }
    processtile {
        dependsOn 'jquery' //Having jquery in core and dependsOn core here doesnt work??
        defaultBundle "app"
        resource url: 'less/process-tile.less', attrs: [rel: "stylesheet/less", type: 'css'], bundle: "app"
        resource url: "css/less-plugin-bug.css" //Bug with less resources, have to have a .css file last
    }
}