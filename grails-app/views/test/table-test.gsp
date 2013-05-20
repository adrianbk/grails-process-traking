<%@ page import="grails.plugin.processtracking.*" contentType="text/html;charset=UTF-8" %>
<html>
<head>
  <title></title>
</head>
<body>
<%
    List<Process> processList = []
%>
<processTracking:processTable processList="${processList}"/>

</body>
</html>