/*
 *
 * @author    Novalnet AG
 * @copyright Copyright by Novalnet
 * @license   https://www.novalnet.de/payment-plugins/kostenlos/lizenz
 *
 * If you have found this script useful a small
 * recommendation as well as a comment on merchant form
 * would be greatly appreciated.
 *
 */
package novalnet.novalnetcheckoutaddon.controllers.integration;

import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;
import java.util.List;
import java.util.Date;
import java.util.regex.*;
import java.text.DecimalFormat;
import java.math.*;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

import org.json.JSONObject;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.Set;
import java.security.MessageDigest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import org.springframework.web.method.HandlerMethod;

import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.addonsupport.interceptors.BeforeControllerHandlerAdaptee;

import javax.annotation.Resource;

import de.hybris.platform.util.Config;

import org.apache.log4j.Logger;

import novalnet.novalnetcheckoutaddon.facades.NovalnetFacade;
import de.hybris.novalnet.core.model.NovalnetCallbackInfoModel;
import de.hybris.novalnet.core.model.NovalnetPaymentInfoModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.servicelayer.user.UserService;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Callback controller
 */
public class NovalnetCallbackHandler implements BeforeControllerHandlerAdaptee {
    private static final Logger LOG = Logger.getLogger(NovalnetCallbackHandler.class);

    private boolean testMode = false;

    @Resource(name = "novalnetFacade")
    NovalnetFacade novalnetFacade;

