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
package novalnet.novalnetcheckoutaddon.controllers.pages.checkout.steps;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Base64;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.util.localization.Localization;
import de.hybris.platform.core.enums.OrderStatus;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.order.PaymentModeService;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

import java.io.*;

import java.net.URLConnection;
import java.net.HttpURLConnection;
import java.io.ObjectOutputStream;
import java.net.URL;

import org.xml.sax.SAXException;

import java.net.MalformedURLException;

import java.nio.charset.StandardCharsets;

import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

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
import java.security.MessageDigest;
import novalnet.novalnetcheckoutaddon.facades.NovalnetFacade;
import de.hybris.novalnet.core.model.NovalnetPaymentInfoModel;
import de.hybris.platform.core.model.user.AddressModel;
import de.hybris.platform.core.model.order.CartModel;
import de.hybris.platform.core.model.c2l.CurrencyModel;
import de.hybris.platform.core.model.user.UserModel;
import de.hybris.platform.payment.model.PaymentTransactionModel;
import de.hybris.platform.order.CartService;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commerceservices.enums.CustomerType;
import de.hybris.platform.payment.enums.PaymentTransactionType;
import de.hybris.platform.servicelayer.dto.converter.Converter;
import de.hybris.platform.payment.model.PaymentTransactionEntryModel;

@Controller
@RequestMapping(value = "/")
public class NovalnetHopPaymentResponseController extends NovalnetPaymentMethodCheckoutStepController {

    @Resource
    private PaymentModeService paymentModeService;

    @Resource(name = "novalnetFacade")
    NovalnetFacade novalnetFacade;

    @Resource(name = "cartService")
    private CartService cartService;

    @Resource
    private Converter<AddressData, AddressModel> addressReverseConverter;

    protected static final String REDIRECT_URL_ORDER_CONFIRMATION = REDIRECT_PREFIX + "/checkout/novalnet/orderConfirmation/";

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.checkout.steps.HopPaymentResponseController#doHandleHopResponse
     * (javax.servlet.http.HttpServletRequest)
     */
    @RequestMapping(value = "checkout/multi/novalnet/hop-response", method = RequestMethod.POST)
    @RequireHardLogIn
    public String doHandleHopResponse(final HttpServletRequest request) {
        final OrderData orderData;
        try {
            orderData = getCheckoutFacade().placeOrder();
        } catch (final Exception e) {
            return getCheckoutStep().currentStep();
        }

        return confirmationPageURL(orderData);

    }

