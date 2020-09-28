package novalnet.novalnetcheckoutaddon.controllers.pages.checkout.steps;

import de.hybris.platform.acceleratorservices.enums.CheckoutPciOptionEnum;
import de.hybris.platform.acceleratorservices.payment.constants.PaymentConstants;
import de.hybris.platform.acceleratorservices.payment.data.PaymentData;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.PreValidateQuoteCheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.checkout.steps.CheckoutStep;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.checkout.steps.AbstractCheckoutStepController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.PaymentDetailsForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.SopPaymentDetailsForm;
import de.hybris.platform.acceleratorstorefrontcommons.util.AddressDataUtil;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.store.BaseStoreModel;
import de.hybris.platform.store.services.BaseStoreService;
import de.hybris.platform.commercefacades.order.data.CCPaymentInfoData;
import de.hybris.platform.commercefacades.order.data.CardTypeData;
import de.hybris.platform.commercefacades.order.data.CartData;
import de.hybris.platform.commercefacades.user.data.AddressData;
import de.hybris.platform.commercefacades.user.data.CountryData;
import de.hybris.platform.commerceservices.enums.CountryType;
import de.hybris.platform.yacceleratorstorefront.controllers.ControllerConstants;
import novalnet.novalnetcheckoutaddon.forms.NovalnetPaymentDetailsForm;
import novalnet.novalnetcheckoutaddon.facades.NovalnetFacade;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hybris.platform.jalo.JaloSession;
import de.hybris.platform.util.localization.Localization;

import java.util.Locale;

import javax.annotation.Resource;
import javax.validation.Valid;

import de.hybris.platform.order.PaymentModeService;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import novalnet.novalnetcheckoutaddon.constants.NovalnetcheckoutaddonWebConstants;
import novalnet.novalnetcheckoutaddon.controllers.NovalnetcheckoutaddonControllerConstants;
import de.hybris.novalnet.core.model.NovalnetDirectDebitSepaPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetGuaranteedDirectDebitSepaPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetPayPalPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetCreditCardPaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetInvoicePaymentModeModel;
import de.hybris.novalnet.core.model.NovalnetGuaranteedInvoicePaymentModeModel;
import de.hybris.platform.core.model.order.payment.PaymentModeModel;
import novalnet.novalnetcheckoutaddon.forms.NovalnetPaymentInfoData;
import de.hybris.novalnet.core.model.NovalnetPaymentInfoModel;
import de.hybris.novalnet.core.model.NovalnetPaymentRefInfoModel;


import java.text.ParseException;
import java.text.NumberFormat;
import java.net.InetAddress;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.UnknownHostException;

import javax.xml.bind.DatatypeConverter;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;
import java.util.zip.CRC32;
import java.util.Base64;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.text.DecimalFormat;

@Controller
@RequestMapping(value = "/checkout/multi/novalnet/select-payment-method")
public class NovalnetPaymentMethodCheckoutStepController extends AbstractCheckoutStepController {
    protected static final Map<String, String> CYBERSOURCE_SOP_CARD_TYPES = new HashMap<>();
    private static final String PAYMENT_METHOD = "payment-method";
    private static final String BILLING_COUNTRIES = "billingCountries";
    private static final String CART_DATA_ATTR = "cartData";

    private static final Logger LOGGER = Logger.getLogger(NovalnetPaymentMethodCheckoutStepController.class);

    @Resource(name = "addressDataUtil")
    private AddressDataUtil addressDataUtil;

    @Resource(name = "novalnetFacade")
    NovalnetFacade novalnetFacade;

    @Resource(name = "baseStoreService")
    private BaseStoreService baseStoreService;

    @Resource
    private PaymentModeService paymentModeService;

    @ModelAttribute("billingCountries")
    public Collection<CountryData> getBillingCountries() {
        return getCheckoutFacade().getCountries(CountryType.BILLING);
    }

    @ModelAttribute("cardTypes")
    public Collection<CardTypeData> getCardTypes() {
        return getCheckoutFacade().getSupportedCardTypes();
    }

    @ModelAttribute("months")
    public List<SelectOption> getMonths() {
        final List<SelectOption> months = new ArrayList<SelectOption>();

        months.add(new SelectOption("1", "01"));
        months.add(new SelectOption("2", "02"));
        months.add(new SelectOption("3", "03"));
        months.add(new SelectOption("4", "04"));
        months.add(new SelectOption("5", "05"));
        months.add(new SelectOption("6", "06"));
        months.add(new SelectOption("7", "07"));
        months.add(new SelectOption("8", "08"));
        months.add(new SelectOption("9", "09"));
        months.add(new SelectOption("10", "10"));
        months.add(new SelectOption("11", "11"));
        months.add(new SelectOption("12", "12"));

        return months;
    }

    @ModelAttribute("startYears")
    public List<SelectOption> getStartYears() {
        final List<SelectOption> startYears = new ArrayList<SelectOption>();
        final Calendar calender = new GregorianCalendar();

        for (int i = calender.get(Calendar.YEAR); i > calender.get(Calendar.YEAR) - 6; i--) {
            startYears.add(new SelectOption(String.valueOf(i), String.valueOf(i)));
        }

        return startYears;
    }

    @ModelAttribute("expiryYears")
    public List<SelectOption> getExpiryYears() {
        final List<SelectOption> expiryYears = new ArrayList<SelectOption>();
        final Calendar calender = new GregorianCalendar();

        for (int i = calender.get(Calendar.YEAR); i < calender.get(Calendar.YEAR) + 11; i++) {
            expiryYears.add(new SelectOption(String.valueOf(i), String.valueOf(i)));
        }

        return expiryYears;
    }

