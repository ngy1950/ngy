<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<html>
<head>
<meta charset="UTF-8">
    <title>JSP Sample Page</title>
</head>
    <body>

       <div>[${name}]님의 시험성적입니다.</div>

        <c:forEach var="item" items="${list}">
         ${item} <br />
        </c:forEach>

    </body>
</html>