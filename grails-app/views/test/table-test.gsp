<%@ page import="org.joda.time.DateTime; grails.plugin.processtracking.*" contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title>Process Table Tests</title>
</head>
<body>
<%
    def processList =  (1 .. 10).collect  {
        new Process(
                name: "Test Process $it",
                initiated: new DateTime(),
                complete: new DateTime(),
                status: Process.ProcessStatus.SUCCESS,
                userId: 'Adrian',
                progress: 10F,
                processGroup: new ProcessGroup(averageDuration: 5600L, name: 'Test Group' )
        )
    }

%>
<processTracking:processTable processList="${processList}"/>

</body>
</html>