    @RequestMapping(value = "checkout/multi/novalnet/hop-response", method = RequestMethod.GET)
    @RequireHardLogIn
    public String handleHopResponse(final RedirectAttributes redirectAttributes, final HttpServletRequest request) {
		
        final Map<String, String> resultMap = getRequestParameterMap(request);

        final Map<String, Object> transactionParameters = new HashMap<String, Object>();
        final Map<String, Object> dataParameters = new HashMap<String, Object>();
        final Map<String, Object> customParameters = new HashMap<String, Object>();
        
        String txn_secret = getSessionService().getAttribute("txn_secret");
        
        String[] successStatus = {"CONFIRMED", "ON_HOLD", "PENDING"};
        
        if (! resultMap.get("checksum").toString().equals("") && ! resultMap.get("tid").toString().equals("") && !txn_secret.equals("") && ! resultMap.get("status").toString().equals("")) 
		{
		   String token_string = resultMap.get("tid").toString() + getSessionService().getAttribute("txn_secret") + resultMap.get("status").toString() + new StringBuilder( getSessionService().getAttribute("txn_check") ).reverse().toString();
		   		   
		   String generated_checksum = "";
			try{
				MessageDigest digest = MessageDigest.getInstance("SHA-256");
				byte[] hash = digest.digest(token_string.getBytes("UTF-8"));
				StringBuffer hexString = new StringBuffer();

					for (int i = 0; i < hash.length; i++) {
						String hex = Integer.toHexString(0xff & hash[i]);
						if(hex.length() == 1) hexString.append('0');
						hexString.append(hex);
					}
				
				generated_checksum =  hexString.toString();
				} catch(Exception ex) {
					throw new RuntimeException(ex);
				}

		   if (!generated_checksum.equals(resultMap.get("checksum").toString())) 
		   {
			  final String statusMessage = "While redirecting some data has been changed. The hash check failed";
			  getSessionService().setAttribute("novalnetCheckoutError", statusMessage );
			  return getCheckoutStep().currentStep();
		   } 
		   else
		   {
			    transactionParameters.put("tid", resultMap.get("tid"));
				customParameters.put("lang", "DE");

				dataParameters.put("transaction", transactionParameters);
				dataParameters.put("custom", customParameters);

				Gson gson = new GsonBuilder().create();
				String jsonString = gson.toJson(dataParameters);

				String url = "https://payport.novalnet.de/v2/transaction/details";
				StringBuffer response = novalnetFacade.sendRequest(url, jsonString);

				JSONObject tomJsonObject = new JSONObject(response.toString());
				JSONObject resultJsonObject = tomJsonObject.getJSONObject("result");
				JSONObject customerJsonObject = tomJsonObject.getJSONObject("customer");
				JSONObject transactionJsonObject = tomJsonObject.getJSONObject("transaction");
				String currentPayment = getSessionService().getAttribute("selectedPaymentMethodId");
					
				if (Arrays.asList(successStatus).contains(transactionJsonObject.get("status").toString())) {

					final CartModel cartModel = novalnetFacade.getNovalnetCheckoutCart();
					String orderComments = "Novalnet transaction id : " + transactionJsonObject.get("tid");
					AddressData addressData = getSessionService().getAttribute("novalnetAddressData");

					AddressModel billingAddress = novalnetFacade.getBillingAddress();
					billingAddress = addressReverseConverter.convert(addressData, billingAddress);
					billingAddress.setEmail(customerJsonObject.getString("email"));
					billingAddress.setOwner(cartModel);

					final UserModel currentUser = novalnetFacade.getCurrentUser();
					NovalnetPaymentInfoModel paymentInfoModel = new NovalnetPaymentInfoModel();
					paymentInfoModel.setBillingAddress(billingAddress);
					paymentInfoModel.setPaymentEmailAddress(customerJsonObject.getString("email"));
					paymentInfoModel.setDuplicate(Boolean.FALSE);
					paymentInfoModel.setSaved(Boolean.TRUE);
					paymentInfoModel.setUser(currentUser);
					paymentInfoModel.setPaymentInfo(orderComments);
					paymentInfoModel.setOrderHistoryNotes(orderComments);
					paymentInfoModel.setPaymentProvider(currentPayment);
					paymentInfoModel.setCode("");
					paymentInfoModel.setPaymentGatewayStatus(transactionJsonObject.get("status").toString());

					PaymentTransactionEntryModel orderTransactionEntry = null;
					int orderAmountCent = Integer.parseInt(transactionJsonObject.get("amount").toString());

					final List<PaymentTransactionEntryModel> paymentTransactionEntries = new ArrayList<>();
					orderTransactionEntry = novalnetFacade.createTransactionEntry(transactionJsonObject.get("tid").toString(), cartModel, orderAmountCent, orderComments, transactionJsonObject.getString("currency"));
					paymentTransactionEntries.add(orderTransactionEntry);

					// Initiate/ Update PaymentTransactionModel
					PaymentTransactionModel paymentTransactionModel = new PaymentTransactionModel();
					paymentTransactionModel.setPaymentProvider(currentPayment);
					paymentTransactionModel.setRequestId(transactionJsonObject.get("tid").toString());
					paymentTransactionModel.setEntries(paymentTransactionEntries);
					paymentTransactionModel.setOrder(cartModel);
					paymentTransactionModel.setInfo(paymentInfoModel);

					// Update the OrderModel
					cartModel.setPaymentTransactions(Arrays.asList(paymentTransactionModel));
					cartModel.setPaymentInfo(paymentInfoModel);
					final OrderData orderData;

					try {
						orderData = getCheckoutFacade().placeOrder();
					} catch (final Exception e) {
						return getCheckoutStep().currentStep();
					}

					transactionParameters.clear();
					dataParameters.clear();

					transactionParameters.put("tid", resultMap.get("tid"));
					transactionParameters.put("order_no", orderData.getCode());

					dataParameters.put("transaction", transactionParameters);
					dataParameters.put("custom", customParameters);

					jsonString = gson.toJson(dataParameters);

					url = "https://payport.novalnet.de/v2/transaction/update";
					StringBuffer responseString = novalnetFacade.sendRequest(url, jsonString);
					
					if (currentPayment.equals("novalnetPayPal")) {

						boolean novalnetPayPalStorePaymentData = getSessionService().getAttribute("novalnetPayPalStorePaymentData");

						if (novalnetPayPalStorePaymentData == true) {
							novalnetFacade.handleReferenceTransactionInfo(response, customerJsonObject.get("customer_no").toString(), "novalnetPayPal");
						}
					} else if (currentPayment.equals("novalnetCreditCard") && !novalnetFacade.isGuestUser()) {
						boolean novalnetCreditCardStorePaymentData = getSessionService().getAttribute("novalnetCreditCardStorePaymentData");
						
						if(novalnetCreditCardStorePaymentData == true) {
							novalnetFacade.handleReferenceTransactionInfo(response, customerJsonObject.get("customer_no").toString(), "novalnetCreditCard");
						}
					}
					
					getSessionService().setAttribute("tid", orderComments);

					novalnetFacade.updateOrderStatus(orderData.getCode(), paymentInfoModel);
					novalnetFacade.saveOrderData(orderData.getCode(), orderComments, currentPayment, transactionJsonObject.get("status").toString(), orderAmountCent, transactionJsonObject.getString("currency"), transactionJsonObject.get("tid").toString(), customerJsonObject.getString("email"), addressData, cartModel, paymentInfoModel, billingAddress);

					return confirmationPageURL(orderData);
				
				}
				else
				{
					// Unset the stored novalnet session
					getSessionService().setAttribute("novalnetOrderCurrency", null);
					getSessionService().setAttribute("novalnetOrderAmount", null);
					getSessionService().setAttribute("novalnetCustomerParams", null);
					getSessionService().setAttribute("novalnetRedirectPaymentTestModeValue", null);
					getSessionService().setAttribute("novalnetRedirectPaymentName", null);
					getSessionService().setAttribute("novalnetCreditCardPanHash", null);
					getSessionService().setAttribute("paymentAccessKey", null);
						
					final String statusMessage = resultJsonObject.get("status_text").toString() != null ? resultJsonObject.get("status_text").toString() : resultMap.get("status_desc").toString();
					getSessionService().setAttribute("novalnetCheckoutError", statusMessage );
					return getCheckoutStep().currentStep();
				}
		   }            
		} 
		else
		{
			  final String statusMessage = "While redirecting some data has been changed. The hash check failed";
			  getSessionService().setAttribute("novalnetCheckoutError", statusMessage );
			  return getCheckoutStep().currentStep();
		}

    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.AbstractCheckoutController#redirectToOrderConfirmationPage
     * (javax.servlet.http.HttpServletRequest)
     */
    protected String confirmationPageURL(final OrderData orderData) {
        return REDIRECT_URL_ORDER_CONFIRMATION
                + (getCheckoutCustomerStrategy().isAnonymousCheckout() ? orderData.getGuid() : orderData.getCode());
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.addons.novalnetcheckoutaddon.controllers.pages.NovalnetCallbackHandler#beforeController
     * (javax.servlet.http.HttpServletRequest)
     */
    @RequestMapping(value = "novalnet/callback", method = RequestMethod.POST)
    public void doHandle(final HttpServletRequest request) {
    }// Handle callback request in handler adaptee
}
