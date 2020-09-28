<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="template" tagdir="/WEB-INF/tags/responsive/template" %>
<%@ taglib prefix="cms" uri="http://hybris.com/tld/cmstags" %>
<%@ taglib prefix="multiCheckout" tagdir="/WEB-INF/tags/responsive/checkout/multi" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ taglib prefix="formElement" tagdir="/WEB-INF/tags/responsive/formElement" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<%@ taglib prefix="address" tagdir="/WEB-INF/tags/responsive/address" %>
<%@ taglib prefix="ycommerce" uri="http://hybris.com/tld/ycommercetags" %>
<%@ taglib prefix="multiCheckoutNovalnet" tagdir="/WEB-INF/tags/addons/novalnetcheckoutaddon/responsive/checkout/multi" %>

<spring:htmlEscape defaultHtmlEscape="true"/>

<template:page pageTitle="${pageTitle}" hideHeaderLinks="true">
    <div class="row">
        <div class="col-sm-6">
            <div class="checkout-headline">
                <span class="glyphicon glyphicon-lock"></span>
                <spring:theme code="checkout.multi.secure.checkout"/>
            </div>
            <multiCheckout:checkoutSteps checkoutSteps="${checkoutSteps}" progressBarId="${progressBarId}">
                <jsp:body>
                    <ycommerce:testId code="checkoutStepThree">
                        <div class="checkout-paymentmethod">
                            <div class="checkout-indent">

                                <div class="headline"><spring:theme code="checkout.multi.paymentMethod"/></div>

                                    <ycommerce:testId code="paymentDetailsForm">

                                        <form:form id="paymentDetailsForm" name="paymentDetailsForm" modelAttribute="paymentDetailsForm"  method="POST">
                                        
                                        <div id="billingAdrressInfo" style="display:block">
                                            <h1 class="headline">
                                                <spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.billingAddress"/></h1>

                                            <c:if test="${cartData.deliveryItemsQuantity > 0}">
												<div id="useDeliveryAddressData"
													data-title="${fn:escapeXml(deliveryAddress.title)}"
													data-firstname="${fn:escapeXml(deliveryAddress.firstName)}"
													data-lastname="${fn:escapeXml(deliveryAddress.lastName)}"
													data-line1="${fn:escapeXml(deliveryAddress.line1)}"
													data-line2="${fn:escapeXml(deliveryAddress.line2)}"
													data-town="${fn:escapeXml(deliveryAddress.town)}"
													data-postalcode="${fn:escapeXml(deliveryAddress.postalCode)}"
													data-countryisocode="${fn:escapeXml(deliveryAddress.country.isocode)}"
													data-regionisocode="${fn:escapeXml(deliveryAddress.region.isocodeShort)}"
													data-address-id="${fn:escapeXml(deliveryAddress.id)}"
												></div>
												
												<formElement:formCheckbox
													path="useDeliveryAddress"
													idKey="useDeliveryAddress"
													labelKey="checkout.multi.sop.useMyDeliveryAddress"
													tabindex="11"/>
											</c:if>
											
											<div id="novalnetBillAddressForm">
												<address:billAddressFormSelector supportedCountries="${countries}" regions="${regions}" tabindex="12"/>
											</div>
											
											<h1 class="headline">
                                                <spring:theme code="checkout.summary.select.payment.method"/></h1>
											
										<input type="hidden" id="ship_firstName" value="${fn:escapeXml(deliveryAddress.firstName)}"/>
										<input type="hidden" id="ship_lastName" value="${fn:escapeXml(deliveryAddress.lastName)}"/>
										<input type="hidden" id="ship_zip" value="${fn:escapeXml(deliveryAddress.postalCode)}"/>
										<input type="hidden" id="ship_country" value="${fn:escapeXml(deliveryAddress.country.isocode)}"/>
										<input type="hidden" id="ship_city" value="${fn:escapeXml(deliveryAddress.town)}"/>
										<input type="hidden" id="ship_street" value="${fn:escapeXml(deliveryAddress.line1)} ${fn:escapeXml(deliveryAddress.line2)}"/>
										<input type="hidden" id="email" value="${email} "/>
                                        
                                        <c:if test="${novalnetBaseStoreConfiguration.novalnetTariffId != null && novalnetBaseStoreConfiguration.novalnetPaymentAccessKey != null}">
                                        <div class="paymentMethods">
										<c:if test="${novalnetCreditCard.active == true}">
										
											<div class="form-group">
												<form:radiobutton path="selectedPaymentMethodId" id="novalnetCreditCard" value="novalnetCreditCard" label="${novalnetCreditCard.name}"/>
												
												&nbsp;&nbsp;<a href=<spring:theme code="http://www.novalnet.com"/> target="_new">
												
												<c:choose>
													<c:when test="${novalnetCreditCard.novalnetAmexLogo == true}">
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnet_cc_visa_master_amex.png" />
													</c:when>    
													<c:otherwise>
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnet_cc_visa_master.png" />
													</c:otherwise>
												</c:choose>
												
												
												</a>&nbsp;&nbsp;
												<div id="novalnetCreditCardPaymentForm" style="display:none;" class="novalnetPaymentForm">	
													<c:if test="${novalnetCreditCard.novalnetTestMode == true}">
																																												<input type="hidden" id="novalnetTestMode" value=1/>
																																												<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													<div class="description">${novalnetCreditCard.description}
													</div>
													<form:hidden path="novalnetCreditCardPanHash" id="novalnetCreditCardPanHash"/>
														<form:hidden path="novalnetCreditCardUniqueId" id="novalnetCreditCardUniqueId"/>
														<form:hidden path="do_redirect" id="do_redirect"/>
														
														<input type="hidden" id="novalnetStandardLabelCss" value="${novalnetCreditCard.novalnetStandardLabelCss}"/>
														<input type="hidden" id="novalnetStandardInputCss" value="${novalnetCreditCard.novalnetStandardInputCss}"/>
														<input type="hidden" id="novalnetStandardCss" value="${novalnetCreditCard.novalnetStandardCss}"/>
														<c:if test="${novalnetCreditCard.novalnetInlineCC == true}">
															<input type="hidden" id="novalnetInlineCC" value=1/>
														</c:if>
														<input type="hidden" id="lang" value="${lang}"/>
														<input type="hidden" id="orderAmount" value="${orderAmountCent}"/>
														<input type="hidden" id="currency" value="${currency}"/>
														<br/>
														<input type="hidden" id="Clientkey" value="${novalnetBaseStoreConfiguration.novalnetClientKey}"/>
														
													<c:if test="${novalnetCreditCardOneClick == true}">
														<script src="https://cdn.novalnet.de/js/v2/Novalnet.js"></script>
														
														<form:radiobutton path="creditCardOneClickData1" id="creditCardOneClickData1" value="1" label="${novalnetCreditCardOneClickCardType} ${novalnetCreditCardOneClickCardHolder} ${novalnetCreditCardOneClickMaskedCardNumber} (${novalnetCreditCardOneClickCardExpiry}) " tabindex="12"/>
														<br/>
														<c:if test="${novalnetCreditCardOneClickToken2 != null}">
															<form:radiobutton path="creditCardOneClickData1" id="creditCardOneClickData1" value="2" label="${novalnetCreditCardOneClickCardType2} ${novalnetCreditCardOneClickCardHolder2} ${novalnetCreditCardOneClickMaskedCardNumber2} (${novalnetCreditCardOneClickCardExpiry2}) " tabindex="12"/>
														<br/>
														</c:if>
														<form:radiobutton path="creditCardOneClickData1" id="creditCardOneClickNewDeatails" value="3" label="Add new card details" tabindex="12"/>
														<div class="novalnetCreditCardOneClickForm" style = "display:none">
														
														<iframe id='novalnetCreditCardIframe' frameborder='0' scrolling='no'></iframe>
														</div>
														 
													</c:if>
													<c:if test="${novalnetCreditCardOneClick == false}">
													<span id="novalnetCreditCardPaymentFormElements">	
														<script src="https://cdn.novalnet.de/js/v2/Novalnet.js"></script>
														 <iframe id="novalnetCreditCardIframe" frameborder="0" scrolling="no"></iframe>
														 
													</span>		
													</c:if>	
												</div>	
											</div>
										</c:if>			
										<c:if test="${novalnetGuaranteedDirectDebitSepa.active == true}">
										
											<div class="novalnet-select-payment">
												<form:radiobutton path="selectedPaymentMethodId" id="novalnetGuaranteedDirectDebitSepa" value="novalnetGuaranteedDirectDebitSepa" label="${novalnetGuaranteedDirectDebitSepa.name}"/>
												&nbsp;&nbsp;
												<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetGuaranteedDirectDebitSepa.png" />
												</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetGuaranteedDirectDebitSepaPaymentForm" style="display:none;" class="novalnetPaymentForm">
												<div class="description">${novalnetGuaranteedDirectDebitSepa.description}</div><br/>
												<c:if test="${novalnetGuaranteedDirectDebitSepa.novalnetTestMode == true}">
													<div color="red" id= "testModeText">
														test mode
													</div>
												</c:if>
												<div class="form-group">
													<formElement:formInputBox idKey="accountIban" labelKey="novalnet.iban" path="accountIban" inputCSS="form-control" tabindex="2" mandatory="true" />
												</div>
												<div class="form-group">
													<multiCheckoutNovalnet:formDate path="novalnetGuaranteedDirectDebitSepaDateOfBirth" idKey="novalnetGuaranteedDirectDebitSepaDateOfBirth" labelKey="novalnet.dob" inputCSS="" labelCSS=""/>
												</div>
											</div>
										</c:if>
										<c:if test="${novalnetDirectDebitSepa.active == true}">
										
											<div class="novalnet-select-payment">
												<form:radiobutton path="selectedPaymentMethodId" id="novalnetDirectDebitSepa" value="novalnetDirectDebitSepa" label="${novalnetDirectDebitSepa.name}"/>
												&nbsp;&nbsp;
												<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetDirectDebitSepa.png" />
												
												
												</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetDirectDebitSepaPaymentForm" style="display:none;" class="novalnetPaymentForm">
												<div class="description">${novalnetDirectDebitSepa.description}</div><br/>
												
												<c:if test="${novalnetDirectDebitSepa.novalnetTestMode == true}">
			
													<div color="red" id= "testModeText">
														test mode
													</div>
												</c:if>
												<c:if test="${novalnetDirectDebitSepaOneClick == true}">													
													<form:radiobutton path="directDebitSepaOneClickData1" id="directDebitSepaOneClickData1" value="1" label="${novalnetDirectDebitSepaAccountHolder} ${novalnetDirectDebitSepaAccountIban}" tabindex="12"/>
													<br/>
													<c:if test="${novalnetDirectDebitSepaOneClickToken2 != null}">
														<form:radiobutton path="directDebitSepaOneClickData1" id="directDebitSepaOneClickData1" value="2" label="${novalnetDirectDebitSepaAccountHolder2} ${novalnetDirectDebitSepaAccountIban2}" tabindex="12"/>
														<br/>
													</c:if>
													<form:radiobutton path="directDebitSepaOneClickData1" id="directDebitSepaOneClickData1AddNew" value="3" label="Add new Account details" tabindex="12"/>
													<div class="novalnetDirectDebitSepaOneClickForm" style = "display:none">
														<div class="form-group">
																<formElement:formInputBox idKey="accountIban" labelKey="novalnet.iban" path="accountIban" inputCSS="form-control" tabindex="2" mandatory="true" />
														</div>
														<div class="form-group">
															<a id="novalnet-sepa-mandate" style="cursor:pointer;" onclick="jQuery( '#novalnet-about-mandate' ).toggle();" ><spring:theme code="novalnet.sepaNotificationText"/></a>
														</div>
														<div class="form-group" id="novalnet-about-mandate" style="display:none;">
															<spring:theme code="novalnet.sepaAboutMandateDescOne"/><br/><br/><strong><spring:theme code="novalnet.sepaAboutMandateDescTwo"/></strong><br/><br/><spring:theme code="novalnet.sepaAboutMandateDescThree"/>
														</div>
													</div>
													
												</c:if>
												<c:if test="${novalnetDirectDebitSepaOneClick == false}">
																					
													<span id="novalnetDirectDebitSepaPaymentFormElements">
														<div class="form-group">
															<formElement:formInputBox idKey="accountIban" labelKey="novalnet.iban" path="accountIban" inputCSS="form-control" tabindex="2" mandatory="true" />
														</div>
													
													<div class="form-group">
														<a id="novalnet-sepa-mandate" style="cursor:pointer;" onclick="jQuery( '#novalnet-about-mandate' ).toggle();" ><spring:theme code="novalnet.sepaNotificationText"/></a>
													</div>
													<div class="form-group" id="novalnet-about-mandate" style="display:none;">
														<spring:theme code="novalnet.sepaAboutMandateDescOne"/><br/><br/><strong><spring:theme code="novalnet.sepaAboutMandateDescTwo"/></strong><br/><br/><spring:theme code="novalnet.sepaAboutMandateDescThree"/>
													</div>
													</span>
												</c:if>
											</div>
										</c:if>
										<c:if test="${novalnetPayPal.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetPayPal" value="novalnetPayPal" label="${novalnetPayPal.name}"/>
													&nbsp;&nbsp;
													
													
													<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetPayPal.png" />
													
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetPayPalPaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetPayPal.description}</div><br/>
													
													<c:if test="${novalnetPayPal.novalnetTestMode == true}">
																																										<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													
													<c:if test="${novalnetPayPal.novalnetEndUserInfo != null}">
														<br/><br/>${novalnetPayPal.novalnetEndUserInfo}
													</c:if>
														<c:if test="${novalnetPayPalOneClick == true}">													
																<form:radiobutton path="payPalOneClickData1" id="payPalOneClickData1" value="1" label="${novalnetPaypalEmailID} ${novalnetPayPalTransactionId}" tabindex="12"/>
																<br/>
																<c:if test="${novalnetPayPalOneClickToken2 != null}">
																	<form:radiobutton path="payPalOneClickData1" id="payPalOneClickData1" value="2" label="${novalnetPaypalEmailID2} ${novalnetPayPalTransactionId2}" tabindex="12"/>
																	<br/>
																</c:if>
																<form:radiobutton path="payPalOneClickData1" id="payPalOneClickData1AddNew" value="3" label="Add new Account details" tabindex="12"/>
																<div class="novalnetPayPalOneClickForm" style = "display:none">
																<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" style="width:10px;height:10px;"><path style="fill:#0080c9;" d="M400 480H48c-26.51 0-48-21.49-48-48V80c0-26.51 21.49-48 48-48h352c26.51 0 48 21.49 48 48v352c0 26.51-21.49 48-48 48zm-204.686-98.059l184-184c6.248-6.248 6.248-16.379 0-22.627l-22.627-22.627c-6.248-6.248-16.379-6.249-22.628 0L184 302.745l-70.059-70.059c-6.248-6.248-16.379-6.248-22.628 0l-22.627 22.627c-6.248 6.248-6.248 16.379 0 22.627l104 104c6.249 6.25 16.379 6.25 22.628.001z"/></svg>
																	<spring:theme code="novalnet.paypal.saveData"/><br/><br/>
																</div>
														</c:if>
														<c:if test="${novalnetPayPalOneClick == false}">
													<span id="novalnetPayPalPaymentFormElements">
														<c:if test="${novalnetPayPalOneClickEnabled == true}">
															<sec:authorize access="!hasAnyRole('ROLE_ANONYMOUS')">
																<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 448 512" style="width:10px;height:10px;"><path style="fill:#0080c9;" d="M400 480H48c-26.51 0-48-21.49-48-48V80c0-26.51 21.49-48 48-48h352c26.51 0 48 21.49 48 48v352c0 26.51-21.49 48-48 48zm-204.686-98.059l184-184c6.248-6.248 6.248-16.379 0-22.627l-22.627-22.627c-6.248-6.248-16.379-6.249-22.628 0L184 302.745l-70.059-70.059c-6.248-6.248-16.379-6.248-22.628 0l-22.627 22.627c-6.248 6.248-6.248 16.379 0 22.627l104 104c6.249 6.25 16.379 6.25 22.628.001z"/></svg>
																<spring:theme code="novalnet.paypal.saveData"/><br/><br/>
															</sec:authorize>
														</c:if>
													</span>
													</c:if>
											</div>
										</c:if>
										<c:if test="${novalnetInvoice.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetInvoice" value="novalnetInvoice" label="${novalnetInvoice.name}"/>
													&nbsp;&nbsp;
													
													
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetInvoice.png" />
													
													
													
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetInvoicePaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetInvoice.description}</div><br/>
													
													<c:if test="${novalnetInvoice.novalnetTestMode == true}">
																																							<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													
													
													
											</div>
										</c:if>
										<c:if test="${novalnetGuaranteedInvoice.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetGuaranteedInvoice" value="novalnetGuaranteedInvoice" label="${novalnetGuaranteedInvoice.name}"/>
													&nbsp;&nbsp;
													
													
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetGuaranteedInvoice.png" />
													
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetGuaranteedInvoicePaymentForm" style="display:none;" class="novalnetPaymentForm">
												<div class="description">${novalnetGuaranteedInvoice.description}</div><br/>
												
												<c:if test="${novalnetGuaranteedInvoice.novalnetTestMode == true}">
																																						<div color="red" id= "testModeText">
														test mode
													</div>

												</c:if>
													
												<div class="form-group">
													<multiCheckoutNovalnet:formDate path="novalnetGuaranteedInvoiceDateOfBirth" idKey="novalnetGuaranteedInvoiceDateOfBirth" labelKey="novalnet.dob" inputCSS="" labelCSS=""/>
												</div>
													
											</div>
										</c:if>
										<c:if test="${novalnetPrepayment.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetPrepayment" value="novalnetPrepayment" label="${novalnetPrepayment.name}"/>
													&nbsp;&nbsp;
													
													
													<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetPrepayment.png" />
													
													
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetPrepaymentPaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetPrepayment.description}</div><br/>
													
													<c:if test="${novalnetPrepayment.novalnetTestMode == true}">
																																							<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													
											</div>
										</c:if>
										<c:if test="${novalnetBarzahlen.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetBarzahlen" value="novalnetBarzahlen" label="${novalnetBarzahlen.name}"/>
													&nbsp;&nbsp;
													
													
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetBarzahlen.png" />
													
													
													
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetBarzahlenPaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetBarzahlen.description}</div><br/>
													
													<c:if test="${novalnetBarzahlen.novalnetTestMode == true}">
																																							<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													
											</div>
										</c:if>
										<c:if test="${novalnetIdeal.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetIdeal" value="novalnetIdeal" label="${novalnetIdeal.name}"/>
													&nbsp;&nbsp;
													
													
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetIdeal.png" />
													
													
													
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetIdealPaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetIdeal.description}</div><br/>
													
													<c:if test="${novalnetIdeal.novalnetTestMode == true}">
																																							<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													
											</div>
										</c:if>
										<c:if test="${novalnetGiropay.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetGiropay" value="novalnetGiropay" label="${novalnetGiropay.name}"/>
													&nbsp;&nbsp;
													
													
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetGiropay.png" />
													
												
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetGiropayPaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetGiropay.description}</div><br/>
													
													<c:if test="${novalnetGiropay.novalnetTestMode == true}">
																																							<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													
											</div>
										</c:if>
										<c:if test="${novalnetPrzelewy24.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetPrzelewy24" value="novalnetPrzelewy24" label="${novalnetPrzelewy24.name}"/>
													&nbsp;&nbsp;
													
													
														
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetPrzelewy24.png" />
												
												
											
													
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetPrzelewy24PaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetPrzelewy24.description}</div><br/>
													
													<c:if test="${novalnetPrzelewy24.novalnetTestMode == true}">
																																							<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													
											</div>
										</c:if>
										<c:if test="${novalnetEps.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetEps" value="novalnetEps" label="${novalnetEps.name}"/>
													&nbsp;&nbsp;
													
													
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetEps.png" />
													
													
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetEpsPaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetEps.description}</div><br/>
													
													<c:if test="${novalnetEps.novalnetTestMode == true}">
																																							<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													
											</div>
										</c:if>
										<c:if test="${novalnetInstantBankTransfer.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetInstantBankTransfer" value="novalnetInstantBankTransfer" label="${novalnetInstantBankTransfer.name}"/>
													&nbsp;&nbsp;
													
													
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetInstantBankTransfer.png" />
													
													
													
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetInstantBankTransferPaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetInstantBankTransfer.description}</div><br/>
													
													<c:if test="${novalnetInstantBankTransfer.novalnetTestMode == true}">
																																							<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													
											</div>
										</c:if>
										<c:if test="${novalnetPostFinance.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetPostFinance" value="novalnetPostFinance" label="${novalnetPostFinance.name}"/>
													&nbsp;&nbsp;
													
													
														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetPostFinance.png" />
													
												
													
													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetPostFinancePaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetPostFinance.description}</div><br/>
													
													<c:if test="${novalnetPostFinance.novalnetTestMode == true}">
																																							<div color="red" id= "testModeText">
															test mode
														</div>

													</c:if>
													
											</div>
										</c:if>
										<c:if test="${novalnetPostFinanceCard.active == true}">
											<div class="novalnet-select-payment">
													<form:radiobutton path="selectedPaymentMethodId" id="novalnetPostFinanceCard" value="novalnetPostFinanceCard" label="${novalnetPostFinanceCard.name}"/>
													&nbsp;&nbsp;

														<img src="${contextPath}/_ui/addons/novalnetcheckoutaddon/responsive/common/images/novalnetPostFinanceCard.png" />

													</a>&nbsp;&nbsp;
											</div>
											<div id="novalnetPostFinanceCardPaymentForm" style="display:none;" class="novalnetPaymentForm">
													<div class="description">${novalnetPostFinanceCard.description}</div><br/>
													
														<c:if test="${novalnetPostFinanceCard.novalnetTestMode == true}">
																																								<div color="red" id= "testModeText">
																test mode
															</div>

													</c:if>
											</div>
										</c:if>
										</div>
										</c:if>
										
											<p>
											<spring:theme code="checkout.multi.paymentMethod.seeOrderSummaryForMoreInformation"/></p>
										</div>
											
										</form:form>
										<button type="button"
												class="btn btn-primary btn-block submit_novalnetPaymentDetailsForm checkout-next" id = "submit_novalnetPaymentDetailsForm">
												<spring:theme code="checkout.multi.paymentMethod.continue"/>
										</button> 
                                       
                                    </ycommerce:testId>
                                    
                            </div>
                        </div>

                        <c:if test="${not empty paymentInfos}">
                            <div id="savedpayments">
                                <div id="savedpaymentstitle">
                                    <div class="headline">
                                        <span class="headline-text"><spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.useSavedCard"/></span>
                                    </div>
                                </div>
                                <div id="savedpaymentsbody">
                                    <spring:url var="choosePaymentMethod" value="{contextPath}/checkout/multi/payment-method/choose" htmlEscape="false">
                                        <spring:param name="contextPath" value="${request.contextPath}" />
                                    </spring:url>
                                    <c:forEach items="${paymentInfos}" var="paymentInfo" varStatus="status">
                                        <form action="${fn:escapeXml(choosePaymentMethod)}" method="GET">
                                            <input type="hidden" name="selectedPaymentMethodId" value="${fn:escapeXml(paymentInfo.id)}"/>
                                                    <strong>${fn:escapeXml(paymentInfo.billingAddress.firstName)}&nbsp; ${fn:escapeXml(paymentInfo.billingAddress.lastName)}</strong><br/>
                                                    ${fn:escapeXml(paymentInfo.cardType)}<br/>
                                                    ${fn:escapeXml(paymentInfo.accountHolderName)}<br/>
                                                    ${fn:escapeXml(paymentInfo.cardNumber)}<br/>
                                                    <spring:theme code="checkout.multi.paymentMethod.paymentDetails.expires" arguments="${paymentInfo.expiryMonth},${paymentInfo.expiryYear}"/><br/>
                                                    ${fn:escapeXml(paymentInfo.billingAddress.line1)}<br/>
                                                    ${fn:escapeXml(paymentInfo.billingAddress.town)}&nbsp; ${fn:escapeXml(paymentInfo.billingAddress.region.isocodeShort)}<br/>
                                                    ${fn:escapeXml(paymentInfo.billingAddress.postalCode)}&nbsp; ${fn:escapeXml(paymentInfo.billingAddress.country.isocode)}<br/>
                                                <button type="submit" class="btn btn-primary btn-block" tabindex="${(status.count * 2) - 1}"><spring:theme code="checkout.multi.paymentMethod.addPaymentDetails.useThesePaymentDetails"/></button>
                                                
                                                <button type="button"
                                class="btn btn-primary btn-block submit_silentOrderPostForm checkout-next">
                            <spring:theme code="checkout.multi.paymentMethod.continue"/>
                        </button> 
                                        </form>
                                    </c:forEach>
                                </div>
                            </div>
                        </c:if>
                        
                    </ycommerce:testId>
               </jsp:body>

            </multiCheckout:checkoutSteps>
		</div>

        <div class="col-sm-6 hidden-xs">
            <multiCheckout:checkoutOrderDetails cartData="${cartData}" showDeliveryAddress="true" showPaymentInfo="false" showTaxEstimate="false" showTax="true" />
        </div>

		<div class="col-sm-12 col-lg-12">
			<cms:pageSlot position="SideContent" var="feature" element="div" class="checkout-help">
				<cms:component component="${feature}"/>
			</cms:pageSlot>
		</div>
	</div>

</template:page>


