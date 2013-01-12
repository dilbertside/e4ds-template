<!doctype html>
<%@ page language="java" isErrorPage="true" pageEncoding="UTF-8" contentType="text/html; charset=utf-8"%>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="java.util.Locale"%>
<%@page import="org.springframework.web.servlet.support.RequestContextUtils"%>
<html>
<head>
<meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
<meta charset="utf-8">
<link rel="shortcut icon" href="<c:url value="/favicon.ico"/>" />
<title>e4ds-template</title>
</head>
<body>
   <h2>Ooops!!! Serious Internal error caught</h2>
   <p />
   <%
   	try {
   		// The Servlet spec guarantees this attribute will be available
   		Throwable exc = (Throwable) request.getAttribute("exc");
   		if (exc == null)
   			exc = (Throwable) request.getAttribute("javax.servlet.error.exception");

   		if (exc != null) {
   			if (exc instanceof ServletException) {
   				// It's a ServletException: we should extract the root cause
   				ServletException sex = (ServletException) exc;
   				Throwable rootCause = sex.getRootCause();
   				if (rootCause == null)
   					rootCause = sex;
   				out.println("** Root cause is:");
   				out.println(rootCause.getMessage());
   				rootCause.printStackTrace(new java.io.PrintWriter(out));
   			} else {
   				// It's not a ServletException, so we'll just show it
   				if (exc instanceof org.springframework.security.access.AccessDeniedException) {
   					out.println("{\"type\":\"exception\",\"message\":\"403\",\"data\":\"" + exc.getMessage() + "\"}");
   				} else
   					exc.printStackTrace(new java.io.PrintWriter(out));
   			}
   		} else {
   			out.println("No error information available");
   		}

   		// Display cookies
   		out.println("\nCookies:\n");
   		Cookie[] cookies = request.getCookies();
   		if (cookies != null) {
   			for (int i = 0; i < cookies.length; i++) {
   				out.println(cookies[i].getName() + "=[" + cookies[i].getValue() + "]");
   			}
   		}

   	} catch (Exception ex) {
   		ex.printStackTrace(new java.io.PrintWriter(out));
   	}
   %>
   <p />
   <br />
</body>
</html>