    /**
     * Handle callback process
     *
     * @param request  request
     * @param response response
     * @param handler  handler
     * @return Boolean
     */
    public boolean beforeController(HttpServletRequest request, HttpServletResponse response, HandlerMethod handler) throws Exception {

        // Check for callback URL
        if (request.getRequestURL().toString().contains("/novalnet/callback")) {
			
			response.setStatus(HttpServletResponse.SC_OK);
			final BaseStoreModel baseStore = novalnetFacade.getBaseStoreModel();

            // NOVALNET IP ADDRESS FROM HOST
            String vendorScriptHostIpAddress = "";
            try {
                InetAddress address = InetAddress.getByName("pay-nn.de"); //Novalnet vendor script host
                vendorScriptHostIpAddress = address.getHostAddress();
            } catch (UnknownHostException e) {
                vendorScriptHostIpAddress = "";
            }
            if ("" == vendorScriptHostIpAddress) {
                LOG.info("Novalnet HOST IP missing");
                return false;
            }
            
            // Get remote IP address
            String callerIp = request.getHeader("HTTP_X_FORWARDED_FOR");

            if (callerIp == null || callerIp.split("[.]").length != 4) {
                callerIp = request.getRemoteAddr();
            }
            testMode = baseStore.getNovalnetVendorscriptTestMode();

            // Check for IP and testmode validation
            if (vendorScriptHostIpAddress != callerIp && !testMode) {
                LOG.info("Novalnet callback received. Unauthorised access from the IP " + callerIp);
                return false;
            }

            // Set response status as OK
            
            Map<String, String> captureParams = getRequestParameterMap(request);
            Set<String> keys = captureParams.keySet();
            StringBuffer jsonParams = new StringBuffer();
			for(String key: keys){
				if(!key.equals("site")) {
					jsonParams.append(key);
				}
			}
			
			if(jsonParams.length() == 0) {
				LOG.info("Required params are missing");
                return false;
			}
			
			JSONObject tomJsonObject = new JSONObject(jsonParams.toString());
			JSONObject resultJsonObject = tomJsonObject.getJSONObject("result");
			JSONObject eventJsonObject = tomJsonObject.getJSONObject("event");
			JSONObject transactionJsonObject = tomJsonObject.getJSONObject("transaction");
			JSONObject merchantJsonObject = tomJsonObject.getJSONObject("merchant");
			JSONObject customerJsonObject = tomJsonObject.getJSONObject("customer");
			JSONObject customJsonObject = tomJsonObject.getJSONObject("custom");
			
			String tokenString = eventJsonObject.get("tid").toString() + eventJsonObject.get("type").toString() + resultJsonObject.get("status").toString();

			if ( !transactionJsonObject.get("amount").toString().equals("") ) {
				tokenString += transactionJsonObject.get("amount").toString();
			}
			if ( !transactionJsonObject.get("currency").toString().equals("") ) {
				tokenString += transactionJsonObject.get("currency").toString();
			}
			if (!baseStore.getNovalnetPaymentAccessKey().equals("")) {
				tokenString += new StringBuilder(baseStore.getNovalnetPaymentAccessKey().trim()).reverse().toString();
			}

			String createdHash = "";
			try{
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(tokenString.getBytes("UTF-8"));
				StringBuffer hexString = new StringBuffer();

					for (int i = 0; i < hash.length; i++) {
						String hex = Integer.toHexString(0xff & hash[i]);
						if(hex.length() == 1) hexString.append('0');
						hexString.append(hex);
					}
				
				createdHash =  hexString.toString();
				} catch(Exception ex) {
					throw new RuntimeException(ex);
				}

			if ( !eventJsonObject.get("checksum").toString().equals(createdHash) ) {
				 displayMessage( "While notifying some data has been changed. The hash check failed");
				 return false;
			}


            // Get email address for callback notification
            String toEmailAddress = baseStore.getNovalnetVendorscriptToEmailAddress();
            
            Map<String, String> capturePayments = new HashMap<String, String>();
            capturePayments.put("CREDITCARD", "CREDITCARD");
            capturePayments.put("INVOICE", "INVOICE");
            capturePayments.put("GUARANTEED_INVOICE", "GUARANTEED_INVOICE");
            capturePayments.put("DIRECT_DEBIT_SEPA", "DIRECT_DEBIT_SEPA");
            capturePayments.put("GUARANTEED_DIRECT_DEBIT_SEPA", "GUARANTEED_DIRECT_DEBIT_SEPA");
            capturePayments.put("PAYPAL", "PAYPAL");
            
            Map<String, String> cancelPayments = new HashMap<String, String>();
            cancelPayments.put("CREDITCARD", "CREDITCARD");
            cancelPayments.put("INVOICE", "INVOICE");
            cancelPayments.put("GUARANTEED_INVOICE", "GUARANTEED_INVOICE");
            cancelPayments.put("DIRECT_DEBIT_SEPA", "DIRECT_DEBIT_SEPA");
            cancelPayments.put("GUARANTEED_DIRECT_DEBIT_SEPA", "GUARANTEED_DIRECT_DEBIT_SEPA");
            cancelPayments.put("PAYPAL", "PAYPAL");
            
            Map<String, String> updatePayments = new HashMap<String, String>();
            updatePayments.put("CREDITCARD", "CREDITCARD");
            updatePayments.put("INVOICE_START", "INVOICE_START");
            updatePayments.put("PREPAYMENT", "PREPAYMENT");
            updatePayments.put("GUARANTEED_INVOICE", "GUARANTEED_INVOICE");
            updatePayments.put("DIRECT_DEBIT_SEPA", "DIRECT_DEBIT_SEPA");
            updatePayments.put("GUARANTEED_DIRECT_DEBIT_SEPA", "GUARANTEED_DIRECT_DEBIT_SEPA");
            updatePayments.put("PAYPAL", "PAYPAL");
            updatePayments.put("PRZELEWY24", "PRZELEWY24");
            updatePayments.put("CASHPAYMENT", "CASHPAYMENT");
            updatePayments.put("POSTFINANCE", "POSTFINANCE");
            
            Map<String, String> refundPayments = new HashMap<String, String>();
            refundPayments.put("CREDITCARD_BOOKBACK", "CREDITCARD_BOOKBACK");
            refundPayments.put("REFUND_BY_BANK_TRANSFER_EU", "REFUND_BY_BANK_TRANSFER_EU");
            refundPayments.put("PAYPAL_BOOKBACK", "PAYPAL_BOOKBACK");
            refundPayments.put("PRZELEWY24_REFUND", "PRZELEWY24_REFUND");
            refundPayments.put("CASHPAYMENT_REFUND", "CASHPAYMENT_REFUND");
            refundPayments.put("POSTFINANCE_REFUND", "POSTFINANCE_REFUND");
            refundPayments.put("GUARANTEED_INVOICE_BOOKBACK", "GUARANTEED_INVOICE_BOOKBACK");
            refundPayments.put("GUARANTEED_SEPA_BOOKBACK", "GUARANTEED_SEPA_BOOKBACK");
            refundPayments.put("POSTFINANCE_REFUND", "POSTFINANCE_REFUND");
            
            Map<String, String> creditPayments = new HashMap<String, String>();
            creditPayments.put("INVOICE_CREDIT", "INVOICE_CREDIT");
            creditPayments.put("CREDIT_ENTRY_CREDITCARD", "CREDIT_ENTRY_CREDITCARD");
            creditPayments.put("CREDIT_ENTRY_SEPA", "CREDIT_ENTRY_SEPA");
            creditPayments.put("DEBT_COLLECTION_SEPA", "DEBT_COLLECTION_SEPA");
            creditPayments.put("DEBT_COLLECTION_CREDITCARD", "DEBT_COLLECTION_CREDITCARD");
            creditPayments.put("GUARANTEED_DEBT_COLLECTION", "GUARANTEED_DEBT_COLLECTION");
            creditPayments.put("CASHPAYMENT_CREDIT", "CASHPAYMENT_CREDIT");
            creditPayments.put("ONLINE_TRANSFER_CREDIT", "ONLINE_TRANSFER_CREDIT");
            

            // Type of PAYMENTS available
            Map<String, String> initialPayments = new HashMap<String, String>();
            initialPayments.put("CREDITCARD", "CREDITCARD");
            initialPayments.put("INVOICE_START", "INVOICE_START");
            initialPayments.put("GUARANTEED_INVOICE", "GUARANTEED_INVOICE");
            initialPayments.put("DIRECT_DEBIT_SEPA", "DIRECT_DEBIT_SEPA");
            initialPayments.put("GUARANTEED_DIRECT_DEBIT_SEPA", "GUARANTEED_DIRECT_DEBIT_SEPA");
            initialPayments.put("GUARANTEED_INSTALLMENT_PAYMENT", "GUARANTEED_INSTALLMENT_PAYMENT");
            initialPayments.put("PAYPAL", "PAYPAL");
            initialPayments.put("ONLINE_TRANSFER", "ONLINE_TRANSFER");
            initialPayments.put("IDEAL", "IDEAL");
            initialPayments.put("EPS", "EPS");
            initialPayments.put("PAYSAFECARD", "PAYSAFECARD");
            initialPayments.put("GIROPAY", "GIROPAY");
            initialPayments.put("PRZELEWY24", "PRZELEWY24");
            initialPayments.put("CASHPAYMENT", "CASHPAYMENT");
            initialPayments.put("POSTFINANCE", "POSTFINANCE");
            initialPayments.put("POSTFINANCE_CARD", "POSTFINANCE_CARD");

            // Type of CHARGEBACKS available
            Map<String, String> chargebackPayments = new HashMap<String, String>();
            chargebackPayments.put("RETURN_DEBIT_SEPA", "RETURN_DEBIT_SEPA");
            chargebackPayments.put("REVERSAL", "REVERSAL");
            chargebackPayments.put("CREDITCARD_CHARGEBACK", "CREDITCARD_CHARGEBACK");         
            chargebackPayments.put("PAYPAL_CHARGEBACK", "PAYPAL_CHARGEBACK");
            

            // Type of CREDIT_ENTRY PAYMENT/COLLECTIONS available
            Map<String, String> collectionPayments = new HashMap<String, String>();
            collectionPayments.put("INVOICE_CREDIT", "INVOICE_CREDIT");
            collectionPayments.put("CREDIT_ENTRY_CREDITCARD", "CREDIT_ENTRY_CREDITCARD");
            collectionPayments.put("CREDIT_ENTRY_SEPA", "CREDIT_ENTRY_SEPA");
            collectionPayments.put("GUARANTEED_CREDIT_ENTRY_SEPA", "GUARANTEED_CREDIT_ENTRY_SEPA");
            collectionPayments.put("DEBT_COLLECTION_SEPA", "DEBT_COLLECTION_SEPA");
            collectionPayments.put("DEBT_COLLECTION_CREDITCARD", "DEBT_COLLECTION_CREDITCARD");
            collectionPayments.put("GUARANTEED_DEBT_COLLECTION", "GUARANTEED_DEBT_COLLECTION");
            collectionPayments.put("CASHPAYMENT_CREDIT", "CASHPAYMENT_CREDIT");
            collectionPayments.put("CREDIT_ENTRY_DE", "CREDIT_ENTRY_DE");
            collectionPayments.put("DEBT_COLLECTION_DE", "DEBT_COLLECTION_DE");

            // Payment types for each payment method
            Map<String, String[]> paymentTypes = new HashMap<String, String[]>();
            String[] creditCardPaymentTypes = {"CREDITCARD", "CREDITCARD_CHARGEBACK", "CREDITCARD_BOOKBACK", "TRANSACTION_CANCELLATION", "CREDIT_ENTRY_CREDITCARD", "DEBT_COLLECTION_CREDITCARD"};
            String[] directDebitSepaPaymentTypes = {"DIRECT_DEBIT_SEPA", "RETURN_DEBIT_SEPA", "REFUND_BY_BANK_TRANSFER_EU", "TRANSACTION_CANCELLATION", "CREDIT_ENTRY_SEPA", "DEBT_COLLECTION_SEPA"};
            String[] invoicePaymentTypes = {"INVOICE_START", "INVOICE_CREDIT", "TRANSACTION_CANCELLATION", "REFUND_BY_BANK_TRANSFER_EU", "CREDIT_ENTRY_DE", "DEBT_COLLECTION_DE"};
            String[] prepaymentPaymentTypes = {"INVOICE_START", "INVOICE_CREDIT", "REFUND_BY_BANK_TRANSFER_EU", "CREDIT_ENTRY_DE", "DEBT_COLLECTION_DE"};
            String[] payPalPaymentTypes = {"PAYPAL", "PAYPAL_BOOKBACK", "REFUND_BY_BANK_TRANSFER_EU"};
            String[] instantBankTransferPaymentTypes = {"ONLINE_TRANSFER", "REFUND_BY_BANK_TRANSFER_EU", "CREDIT_ENTRY_DE", "REVERSAL", "DEBT_COLLECTION_DE", "ONLINE_TRANSFER_CREDIT"};
            String[] idealPaymentTypes = {"IDEAL", "REFUND_BY_BANK_TRANSFER_EU", "CREDIT_ENTRY_DE", "REVERSAL", "DEBT_COLLECTION_DE", "ONLINE_TRANSFER_CREDIT"};
            String[] epsPaymentTypes = {"EPS", "REFUND_BY_BANK_TRANSFER_EU", "CREDIT_ENTRY_DE", "REVERSAL", "DEBT_COLLECTION_DE", "ONLINE_TRANSFER_CREDIT"};
            String[] giropayPaymentTypes = {"GIROPAY", "REFUND_BY_BANK_TRANSFER_EU", "CREDIT_ENTRY_DE", "REVERSAL", "DEBT_COLLECTION_DE", "ONLINE_TRANSFER_CREDIT"};
            String[] przelewy24PaymentTypes = {"PRZELEWY24", "PRZELEWY24_REFUND"};
            String[] cashpaymentPaymentTypes = {"CASHPAYMENT", "CASHPAYMENT_REFUND", "CASHPAYMENT_CREDIT"};
            String[] postFinancePaymentTypes = {"POSTFINANCE", "POSTFINANCE_REFUND"};
            String[] postFinanceCardPaymentTypes = {"POSTFINANCE_CARD", "POSTFINANCE_REFUND"};
            String[] guaranteedInvoicePaymentTypes = {"GUARANTEED_INVOICE", "GUARANTEED_INVOICE_BOOKBACK"};
            String[] guaranteedDirectDebitSepaPaymentTypes = {"GUARANTEED_DIRECT_DEBIT_SEPA", "GUARANTEED_SEPA_BOOKBACK"};

            paymentTypes.put("novalnetCreditCard", creditCardPaymentTypes);
            paymentTypes.put("novalnetDirectDebitSepa", directDebitSepaPaymentTypes);
            paymentTypes.put("novalnetInvoice", invoicePaymentTypes);
            paymentTypes.put("novalnetPrepayment", prepaymentPaymentTypes);
            paymentTypes.put("novalnetPayPal", payPalPaymentTypes);
            paymentTypes.put("novalnetInstantBankTransfer", instantBankTransferPaymentTypes);
            paymentTypes.put("novalnetIdeal", idealPaymentTypes);
            paymentTypes.put("novalnetEps", epsPaymentTypes);
            paymentTypes.put("novalnetGiropay", giropayPaymentTypes);
            paymentTypes.put("novalnetPrzelewy24", przelewy24PaymentTypes);
            paymentTypes.put("novalnetBarzahlen", cashpaymentPaymentTypes);
            paymentTypes.put("novalnetPostFinance", postFinancePaymentTypes);
            paymentTypes.put("novalnetPostFinanceCard", postFinanceCardPaymentTypes);
            paymentTypes.put("novalnetGuaranteedDirectDebitSepa", guaranteedDirectDebitSepaPaymentTypes);
            paymentTypes.put("novalnetGuaranteedInvoice", guaranteedInvoicePaymentTypes);


            Map<String, String> capturedRequiredParams = new HashMap<String, String>();
            capturedRequiredParams.put("vendor", merchantJsonObject.get("vendor").toString());
            capturedRequiredParams.put("payment_type", transactionJsonObject.get("payment_type").toString());
            capturedRequiredParams.put("tid", transactionJsonObject.get("tid").toString());
            capturedRequiredParams.put("status", transactionJsonObject.get("status").toString());

            // Required parameters
            String[] requiredParams = {"vendor", "payment_type", "tid", "status"};
            int requiredParamsLength = requiredParams.length;
            for (int i = 0; i < requiredParamsLength; i++) {
                if(capturedRequiredParams.get(requiredParams[i]).equals("") || capturedRequiredParams.get(requiredParams[i]) == null){
                    displayMessage(requiredParams[i] + " parameter missing");
                    return false;
                } else if (!transactionJsonObject.get("tid").toString().equals("") && transactionJsonObject.get("tid").toString().length() != 17) {
                    displayMessage("Novalnet callback received. TID not valid");
                    return false;
                }
            }
			String requestEventype = eventJsonObject.get("type").toString();
			JSONObject refundRequestPaymentType = new JSONObject();
            String requestPaymentType = transactionJsonObject.get("payment_type").toString();
            if(requestEventype.equals("TRANSACTION_REFUND")) {
				 refundRequestPaymentType = transactionJsonObject.getJSONObject("refund");
				 requestPaymentType = refundRequestPaymentType.get("payment_type").toString();
			}
            
            
            if ((requestEventype.equals("TRANSACTION_CAPTURE") && !capturePayments.containsValue(requestPaymentType)) || (requestEventype.equals("TRANSACTION_UPDATE") && !updatePayments.containsValue(requestPaymentType)) || (requestEventype.equals("TRANSACTION_REFUND") && !refundPayments.containsValue(refundRequestPaymentType.get("payment_type").toString())) || (requestEventype.equals("CREDIT") && !creditPayments.containsValue(requestPaymentType)) || (requestEventype.equals("CHARGEBACK") && !chargebackPayments.containsValue(requestPaymentType)) || (requestEventype.equals("TRANSACTION_CANCEL") && !cancelPayments.containsValue(requestPaymentType)) ) {
				displayMessage("Payment type " + requestPaymentType + " is not supported for event type " + requestEventype);
                return false;
			}

            // Validate TID paymentdescription
            if ((chargebackPayments.containsValue(requestPaymentType) || collectionPayments.containsValue(requestPaymentType) || chargebackPayments.containsValue(requestPaymentType)) && (eventJsonObject.get("parent_tid").toString().equals("") || eventJsonObject.get("parent_tid").toString().length() != 17)) {
                displayMessage("Novalnet callback received. TID not valid");
                return false;
            }
            
            

            String referenceTid = transactionJsonObject.get("tid").toString();
            String[] successStatus = {"CONFIRMED", "PENDING", "ON_HOLD"};
            if (!transactionJsonObject.get("status").toString().equals("") && Arrays.asList(successStatus).contains(transactionJsonObject.get("status").toString())) {

                if ((chargebackPayments.containsValue(requestPaymentType) || collectionPayments.containsValue(requestPaymentType)) || creditPayments.containsValue(requestPaymentType) || refundPayments.containsValue(requestPaymentType)) {
                    // Get reference TID
                    referenceTid = eventJsonObject.get("parent_tid").toString();
                }


                List<NovalnetCallbackInfoModel> orderReference = getOrderReference(referenceTid);
                final List<NovalnetPaymentInfoModel> paymentInfo = novalnetFacade.getNovalnetPaymentInfo(orderReference.get(0).getOrderNo());
                NovalnetPaymentInfoModel paymentInfoModel = novalnetFacade.getPaymentModel(paymentInfo);

                if (orderReference == null) {
                    // Check for order not found
                    displayMessage("Novalnet callback executed. Order reference not found");
                    return false;
                } else if (transactionJsonObject.get("order_no").toString() != null && !orderReference.get(0).getOrderNo().equals(transactionJsonObject.get("order_no").toString())) {
                    // Check for order number mismatch
                    displayMessage("Novalnet callback executed. Order no mismatched");
                    return false;
                }

                // Get transaction status
                String transactionStatus = transactionJsonObject.get("status").toString();

                // Get payment method of the order
                String paymentType = orderReference.get(0).getPaymentType();
                String[] suppotedCallbackPaymentTypes = paymentTypes.get(paymentType);

                // Validate order's payment method with requested payment type
                if (!Arrays.asList(suppotedCallbackPaymentTypes).contains(requestPaymentType)) {
                    displayMessage("Novalnet callback executed. Payment type mismatched");
                    return false;
                }

                // Format the order amount to Integer
                int amountInCents = Integer.parseInt(transactionJsonObject.get("amount").toString());

                long amountToBeFormat = Integer.parseInt(transactionJsonObject.get("amount").toString());

                // Format the order amount to currency format
                BigDecimal formattedAmount = new BigDecimal(amountToBeFormat).movePointLeft(2);

                // Get the callbackTid
                long callbackTid = Long.parseLong(transactionJsonObject.get("tid").toString());

                // Get customer paid amount
                int paidAmount = orderReference.get(0).getPaidAmount();

                // Get order amount
                int orderAmount = orderReference.get(0).getOrderAmount();

                // Get order number of the order
                String orderNo = orderReference.get(0).getOrderNo();

                // Check for valid status
                boolean validStatus = transactionJsonObject.get("status").toString().equals("COMPLETED");
                String callbackComments = "";

                // Total Paid Amount
                int totalAmount = paidAmount + amountInCents;

                // Instantiate a Date object
                Date currentDate = new Date();
                String[] refundType = {"CHARGEBACK", "TRANSACTION_REFUND"};
                String[] pendingPaymentType = {"PAYPAL", "PRZELEWY24"};
                String[] processPaymentType = {"DIRECT_DEBIT_SEPA", "INVOICE_START", "GUARANTEED_DIRECT_DEBIT_SEPA", "GUARANTEED_INVOICE", "CREDITCARD", "PAYPAL"};
                String[] pendingStatus = {"ON_HOLD"};

                if (eventJsonObject.get("type").toString().equals("TRANSACTION_CANCEL")) {
                    callbackComments = "The transaction has been canceled on " + currentDate.toString();

                    novalnetFacade.updatePaymentInfo(paymentInfo, transactionJsonObject.get("status").toString());
                    novalnetFacade.updateCancelStatus(orderNo);
                    novalnetFacade.updateCallbackComments(callbackComments, orderNo, transactionStatus);
                    sendEmail(callbackComments, toEmailAddress);
                    return false;
                } else if (eventJsonObject.get("type").toString().equals("TRANSACTION_CAPTURE")) {
                    if (transactionJsonObject.get("status").toString().equals("CONFIRMED") && Arrays.asList(pendingStatus).contains(paymentInfo.get(0).getPaymentGatewayStatus().toString())) {
                        callbackComments = ((paymentInfo.get(0).getPaymentGatewayStatus().toString().equals("75")) && requestPaymentType.equals("GUARANTEED_INVOICE")) ? "Novalnet callback received. The transaction has been confirmed successfully for the TID:" + transactionJsonObject.get("tid").toString() + "and the due date updated as" + transactionJsonObject.get("due_date").toString() + "This is processed as a guarantee payment" : "Novalnet callback received. The transaction has been confirmed on" + currentDate.toString();
                        String[] invoicePaymentType = {"GUARANTEED_INVOICE", "INVOICE_START"};
                        
                        novalnetFacade.updatePaymentInfo(paymentInfo, transactionJsonObject.get("status").toString());
                        paymentInfoModel = novalnetFacade.getPaymentModel(paymentInfo);
                        novalnetFacade.updateOrderStatus(orderNo, paymentInfoModel);
                        novalnetFacade.updateCallbackComments(callbackComments, orderNo, transactionStatus);
                        sendEmail(callbackComments, toEmailAddress);
                        return false;
                    }
                } else if (eventJsonObject.get("type").toString().equals("TRANSACTION_UPDATE")) {
                    if (Arrays.asList(processPaymentType).contains(requestPaymentType) && paymentInfo.get(0).getPaymentGatewayStatus().toString().equals("PENDING")) {

                        if (transactionJsonObject.get("status").toString().equals("ON_HOLD")) {

                            callbackComments = "The transaction status has been changed from pending to on-hold for the TID:  " + transactionJsonObject.get("tid").toString() + " on " + currentDate.toString();
                            novalnetFacade.updateCallbackComments(callbackComments, orderNo, transactionStatus);
                            novalnetFacade.updatePaymentInfo(paymentInfo, transactionJsonObject.get("status").toString());
                            paymentInfoModel = novalnetFacade.getPaymentModel(paymentInfo);
							novalnetFacade.updateOrderStatus(orderNo, paymentInfoModel);
                            sendEmail(callbackComments, toEmailAddress);
                            return false;
                        } else if (transactionJsonObject.get("status").toString().equals("CONFIRMED")) {
                            callbackComments = ((paymentInfo.get(0).getPaymentGatewayStatus().toString().equals("75")) && requestPaymentType.equals("GUARANTEED_INVOICE")) ? "The transaction has been confirmed successfully for the TID: " + transactionJsonObject.get("tid").toString() + "and the due date updated as" + transactionJsonObject.get("due_date").toString() + "This is processed as a guarantee payment" : "The transaction has been confirmed on " + currentDate.toString();
                            String[] invoicePaymentType = {"GUARANTEED_INVOICE", "INVOICE_START"};
                            novalnetFacade.updatePaymentInfo(paymentInfo, transactionJsonObject.get("status").toString());
                            paymentInfoModel = novalnetFacade.getPaymentModel(paymentInfo);
							novalnetFacade.updateOrderStatus(orderNo, paymentInfoModel);
                            novalnetFacade.updateCallbackComments(callbackComments, orderNo, transactionStatus);
                            sendEmail(callbackComments, toEmailAddress);
                            return false;
                        }

                    }

                    if (Arrays.asList(pendingPaymentType).contains(requestPaymentType)) {   // IF PAYMENT MADE ON REAL TIME (NOT THROUGH SUBSCRIPTION RENEWAL)

                        if (orderAmount > paidAmount) {
                            String[] statusPending = {"PENDING"};
                            if (Arrays.asList(statusPending).contains(paymentInfo.get(0).getPaymentGatewayStatus().toString()) && transactionJsonObject.get("status").toString().equals("CONFIRMED")) {
                                callbackComments = "Novalnet Callback Script executed successfully for the TID: " + transactionJsonObject.get("tid").toString() + " with amount " + formattedAmount + " " + transactionJsonObject.get("currency").toString() + " on " + currentDate.toString();
                                novalnetFacade.updateOrderStatus(orderNo, paymentInfoModel);
                            } else {
                                String reasonText = !(resultJsonObject.get("status_desc").toString().equals("")) ? resultJsonObject.get("status_desc").toString() : (!(resultJsonObject.get("status_text").toString().equals("")) ? resultJsonObject.get("status_text").toString() : "Payment could not be completed");
                                callbackComments = "The transaction has been cancelled due to:" + reasonText;
                            }
                            // Update callback comments
                            novalnetFacade.updateCallbackComments(callbackComments, orderNo, transactionStatus);

                            // Update Order success status


                            // Update Callback info
                            novalnetFacade.updatePaymentInfo(paymentInfo, transactionJsonObject.get("status").toString());
                            novalnetFacade.updateCallbackInfo(callbackTid, orderReference, totalAmount);

                            // Send notification email
                            sendEmail(callbackComments, toEmailAddress);
                            return false;
                        }
                        displayMessage("Novalnet callback script executed. Not applicable for this process");
                        return false;
                    }

                }
                //Incoming of a chargeback
                else if (Arrays.asList(refundType).contains(eventJsonObject.get("type").toString())) {


                    // Form callback comments
                    String[] chargeBackPaymentType = {"CREDITCARD_CHARGEBACK", "PAYPAL_CHARGEBACK", "RETURN_DEBIT_SEPA", "REVERSAL"};
                    BigDecimal refundFormattedAmount = new BigDecimal(0);
                    
                    if(!Arrays.asList(chargeBackPaymentType).contains(requestPaymentType)) {
						long refundAmountToBeFormat = Integer.parseInt(refundRequestPaymentType.get("amount").toString());

					// Format the order amount to currency format
						refundFormattedAmount = new BigDecimal(refundAmountToBeFormat).movePointLeft(2);
					}
					
					if(Arrays.asList(chargeBackPaymentType).contains(requestPaymentType)) {
						callbackComments = "Chargeback executed successfully for the TID: " + eventJsonObject.get("parent_tid").toString() + " amount: " + formattedAmount + " " + transactionJsonObject.get("currency").toString() + " on " + currentDate.toString() + ". The subsequent TID: " + transactionJsonObject.get("tid").toString();
					} else if(requestPaymentType.equals("REVERSAL")) {
						callbackComments = "Chargeback executed for reversal of TID:" + eventJsonObject.get("parent_tid").toString() + " with the amount  " + formattedAmount + " " + transactionJsonObject.get("currency").toString() + " on " + currentDate.toString() + ". The subsequent TID: " + transactionJsonObject.get("tid").toString();
					} else if(requestPaymentType.equals("RETURN_DEBIT_SEPA")) {
						callbackComments = "Chargeback executed for return debit of TID:" + eventJsonObject.get("parent_tid").toString() + " with the amount  " + formattedAmount + " " + transactionJsonObject.get("currency").toString() + " on " + currentDate.toString() + ". The subsequent TID: " + transactionJsonObject.get("tid").toString();
					} else {
                
						callbackComments =  "Refund has been initiated for the TID " + eventJsonObject.get("parent_tid").toString() + " with the amount : " + formattedAmount + " " + transactionJsonObject.get("currency").toString() + ". New TID: " + transactionJsonObject.get("tid").toString() + "  for the refunded amount " + refundFormattedAmount ;
					}

                    // Update callback comments
                    novalnetFacade.updateCallbackComments(callbackComments, orderNo, transactionStatus);

                    // Send notification email
                    sendEmail(callbackComments, toEmailAddress);
                    return false;
                } else if (eventJsonObject.get("type").toString().equals("CREDIT")) { // Incoming of collection of a payment OR Bank transfer OR invoice OR Advance payment through Customer

                    String[] creditPayment = {"CREDIT_ENTRY_CREDITCARD", "CREDIT_ENTRY_SEPA", "DEBT_COLLECTION_SEPA", "DEBT_COLLECTION_CREDITCARD", "CREDIT_ENTRY_DE", "DEBT_COLLECTION_DE"};
                    String[] creditPaymentType = {"INVOICE_CREDIT", "CASHPAYMENT_CREDIT"};
                    if (Arrays.asList(creditPaymentType).contains(requestPaymentType)) {
                        // if settlement of invoice OR Advance payment through Customer
                        if (orderAmount > paidAmount) {
                            // Form callback comments
                            String notifyComments = callbackComments = "Novalnet Callback Script executed successfully for the TID: " + eventJsonObject.get("parent_tid").toString() + " with amount " + formattedAmount + " " + transactionJsonObject.get("currency").toString() + " on " + currentDate.toString() + ". Please refer PAID transaction in our Novalnet Merchant Administration with the TID: " + transactionJsonObject.get("tid").toString();

                            // Update PART PAID payment status
                            novalnetFacade.updatePartPaidStatus(orderNo);

                            // Update Callback info
                            novalnetFacade.updateCallbackInfo(callbackTid, orderReference, totalAmount);

                            // Full amount paid by the customer
                            if (totalAmount >= orderAmount) {
                                // Update Callback order status
                                novalnetFacade.updateCallbackOrderStatus(orderNo, paymentType);

                                // Customer paid greater than the order amount
                                if (totalAmount > orderAmount) {
                                    notifyComments += ". Customer paid amount is greater than order amount.";
                                }
                            }

                            // Update callback comments
                            novalnetFacade.updateCallbackComments(callbackComments, orderNo, transactionStatus);

                            // Send notification email
                            sendEmail(notifyComments, toEmailAddress);
                            return false;
                        }
                    } else if (Arrays.asList(creditPayment).contains(requestPaymentType)) {
                        callbackComments = "Novalnet Callback Script executed successfully for the TID: " + eventJsonObject.get("parent_tid").toString() + " with amount" + formattedAmount + " " + transactionJsonObject.get("currency").toString() + " on " + currentDate.toString() + ". Please refer PAID transaction in our Novalnet Merchant Administration with the TID:" + transactionJsonObject.get("tid").toString() + ".";
                        novalnetFacade.updateCallbackInfo(callbackTid, orderReference, totalAmount);
                        novalnetFacade.updateCallbackComments(callbackComments, orderNo, transactionStatus);

                        // Send notification email
                        sendEmail(callbackComments, toEmailAddress);
                        return false;
                    }
                    displayMessage("Novalnet callback script already executed.");
                    return false;
                } else if (requestPaymentType.equals("SUBSCRIPTION_STOP")) {

                    // UPDATE THE STATUS OF THE USER SUBSCRIPTION
                }
            }
            displayMessage("Novalnet callback script executed. Status not valid");
            return false;
        }
        return true;
    }

