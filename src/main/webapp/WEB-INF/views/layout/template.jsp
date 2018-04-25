<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" language="java" %>
<html>
<head>
    <title>Excel Parser</title>
    <script type="text/javascript" src="../../../frameworks/jquery-3.3.1.min.js"></script>
    <link rel="stylesheet" href="../../../frameworks/bootstrap-4/css/bootstrap.min.css">
    <script type="text/javascript" src="../../../frameworks/bootstrap-4/js/bootstrap.min.js"></script>
    <link rel="stylesheet" href="../../../css/style.css">
    <c:forEach items="${css}" var="href">
        <link rel="stylesheet" href="../../../css/${href}">
    </c:forEach>
    <c:forEach items="${js}" var="src">
        <script type="text/javascript" src="../../../js/${src}"></script>
    </c:forEach>
</head>
<body>
<header>
    <jsp:include page="header.jsp"/>
</header>
<main style="">
    <jsp:include page="../${body}"/>
</main>
</body>
</html>