    @Override
    @RequestMapping(value = "/add", method = RequestMethod.GET)
    @RequireHardLogIn
    @PreValidateQuoteCheckoutStep
    @PreValidateCheckoutStep(checkoutStep = PAYMENT_METHOD)
    public String enterStep(final Model model, final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {

        getCheckoutFacade().setDeliveryModeIfAvailable();
        setupAddPaymentPage(model);

        String errorMessage = getSessionService().getAttribute("novalnetCheckoutError");
        if (errorMessage != null) {
            GlobalMessages.addErrorMessage(model, errorMessage);
        }
        getSessionService().setAttribute("novalnetCheckoutError", null);

        // Use the checkout PCI strategy for getting the URL for creating new subscriptions.
        final CheckoutPciOptionEnum subscriptionPciOption = getCheckoutFlowFacade().getSubscriptionPciOption();
        setCheckoutStepLinksForModel(model, getCheckoutStep());
        // If not using HOP or SOP we need to build up the payment details form
        final NovalnetPaymentDetailsForm paymentDetailsForm = new NovalnetPaymentDetailsForm();

        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        AddressForm addressForm = getAddressForm(cartData, model);
        paymentDetailsForm.setBillingAddress(addressForm);
        setupNovalnetPaymentPostPage(paymentDetailsForm, model);
        model.addAttribute(CART_DATA_ATTR, cartData);
        model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());

        final BaseStoreModel baseStore = this.getBaseStoreModel();
        model.addAttribute("novalnetBaseStoreConfiguration", baseStore);

        final Integer tariff = baseStore.getNovalnetTariffId();

        final Locale language = JaloSession.getCurrentSession().getSessionContext().getLocale();


        model.addAttribute("novalnetDirectDebitSepa", paymentModeService.getPaymentModeForCode("novalnetDirectDebitSepa"));
        model.addAttribute("novalnetGuaranteedDirectDebitSepa", paymentModeService.getPaymentModeForCode("novalnetGuaranteedDirectDebitSepa"));
        model.addAttribute("novalnetCreditCard", paymentModeService.getPaymentModeForCode("novalnetCreditCard"));
        model.addAttribute("novalnetPayPal", paymentModeService.getPaymentModeForCode("novalnetPayPal"));
        model.addAttribute("novalnetInvoice", paymentModeService.getPaymentModeForCode("novalnetInvoice"));
        model.addAttribute("novalnetGuaranteedInvoice", paymentModeService.getPaymentModeForCode("novalnetGuaranteedInvoice"));
        model.addAttribute("novalnetPrepayment", paymentModeService.getPaymentModeForCode("novalnetPrepayment"));
        model.addAttribute("novalnetBarzahlen", paymentModeService.getPaymentModeForCode("novalnetBarzahlen"));
        model.addAttribute("novalnetIdeal", paymentModeService.getPaymentModeForCode("novalnetIdeal"));
        model.addAttribute("novalnetGiropay", paymentModeService.getPaymentModeForCode("novalnetGiropay"));
        model.addAttribute("novalnetPrzelewy24", paymentModeService.getPaymentModeForCode("novalnetPrzelewy24"));
        model.addAttribute("novalnetEps", paymentModeService.getPaymentModeForCode("novalnetEps"));
        model.addAttribute("novalnetInstantBankTransfer", paymentModeService.getPaymentModeForCode("novalnetInstantBankTransfer"));
        model.addAttribute("novalnetPostFinance", paymentModeService.getPaymentModeForCode("novalnetPostFinance"));
        model.addAttribute("novalnetPostFinanceCard", paymentModeService.getPaymentModeForCode("novalnetPostFinanceCard"));

        final String languageCode = language.toString().toUpperCase();
        model.addAttribute("lang", languageCode);
        DecimalFormat decimalFormat = new DecimalFormat("##.##");
        String totalAmount = formatAmount(String.valueOf(cartData.getTotalPriceWithTax().getValue()));
        String orderAmount = decimalFormat.format(Float.parseFloat(totalAmount));
        model.addAttribute("orderAmount", orderAmount);
        float floatAmount = Float.parseFloat(orderAmount);
        Integer orderAmountCent = (int) (floatAmount * 100);
        final String currency = cartData.getTotalPriceWithTax().getCurrencyIso().toString();
        model.addAttribute("currency", currency);
        PaymentModeModel paymentNovalnetDirectDebitSepaModeModel = paymentModeService.getPaymentModeForCode("novalnetDirectDebitSepa");

        String guestEmail = novalnetFacade.getGuestEmail();
        final String emailAddress = (guestEmail != null) ? guestEmail : JaloSession.getCurrentSession().getUser().getLogin();
        model.addAttribute("email", emailAddress);
        model.addAttribute("orderAmountCent", orderAmountCent);

        NovalnetDirectDebitSepaPaymentModeModel novalnetDirectDebitSepaPaymentMethod = (NovalnetDirectDebitSepaPaymentModeModel) paymentNovalnetDirectDebitSepaModeModel;

        boolean novalnetDirectDebitSepaOneClickCondition = Boolean.TRUE.equals(novalnetDirectDebitSepaPaymentMethod.getNovalnetOneClickShopping()) && !novalnetFacade.isGuestUser() ? true : false;

        if (novalnetDirectDebitSepaPaymentMethod.getActive() == true) {
            showOneClickShopping("novalnetDirectDebitSepa", novalnetDirectDebitSepaOneClickCondition, paymentDetailsForm, model);
        }

        PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode("novalnetCreditCard");
        NovalnetCreditCardPaymentModeModel novalnetCreditCardPaymentMethod = (NovalnetCreditCardPaymentModeModel) paymentModeModel;


        if (novalnetCreditCardPaymentMethod.getActive() == true) {

            boolean novalnetCreditCardOneClickCondition = !novalnetCreditCardPaymentMethod.getNovalnet3dSecure() && Boolean.TRUE.equals(novalnetCreditCardPaymentMethod.getNovalnetOneClickShopping()) && !novalnetFacade.isGuestUser() ? true : false;
            showOneClickShopping("novalnetCreditCard", novalnetCreditCardOneClickCondition, paymentDetailsForm, model);
        }
        
        PaymentModeModel novalnetPayPalPaymentModeModel = paymentModeService.getPaymentModeForCode("novalnetPayPal");
        NovalnetPayPalPaymentModeModel novalnetPaymentMethod = (NovalnetPayPalPaymentModeModel) novalnetPayPalPaymentModeModel;
        
        if (novalnetPaymentMethod.getActive() == true) {
			
            boolean novalnetPayPalOneClickCondition = Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping()) && !novalnetFacade.isGuestUser() ? true : false;
			showOneClickShopping("novalnetPayPal", novalnetPayPalOneClickCondition, paymentDetailsForm, model);
		}

