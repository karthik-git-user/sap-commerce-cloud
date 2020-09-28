<%@ tag body-content="empty" trimDirectiveWhitespaces="true"%>
<%@ attribute name="idKey" required="true" type="java.lang.String"%>
<%@ attribute name="labelKey" required="true" type="java.lang.String"%>
<%@ attribute name="path" required="true" type="java.lang.String"%>
<%@ attribute name="labelCSS" required="false" type="java.lang.String"%>
<%@ attribute name="inputCSS" required="false" type="java.lang.String"%>
<%@ attribute name="placeholder" required="false" type="java.lang.String"%>
<%@ attribute name="tabindex" required="false" rtexprvalue="true"%>
<%@ attribute name="autocomplete" required="false" type="java.lang.String"%>
<%@ attribute name="disabled" required="false" type="java.lang.Boolean"%>

<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template"%>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags"%>

<spring:htmlEscape defaultHtmlEscape="true" />

<template:errorSpanField path="${path}">
	<ycommerce:testId code="LoginPage_Item_${idKey}">
		<label class="control-label ${fn:escapeXml(labelCSS)}" for="${fn:escapeXml(idKey)}">
			<spring:theme code="${labelKey}" />
		</label>
			
		<spring:theme code="${placeholder}" var="placeHolderMessage" htmlEscape="false"/>

		<form:input type="date" cssClass="${fn:escapeXml(inputCSS)} form-control" id="${idKey}" path="${path}"
					tabindex="${tabindex}" autocomplete="${autocomplete}" placeholder="${placeHolderMessage}"
					disabled="${disabled}"/>
						
	</ycommerce:testId>
</template:errorSpanField>