    /**
     * Get order reference
     *
     * @param referenceTid Reference TID
     * @return List<NovalnetCallbackInfoModel>
     */
    protected List<NovalnetCallbackInfoModel> getOrderReference(String referenceTid) {
        final List<NovalnetCallbackInfoModel> paymentInfo = novalnetFacade.getCallbackInfo(referenceTid);
        return paymentInfo;
    }

    public static Map<String, String> getRequestParameterMap(final HttpServletRequest request) {
        final Map<String, String> map = new HashMap<>();
        final Enumeration myEnum = request.getParameterNames();
        while (myEnum.hasMoreElements()) {
            final String paramName = (String) myEnum.nextElement();
            final String paramValue = request.getParameter(paramName);
            map.put(paramName, paramValue);
        }
        return map;
    }

    /**
     * Get Reference TID
     *
     * @param params request parameters
     * @return String
     */
    protected String getReferenceTid(Map<String, String> params) {
        String referenceTid = params.get("tid");

        if (params.get("tid_payment") != null && !params.get("tid_payment").equals("")) {
            referenceTid = params.get("tid_payment"); // if its a followup transaction and the original_tid is given, then define the original TID to get the initial order details
        } else if (params.get("signup_tid") != null && !params.get("signup_tid").equals("")) {
            referenceTid = params.get("signup_tid"); // if its a subscription and the signup_tid is given, then define the initial TID to get the initial order details
        }

        return referenceTid;
    }