        model.addAttribute(BILLING_COUNTRIES, getCheckoutFacade().getBillingCountries());
        return NovalnetcheckoutaddonControllerConstants.AddPaymentMethodPage;
    }

    private void setupNovalnetPaymentPostPage(final NovalnetPaymentDetailsForm paymentDetailsForm, final Model model) {

        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute("commonPaymentDetailsForm", new NovalnetPaymentDetailsForm());
        model.addAttribute(CART_DATA_ATTR, cartData);
        model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());
        model.addAttribute("paymentDetailsForm", paymentDetailsForm);
        if (StringUtils.isNotBlank(paymentDetailsForm.getBillToCountry())) {
            model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(paymentDetailsForm.getBillToCountry()));
            model.addAttribute("country", paymentDetailsForm.getBillToCountry());
        }
    }

    public static String formatAmount(String amount) {
        if (amount.contains(",")) {
            try {
                NumberFormat formattedAmount = NumberFormat.getNumberInstance(Locale.GERMANY);
                double formattedValue = formattedAmount.parse(amount).doubleValue();
                amount = Double.toString(formattedValue);
            } catch (Exception e) {
                amount = amount.replaceAll(",", ".");
            }
        }
        return amount;
    }

    @RequestMapping(value =
            {"/add"}, method = RequestMethod.POST)
    @RequireHardLogIn
    public String add(final Model model, @Valid final NovalnetPaymentDetailsForm paymentDetailsForm, final BindingResult bindingResult)
            throws CMSItemNotFoundException {

        setupAddPaymentPage(model);

        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute(CART_DATA_ATTR, cartData);

        String selectedPaymentMethod = "";

        selectedPaymentMethod = paymentDetailsForm.getSelectedPaymentMethodId();

        // Initiate AddressData
        final AddressData addressData;
        final AddressForm addressForm = paymentDetailsForm.getBillingAddress();

        // Initiate map to store customer parameters
        Map<String, String> customerParameters = new HashMap<String, String>();

        String guestEmail = novalnetFacade.getGuestEmail();
        final String emailAddress = (guestEmail != null) ? guestEmail : JaloSession.getCurrentSession().getUser().getLogin();
        customerParameters.put("email", emailAddress);

        if (Boolean.TRUE.equals(paymentDetailsForm.isUseDeliveryAddress())) {
            addressData = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();
            if (addressData == null) {

                GlobalMessages.addErrorMessage(model, "checkout.multi.paymentMethod.createSubscription.billingAddress.noneSelectedMsg");
                return addPaymentProcess(model);
            }
            customerParameters.put("first_name", addressData.getFirstName());
            customerParameters.put("last_name", addressData.getLastName());
            customerParameters.put("street", addressData.getLine1() + addressData.getLine2());
            customerParameters.put("city", addressData.getTown());
            customerParameters.put("zip", addressData.getPostalCode());
            customerParameters.put("country", addressData.getCountry().getIsocode());
            getSessionService().setAttribute("same_as_billing", "1");
        } else {

            addressData = new AddressData();
            if (addressForm != null) {
                customerParameters.put("shipping_first_name", addressData.getFirstName());
                customerParameters.put("shipping_last_name", addressData.getLastName());
                customerParameters.put("shipping_street", addressData.getLine1() + addressData.getLine2());
                customerParameters.put("shipping_city", addressData.getTown());
                customerParameters.put("shipping_zip", addressData.getPostalCode());
                customerParameters.put("shipping_country", addressData.getCountry().getIsocode());

                addressData.setId(addressForm.getAddressId());
                addressData.setTitleCode(addressForm.getTitleCode());
                addressData.setFirstName(addressForm.getFirstName());
                addressData.setLastName(addressForm.getLastName());
                addressData.setLine1(addressForm.getLine1());
                addressData.setLine2(addressForm.getLine2());
                addressData.setTown(addressForm.getTownCity());
                addressData.setPostalCode(addressForm.getPostcode());
                if (addressForm.getCountryIso() != null) {
                    addressData.setCountry(getI18NFacade().getCountryForIsocode(addressForm.getCountryIso()));
                }
                if (addressForm.getRegionIso() != null) {
                    addressData.setRegion(getI18NFacade().getRegion(addressForm.getCountryIso(), addressForm.getRegionIso()));
                }


                addressData.setShippingAddress(Boolean.TRUE.equals(addressForm.getShippingAddress()));
                addressData.setBillingAddress(Boolean.TRUE.equals(addressForm.getBillingAddress()));
            }
            customerParameters.put("first_name", paymentDetailsForm.getBillTo_firstName());
            customerParameters.put("last_name", paymentDetailsForm.getBillTo_lastName());
            customerParameters.put("street", paymentDetailsForm.getBillTo_street1() + paymentDetailsForm.getBillTo_street2());
            customerParameters.put("city", paymentDetailsForm.getBillTo_city());
            customerParameters.put("zip", paymentDetailsForm.getBillTo_postalCode());
            customerParameters.put("country", paymentDetailsForm.getBillTo_country());
            addressData.setTitleCode(paymentDetailsForm.getBillTo_titleCode());
            addressData.setFirstName(paymentDetailsForm.getBillTo_firstName());
            addressData.setLastName(paymentDetailsForm.getBillTo_lastName());
            addressData.setLine1(paymentDetailsForm.getBillTo_street1());
            addressData.setLine2(paymentDetailsForm.getBillTo_street2());
            addressData.setTown(paymentDetailsForm.getBillTo_city());
            addressData.setPostalCode(paymentDetailsForm.getBillTo_postalCode());
        }

        // Verify Address data
        getAddressVerificationFacade().verifyAddressData(addressData);
        getSessionService().setAttribute("novalnetAddressData", addressData);
        NovalnetPaymentInfoData paymentInfoData = new NovalnetPaymentInfoData();
        paymentInfoData.setBillingAddress(addressData);

        // Set required values in session
        getSessionService().setAttribute("novalnetCustomerParams", customerParameters);
        getSessionService().setAttribute("selectedPaymentMethodId", selectedPaymentMethod);

        getSessionService().setAttribute("selectedPaymentMethodId", selectedPaymentMethod);
        String currentPayment = getSessionService().getAttribute("selectedPaymentMethodId");
        
        String token = "";
        String oneClickData = "";

        if (currentPayment.equals("novalnetDirectDebitSepa")) {
            PaymentModeModel paymentNovalnetDirectDebitSepaModeModel = paymentModeService.getPaymentModeForCode("novalnetDirectDebitSepa");

            NovalnetDirectDebitSepaPaymentModeModel novalnetDirectDebitSepaPaymentMethod = (NovalnetDirectDebitSepaPaymentModeModel) paymentNovalnetDirectDebitSepaModeModel;

            getSessionService().setAttribute("novalnetDirectDebitSepaStorePaymentData", false);
            if (Boolean.TRUE.equals(novalnetDirectDebitSepaPaymentMethod.getNovalnetOneClickShopping())) {
				
				
				try {
					oneClickData = paymentDetailsForm.getDirectDebitSepaOneClickData1();
					if (paymentDetailsForm.getDirectDebitSepaOneClickData1() == null) { 
						 oneClickData = "";
					}
				} catch(NullPointerException e) {
					LOGGER.error("oneClickData  is null", e);
					oneClickData = "";
				}
				
				
				if (!oneClickData.equals("")) {
					if (paymentDetailsForm.getDirectDebitSepaOneClickData1().trim().equals("3")) {
						getSessionService().setAttribute("novalnetDirectDebitSepaStorePaymentData", true);
					}
					if (paymentDetailsForm.getDirectDebitSepaOneClickData1().trim().equals("1")) {
						token =  getSessionService().getAttribute("novalnetDirectDebitSepaOneClickToken1");
						getSessionService().setAttribute("novalnetDirectDebitSepatoken", token);
					}
					if (paymentDetailsForm.getDirectDebitSepaOneClickData1().trim().equals("2")) {
						token =  getSessionService().getAttribute("novalnetDirectDebitSepaOneClickToken2");

						getSessionService().setAttribute("novalnetDirectDebitSepatoken", token);
					}
					
				} else {
					getSessionService().setAttribute("novalnetDirectDebitSepaStorePaymentData", true);
				}
            }

            getSessionService().setAttribute("novalnetDirectDebitSepaAccountIban", paymentDetailsForm.getAccountIban().trim());

        } else if (currentPayment.equals("novalnetGuaranteedDirectDebitSepa")) {
            PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(currentPayment);
            NovalnetGuaranteedDirectDebitSepaPaymentModeModel novalnetPaymentMethod = (NovalnetGuaranteedDirectDebitSepaPaymentModeModel) paymentModeModel;

            getSessionService().setAttribute("novalnetGuaranteedDirectDebitSepaAccountIban", paymentDetailsForm.getAccountIban().trim());
            String novalnetDirectDebitSepaGuaranteeError = handleGuaranteeProcess("novalnetGuaranteedDirectDebitSepa", paymentDetailsForm.getNovalnetGuaranteedDirectDebitSepaDateOfBirth(), paymentDetailsForm);

            if (!novalnetDirectDebitSepaGuaranteeError.equals("")) {
                GlobalMessages.addErrorMessage(model, novalnetDirectDebitSepaGuaranteeError);
                return addPaymentProcess(model);
            }

        } else if (currentPayment.equals("novalnetGuaranteedInvoice")) {
            PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(currentPayment);
            NovalnetGuaranteedInvoicePaymentModeModel novalnetPaymentMethod = (NovalnetGuaranteedInvoicePaymentModeModel) paymentModeModel;

            String novalnetGuaranteedInvoiceGuaranteeError = handleGuaranteeProcess("novalnetGuaranteedInvoice", paymentDetailsForm.getNovalnetGuaranteedInvoiceDateOfBirth(), paymentDetailsForm);

            if (!novalnetGuaranteedInvoiceGuaranteeError.equals("")) {
                GlobalMessages.addErrorMessage(model, novalnetGuaranteedInvoiceGuaranteeError);
                return addPaymentProcess(model);
            }

        } else if (currentPayment.equals("novalnetCreditCard")) {
			
			PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(currentPayment);
            NovalnetCreditCardPaymentModeModel novalnetPaymentMethod = (NovalnetCreditCardPaymentModeModel) paymentModeModel;

            getSessionService().setAttribute("novalnetCreditCardPanHash", paymentDetailsForm.getNovalnetCreditCardPanHash().trim());
            getSessionService().setAttribute("novalnetCreditCardUniqueId", paymentDetailsForm.getNovalnetCreditCardUniqueId().trim());
            getSessionService().setAttribute("do_redirect", paymentDetailsForm.getdo_redirect().trim());

            if (paymentDetailsForm.getNovalnetCreditCardPanHash().trim().equals("") && Boolean.FALSE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping())) {
                GlobalMessages.addErrorMessage(model, "novalnet.creditcard.error");
                return addPaymentProcess(model);
            }
            getSessionService().setAttribute("novalnetCreditCardStorePaymentData", false);
            if (Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping())) {
				
				try {
					oneClickData = paymentDetailsForm.getCreditCardOneClickData1();
					if (paymentDetailsForm.getCreditCardOneClickData1() == null) { 
						 oneClickData = "";
					}
				} catch(NullPointerException e) {
					LOGGER.error("oneClickData  is null", e);
					oneClickData = "";
				}
				
				if (!oneClickData.equals("")) {
					if (paymentDetailsForm.getCreditCardOneClickData1().trim().equals("3")) {
						getSessionService().setAttribute("novalnetCreditCardStorePaymentData", true);
					}
					if (paymentDetailsForm.getCreditCardOneClickData1().trim().equals("1")) {
						token =  getSessionService().getAttribute("novalnetCreditCardOneClickToken1");
						getSessionService().setAttribute("novalnetCreditCardtoken", token);
					}
					if (paymentDetailsForm.getCreditCardOneClickData1().trim().equals("2")) {
						token =  getSessionService().getAttribute("novalnetCreditCardOneClickToken2");

						getSessionService().setAttribute("novalnetCreditCardtoken", token);
					}
					
				}  else {
					getSessionService().setAttribute("novalnetCreditCardStorePaymentData", true);
				}
            }
        } else if (currentPayment.equals("novalnetPayPal")) {
			PaymentModeModel paymentModeModel = paymentModeService.getPaymentModeForCode(currentPayment);
            NovalnetPayPalPaymentModeModel novalnetPaymentMethod = (NovalnetPayPalPaymentModeModel) paymentModeModel;
            getSessionService().setAttribute("novalnetPayPalStorePaymentData", false);
            if (Boolean.TRUE.equals(novalnetPaymentMethod.getNovalnetOneClickShopping())) {
				try {
					oneClickData = paymentDetailsForm.getPayPalOneClickData1();
					if (paymentDetailsForm.getPayPalOneClickData1() == null) { 
						 oneClickData = "";
					}
				} catch(NullPointerException e) {
					LOGGER.error("oneClickData  is null", e);
					oneClickData = "";
				}
				
				if (!oneClickData.equals("")) {
					if (paymentDetailsForm.getPayPalOneClickData1().trim().equals("3")) {
						getSessionService().setAttribute("novalnetPayPalStorePaymentData", true);
					}
					if (paymentDetailsForm.getPayPalOneClickData1().trim().equals("1")) {
						token =  getSessionService().getAttribute("novalnetPayPalOneClickToken1");
						getSessionService().setAttribute("novalnetPayPaltoken", token);
					}
					if (paymentDetailsForm.getPayPalOneClickData1().trim().equals("2")) {
						token =  getSessionService().getAttribute("novalnetPayPalOneClickToken2");

						getSessionService().setAttribute("novalnetPayPaltoken", token);
					}
					
				}  else {
					getSessionService().setAttribute("novalnetPayPalStorePaymentData", true);
				}
            }
		}

        return getCheckoutStep().nextStep();
    }


    public BaseStoreModel getBaseStoreModel() {
        return getBaseStoreService().getCurrentBaseStore();
    }

    /**
     * show oneclick shopping
     *
     * @param paymentName                name of the payment
     * @param novalnetOneClickCondition  whether the one click process could be proceeded
     * @param novalnetPaymentDetailsForm the payment details submitted by the end customer
     * @param model
     */
    public void showOneClickShopping(String paymentName, boolean novalnetOneClickCondition, NovalnetPaymentDetailsForm novalnetPaymentDetailsForm, Model model) {

        model.addAttribute(paymentName + "OneClick", false);
        model.addAttribute(paymentName + "OneClickEnabled", false);
        if (Boolean.TRUE.equals(novalnetOneClickCondition)) {
            String customerNo = JaloSession.getCurrentSession().getUser().getPK().toString();

            model.addAttribute(paymentName + "OneClickEnabled", true);
            final List<NovalnetPaymentRefInfoModel> paymentInfo = novalnetFacade.getPaymentRefInfo(customerNo, paymentName);
            if (paymentInfo != null && !paymentInfo.isEmpty()) {
                if (paymentName.equals("novalnetCreditCard")) {
                    model.addAttribute("novalnetCreditCardOneClickCardType", paymentInfo.get(0).getCardType());
                    model.addAttribute("novalnetCreditCardOneClickCardHolder", paymentInfo.get(0).getCardHolder());
                    model.addAttribute("novalnetCreditCardOneClickMaskedCardNumber", paymentInfo.get(0).getMaskedCardNumber());
                    model.addAttribute("novalnetCreditCardOneClickCardExpiry", paymentInfo.get(0).getExpiryDate());
                    model.addAttribute("novalnetCreditCardOneClickToken1", paymentInfo.get(0).getToken());
                    getSessionService().setAttribute("novalnetCreditCardOneClickToken1", paymentInfo.get(0).getToken().toString());
                    if(paymentInfo.size() > 1) {
						model.addAttribute("novalnetCreditCardOneClickCardType2", paymentInfo.get(1).getCardType());
						model.addAttribute("novalnetCreditCardOneClickCardHolder2", paymentInfo.get(1).getCardHolder());
						model.addAttribute("novalnetCreditCardOneClickMaskedCardNumber2", paymentInfo.get(1).getMaskedCardNumber());
						model.addAttribute("novalnetCreditCardOneClickCardExpiry2", paymentInfo.get(1).getExpiryDate());
						model.addAttribute("novalnetCreditCardOneClickToken2", paymentInfo.get(1).getToken());
						 getSessionService().setAttribute("novalnetCreditCardOneClickToken2", paymentInfo.get(1).getToken().toString());
					}
                } else if (paymentName.equals("novalnetDirectDebitSepa")) {
                    novalnetPaymentDetailsForm.setNovalnetDirectDebitSepaOneClickAccountHolder(paymentInfo.get(0).getAccountHolder());
                    novalnetPaymentDetailsForm.setNovalnetDirectDebitSepaOneClickAccountIban(paymentInfo.get(0).getMaskedAccountIban());
                    model.addAttribute("novalnetDirectDebitSepaAccountHolder", paymentInfo.get(0).getAccountHolder());
						model.addAttribute("novalnetDirectDebitSepaAccountIban", paymentInfo.get(0).getMaskedAccountIban());
						model.addAttribute("novalnetDirectDebitSepaOneClickToken1", paymentInfo.get(0).getToken());
						getSessionService().setAttribute("novalnetDirectDebitSepaOneClickToken1", paymentInfo.get(0).getToken().toString());
                    if(paymentInfo.size() > 1) {
						model.addAttribute("novalnetDirectDebitSepaAccountHolder2", paymentInfo.get(1).getAccountHolder());
						model.addAttribute("novalnetDirectDebitSepaAccountIban2", paymentInfo.get(1).getMaskedAccountIban());
						model.addAttribute("novalnetDirectDebitSepaOneClickToken2", paymentInfo.get(1).getToken());
						getSessionService().setAttribute("novalnetDirectDebitSepaOneClickToken2", paymentInfo.get(1).getToken().toString());
					}
                } else if (paymentName.equals("novalnetPayPal")) {         
						model.addAttribute("novalnetPayPalTransactionId", paymentInfo.get(0).getPaypalTransactionID());
						model.addAttribute("novalnetPaypalEmailID", paymentInfo.get(0).getPaypalEmailID());
						model.addAttribute("novalnetPayPalOneClickToken1", paymentInfo.get(0).getToken());
						getSessionService().setAttribute("novalnetPayPalOneClickToken1", paymentInfo.get(0).getToken().toString());
                    
                     if(paymentInfo.size() > 1) {
						model.addAttribute("novalnetPayPalTransactionId2", paymentInfo.get(1).getPaypalTransactionID());
						model.addAttribute("novalnetPaypalEmailID2", paymentInfo.get(1).getPaypalEmailID());
						model.addAttribute("novalnetPayPalOneClickToken2", paymentInfo.get(1).getToken());
						getSessionService().setAttribute("novalnetPayPalOneClickToken2", paymentInfo.get(1).getToken().toString());
						
					}
                }
                model.addAttribute(paymentName + "OneClick", true);
                getSessionService().setAttribute(paymentName + "ReferenceTid", paymentInfo.get(0).getOrginalTid().toString());
                try {
                    if (paymentInfo.get(0).getToken() != null) {
                        getSessionService().setAttribute(paymentName + "ReferenceToken", paymentInfo.get(0).getToken().toString());
                    }
                } catch (NullPointerException e) {
                    LOGGER.warn("null pointer exception for token in cc", e);
                }
            }
        }
    }

    public BaseStoreService getBaseStoreService() {
        return baseStoreService;
    }

    public void setBaseStoreService(BaseStoreService baseStoreService) {
        this.baseStoreService = baseStoreService;
    }

    protected boolean checkPaymentSubscription(final Model model, @Valid final PaymentDetailsForm paymentDetailsForm,
                                               final CCPaymentInfoData newPaymentSubscription) {
        if (newPaymentSubscription != null && StringUtils.isNotBlank(newPaymentSubscription.getSubscriptionId())) {
            if (Boolean.TRUE.equals(paymentDetailsForm.getSaveInAccount()) && getUserFacade().getCCPaymentInfos(true).size() <= 1) {
                getUserFacade().setDefaultPaymentInfo(newPaymentSubscription);
            }
            getCheckoutFacade().setPaymentDetails(newPaymentSubscription.getId());
        } else {
            GlobalMessages.addErrorMessage(model, "checkout.multi.paymentMethod.createSubscription.failedMsg");
            return false;
        }
        return true;
    }

    public String getServerIpAddr() {
        try {
            InetAddress ipAddr = InetAddress.getLocalHost();
            if (ipAddr instanceof Inet4Address) {
                return ipAddr.getHostAddress();
            } else if (ipAddr instanceof Inet6Address) {
                return "127.0.0.1";
            }
        } catch (UnknownHostException ex) {
            ex.printStackTrace();
        }
        return "127.0.0.1";
    }

    /**
     * add payment process
     *
     * @param model
     * @return object
     */
    public String addPaymentProcess(Model model) throws CMSItemNotFoundException {

        return NovalnetcheckoutaddonControllerConstants.AddPaymentMethodPage;
    }

    private AddressForm getAddressForm(CartData cartData, Model model) {
        AddressForm addressForm = new AddressForm();
        if (existBillingAddressInCartData(cartData)) {
            addressForm = populateAddressForm(cartData.getPaymentInfo().getBillingAddress());
        } else if (cartData.getDeliveryAddress() != null) {
            addressForm = populateAddressForm(cartData.getDeliveryAddress());
        }

        if (StringUtils.isNotBlank(addressForm.getCountryIso())) {
            model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(addressForm.getCountryIso()));
            model.addAttribute("country", addressForm.getCountryIso());
        }
        return addressForm;
    }

    private AddressForm populateAddressForm(final AddressData addressData) {
        final AddressForm addressForm = new AddressForm();
        addressForm.setAddressId(addressData.getId());
        addressForm.setFirstName(addressData.getFirstName());
        addressForm.setLastName(addressData.getLastName());
        addressForm.setLine1(addressData.getLine1());
        if (addressData.getLine2() != null) {
            addressForm.setLine2(addressData.getLine2());
        }
        addressForm.setTownCity(addressData.getTown());
        addressForm.setPostcode(addressData.getPostalCode());
        addressForm.setCountryIso(addressData.getCountry().getIsocode());
        if (addressData.getRegion() != null) {
            addressForm.setRegionIso(addressData.getRegion().getIsocode());
        }
        addressForm.setShippingAddress(addressData.isShippingAddress());
        addressForm.setBillingAddress(addressData.isBillingAddress());
        if (addressData.getPhone() != null) {
            addressForm.setPhone(addressData.getPhone());
        }
        return addressForm;

    }


    protected void fillInPaymentData(@Valid final PaymentDetailsForm paymentDetailsForm, final CCPaymentInfoData paymentInfoData) {
        paymentInfoData.setId(paymentDetailsForm.getPaymentId());
        paymentInfoData.setCardType(paymentDetailsForm.getCardTypeCode());
        paymentInfoData.setAccountHolderName(paymentDetailsForm.getNameOnCard());
        paymentInfoData.setCardNumber(paymentDetailsForm.getCardNumber());
        paymentInfoData.setStartMonth(paymentDetailsForm.getStartMonth());
        paymentInfoData.setStartYear(paymentDetailsForm.getStartYear());
        paymentInfoData.setExpiryMonth(paymentDetailsForm.getExpiryMonth());
        paymentInfoData.setExpiryYear(paymentDetailsForm.getExpiryYear());
        if (Boolean.TRUE.equals(paymentDetailsForm.getSaveInAccount()) || getCheckoutCustomerStrategy().isAnonymousCheckout()) {
            paymentInfoData.setSaved(true);
        }
        paymentInfoData.setIssueNumber(paymentDetailsForm.getIssueNumber());
    }

    @RequestMapping(value = "/remove", method = RequestMethod.POST)
    @RequireHardLogIn
    public String remove(@RequestParam(value = "paymentInfoId") final String paymentMethodId,
                         final RedirectAttributes redirectAttributes) throws CMSItemNotFoundException {
        getUserFacade().unlinkCCPaymentInfo(paymentMethodId);
        GlobalMessages.addFlashMessage(redirectAttributes, GlobalMessages.CONF_MESSAGES_HOLDER,
                "text.account.profile.paymentCart.removed");
        return getCheckoutStep().currentStep();
    }

    /**
     * This method gets called when the "Use These Payment Details" button is clicked. It sets the selected payment
     * method on the checkout facade and reloads the page highlighting the selected payment method.
     *
     * @param selectedPaymentMethodId - the id of the payment method to use.
     * @return - a URL to the page to load.
     */
    @RequestMapping(value = "/choose", method = RequestMethod.GET)
    @RequireHardLogIn
    public String doSelectPaymentMethod(@RequestParam("selectedPaymentMethodId") final String selectedPaymentMethodId) {
        if (StringUtils.isNotBlank(selectedPaymentMethodId)) {
            getCheckoutFacade().setPaymentDetails(selectedPaymentMethodId);
        }
        return getCheckoutStep().nextStep();
    }

    @RequestMapping(value = "/back", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String back(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().previousStep();
    }

    public String getEncodedValue(String input) {

        try {
            byte[] data = input.getBytes("UTF-8");
            String encoded = DatatypeConverter.printBase64Binary(data);
            return encoded;
        } catch (UnsupportedEncodingException e) {
            return e.getMessage();
        }
    }

    @RequestMapping(value = "/next", method = RequestMethod.GET)
    @RequireHardLogIn
    @Override
    public String next(final RedirectAttributes redirectAttributes) {
        return getCheckoutStep().nextStep();
    }

    protected CardTypeData createCardTypeData(final String code, final String name) {
        final CardTypeData cardTypeData = new CardTypeData();
        cardTypeData.setCode(code);
        cardTypeData.setName(name);
        return cardTypeData;
    }

    protected void setupAddPaymentPage(final Model model) throws CMSItemNotFoundException {
        model.addAttribute("metaRobots", "noindex,nofollow");
        model.addAttribute("hasNoPaymentInfo", Boolean.valueOf(getCheckoutFlowFacade().hasNoPaymentInfo()));
        prepareDataForPage(model);
        model.addAttribute(WebConstants.BREADCRUMBS_KEY,
                getResourceBreadcrumbBuilder().getBreadcrumbs("checkout.multi.paymentMethod.breadcrumb"));
        final ContentPageModel contentPage = getContentPageForLabelOrId(MULTI_CHECKOUT_SUMMARY_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, contentPage);
        setUpMetaDataForContentPage(model, contentPage);
        setCheckoutStepLinksForModel(model, getCheckoutStep());
    }

    private boolean existBillingAddressInCartData(final CartData cartData) {
        return cartData.getPaymentInfo() != null && cartData.getPaymentInfo().getBillingAddress() != null;
    }

    protected void setupSilentOrderPostPage(final SopPaymentDetailsForm sopPaymentDetailsForm, final Model model) {
        try {
            final PaymentData silentOrderPageData = getPaymentFacade().beginSopCreateSubscription("/checkout/multi/sop/response",
                    "/integration/merchant_callback");
            model.addAttribute("silentOrderPageData", silentOrderPageData);
            sopPaymentDetailsForm.setParameters(silentOrderPageData.getParameters());
            model.addAttribute("paymentFormUrl", silentOrderPageData.getPostUrl());
        } catch (final IllegalArgumentException e) {
            model.addAttribute("paymentFormUrl", "");
            model.addAttribute("silentOrderPageData", null);
            LOGGER.warn("Failed to set up silent order post page", e);
            GlobalMessages.addErrorMessage(model, "checkout.multi.sop.globalError");
        }

        final CartData cartData = getCheckoutFacade().getCheckoutCart();
        model.addAttribute("silentOrderPostForm", new PaymentDetailsForm());
        model.addAttribute(CART_DATA_ATTR, cartData);
        model.addAttribute("deliveryAddress", cartData.getDeliveryAddress());
        model.addAttribute("sopPaymentDetailsForm", sopPaymentDetailsForm);
        model.addAttribute("paymentInfos", getUserFacade().getCCPaymentInfos(true));
        model.addAttribute("sopCardTypes", getSopCardTypes());
        if (StringUtils.isNotBlank(sopPaymentDetailsForm.getBillTo_country())) {
            model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(sopPaymentDetailsForm.getBillTo_country()));
            model.addAttribute("country", sopPaymentDetailsForm.getBillTo_country());
        }
    }

    @RequestMapping(value = "/billingaddressform", method = RequestMethod.GET)
    public String getCountryAddressForm(@RequestParam("countryIsoCode") final String countryIsoCode,
                                        @RequestParam("useDeliveryAddress") final boolean useDeliveryAddress, final Model model) {
        model.addAttribute("supportedCountries", getCountries());
        model.addAttribute("regions", getI18NFacade().getRegionsForCountryIso(countryIsoCode));
        model.addAttribute("country", countryIsoCode);

        final NovalnetPaymentDetailsForm novalnetPaymentDetailsForm = new NovalnetPaymentDetailsForm();
        model.addAttribute("novalnetPaymentDetailsForm", novalnetPaymentDetailsForm);
        if (useDeliveryAddress) {
            final AddressData deliveryAddress = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();

            if (deliveryAddress.getRegion() != null && !StringUtils.isEmpty(deliveryAddress.getRegion().getIsocode())) {
                novalnetPaymentDetailsForm.setBillTo_state(deliveryAddress.getRegion().getIsocodeShort());
            }

            novalnetPaymentDetailsForm.setBillTo_titleCode(deliveryAddress.getTitleCode());
            novalnetPaymentDetailsForm.setBillTo_firstName(deliveryAddress.getFirstName());
            novalnetPaymentDetailsForm.setBillTo_lastName(deliveryAddress.getLastName());
            novalnetPaymentDetailsForm.setBillTo_street1(deliveryAddress.getLine1());
            novalnetPaymentDetailsForm.setBillTo_street2(deliveryAddress.getLine2());
            novalnetPaymentDetailsForm.setBillTo_city(deliveryAddress.getTown());
            novalnetPaymentDetailsForm.setBillTo_postalCode(deliveryAddress.getPostalCode());
            novalnetPaymentDetailsForm.setBillTo_country(deliveryAddress.getCountry().getIsocode());
            novalnetPaymentDetailsForm.setBillTo_phoneNumber(deliveryAddress.getPhone());
        }
        return NovalnetcheckoutaddonControllerConstants.BillingAddressForm;
    }

    protected Collection<CardTypeData> getSopCardTypes() {
        final Collection<CardTypeData> sopCardTypes = new ArrayList<CardTypeData>();

        final List<CardTypeData> supportedCardTypes = getCheckoutFacade().getSupportedCardTypes();
        for (final CardTypeData supportedCardType : supportedCardTypes) {
            // Add credit cards for all supported cards that have mappings for cybersource SOP
            if (CYBERSOURCE_SOP_CARD_TYPES.containsKey(supportedCardType.getCode())) {
                sopCardTypes.add(
                        createCardTypeData(CYBERSOURCE_SOP_CARD_TYPES.get(supportedCardType.getCode()), supportedCardType.getName()));
            }
        }
        return sopCardTypes;
    }

    protected CheckoutStep getCheckoutStep() {
        return getCheckoutStep(PAYMENT_METHOD);
    }

    static {
        // Map hybris card type to Cybersource SOP credit card
        CYBERSOURCE_SOP_CARD_TYPES.put("visa", "001");
        CYBERSOURCE_SOP_CARD_TYPES.put("master", "002");
        CYBERSOURCE_SOP_CARD_TYPES.put("amex", "003");
        CYBERSOURCE_SOP_CARD_TYPES.put("diners", "005");
        CYBERSOURCE_SOP_CARD_TYPES.put("maestro", "024");
    }

    /**
     * Handle guarantee process
     *
     * @param paymentName         Name of the payment
     * @param isGuaranteeProcess  wether the guarantee is enabled
     * @param isForceNonGuarantee wether the force guarantee is enabled
     * @param dob                 date of birth entered by the customer
     * @param paymentDetailsForm  payment details submitted by the end customer
     * @return String
     */
    public String handleGuaranteeProcess(String paymentName, String dob, NovalnetPaymentDetailsForm paymentDetailsForm) {

        final AddressData addressData;
        addressData = getCheckoutFacade().getCheckoutCart().getDeliveryAddress();

        if (Boolean.FALSE.equals(paymentDetailsForm.isUseDeliveryAddress())) {
            if (!paymentDetailsForm.getBillTo_street1().equals(addressData.getLine1()) || !paymentDetailsForm.getBillTo_street2().equals(addressData.getLine2()) || !paymentDetailsForm.getBillTo_postalCode().equals(addressData.getPostalCode()) || !paymentDetailsForm.getBillTo_city().equals(addressData.getTown()) || !paymentDetailsForm.getBillTo_country().equals(addressData.getCountry().getIsocode())) {
                return "novalnet.address.error";
            }
        }
        String Guaranteerror = getSessionService().getAttribute(paymentName + "GuaranteeError");
        if (Guaranteerror != null) {
            return Guaranteerror;
        }
        String referenceTid = getSessionService().getAttribute("novalnetDirectDebitSepaReferenceTid");
        if (referenceTid == null) {
            if (!dob.equals("")) {
                boolean isValidDob = hasAgeRequirement(dob);
                if (Boolean.FALSE.equals(isValidDob)) {
                    return "novalnet.age.error";
                } else if (Boolean.TRUE.equals(isValidDob)) {
                    getSessionService().setAttribute(paymentName + "DateOfBirth", dob.trim());
                    getSessionService().setAttribute(paymentName + "PaymentGuarantee", true);
                }
            } else {
                return "novalnet.dob.error";
            }
        }
        return "";
    }

    public static boolean hasAgeRequirement(String dateInString) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-mm-dd");
        try {
            Date birthDate = sdf.parse(dateInString);

            long ageInMillis = System.currentTimeMillis() - birthDate.getTime();

            long years = ageInMillis / (365 * 24 * 60 * 60 * 1000l);

            if (years >= 18) {
                return true;
            }
            return false;
        } catch (ParseException e) {
            return false;
        }
    }

}