    /**
     * Validate the captured parameters
     *
     * @param params       request parameters
     * @param referenceTid Reference TID
     * @return boolean
     */
    protected boolean validateCapturedParameters(Map<String, String> params, String referenceTid) {

        // Validate amount
        if (!params.get("amount").equals("SUBSCRIPTION_STOP") && params.get("amount").equals("") || params.get("amount") == null) {
            displayMessage("Novalnet callback received. Amount not valid");
            return false;
        }

        // Validate reference TID
        if (referenceTid.length() != 17) {
            displayMessage("Novalnet callback received. Reference TID not valid");
            return false;
        }
        return true;
    }

    /**
     * Send email notification
     *
     * @param callbackComments  Get the callback comments
     * @param toEmailAddress    Get TO email address
     * @param emailNotification Check for email notification
     */
    protected void sendEmail(String callbackComments, String toEmailAddress) {
        displayMessage(callbackComments);

        if (toEmailAddress != null) {
            // Sender's email ID needs to be mentioned
            String from = Config.getParameter("mail.from");

            // Assuming you are sending email from localhost
            String host = Config.getParameter("mail.smtp.server");

            // Get system properties
            Properties properties = System.getProperties();

            // Setup mail server
            if (host != null && from != null) {
                properties.setProperty("mail.smtp.host", host);


                // Get the default Session object.
                Session session = Session.getDefaultInstance(properties);

                try {
                    // Create a default MimeMessage object.
                    MimeMessage message = new MimeMessage(session);

                    // Set From: header field of the header.
                    message.setFrom(new InternetAddress(from));

                    // Set To: header field of the header.
                    message.addRecipient(Message.RecipientType.TO, new InternetAddress(toEmailAddress));

                    // Set Subject: header field
                    message.setSubject("Novalnet callback script notification");

                    // Set the message
                    message.setText(callbackComments);

                    // Send message
                    Transport.send(message);
                    displayMessage("Sent message successfully");
                } catch (MessagingException mex) {
                    mex.printStackTrace();
                }
            }
        }
    }

    /**
     * Display Message
     *
     * @param message The message of the process
     */
    protected void displayMessage(String message) {
        if (testMode) {
            LOG.info(message);
        }
    }
}
