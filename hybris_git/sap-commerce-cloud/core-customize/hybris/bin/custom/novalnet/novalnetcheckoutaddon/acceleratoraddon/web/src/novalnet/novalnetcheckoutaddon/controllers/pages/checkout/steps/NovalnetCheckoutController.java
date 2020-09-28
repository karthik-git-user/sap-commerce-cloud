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

import de.hybris.platform.acceleratorfacades.flow.impl.SessionOverrideCheckoutFlowFacade;
import de.hybris.platform.acceleratorservices.controllers.page.PageType;
import de.hybris.platform.acceleratorstorefrontcommons.annotations.RequireHardLogIn;
import de.hybris.platform.acceleratorstorefrontcommons.constants.WebConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.ThirdPartyConstants;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.pages.AbstractCheckoutController;
import de.hybris.platform.acceleratorstorefrontcommons.controllers.util.GlobalMessages;
import de.hybris.platform.acceleratorstorefrontcommons.forms.ConsentForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.GuestRegisterForm;
import de.hybris.platform.acceleratorstorefrontcommons.forms.validation.GuestRegisterValidator;
import de.hybris.platform.acceleratorstorefrontcommons.security.AutoLoginStrategy;
import de.hybris.platform.acceleratorstorefrontcommons.strategy.CustomerConsentDataStrategy;
import de.hybris.platform.cms2.exceptions.CMSItemNotFoundException;
import de.hybris.platform.cms2.model.pages.ContentPageModel;
import de.hybris.platform.commercefacades.consent.ConsentFacade;
import de.hybris.platform.commercefacades.consent.data.AnonymousConsentData;
import de.hybris.platform.commercefacades.coupon.data.CouponData;
import de.hybris.platform.commercefacades.order.CheckoutFacade;
import de.hybris.platform.commercefacades.order.OrderFacade;
import de.hybris.platform.commercefacades.order.data.OrderData;
import de.hybris.platform.commercefacades.order.data.OrderEntryData;
import de.hybris.platform.commercefacades.product.ProductFacade;
import de.hybris.platform.commercefacades.product.ProductOption;
import de.hybris.platform.commercefacades.product.data.ProductData;
import de.hybris.platform.commerceservices.customer.DuplicateUidException;
import de.hybris.platform.commerceservices.util.ResponsiveUtils;
import de.hybris.platform.servicelayer.exceptions.ModelNotFoundException;
import de.hybris.platform.servicelayer.exceptions.UnknownIdentifierException;
import de.hybris.platform.yacceleratorstorefront.controllers.ControllerConstants;
import de.hybris.platform.jalo.JaloSession;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.WebUtils;

import static de.hybris.platform.commercefacades.constants.CommerceFacadesConstants.CONSENT_GIVEN;

import novalnet.novalnetcheckoutaddon.controllers.NovalnetcheckoutaddonControllerConstants;


/**
 * CheckoutController
 */
@Controller
@RequestMapping(value = "/checkout/")
public class NovalnetCheckoutController extends AbstractCheckoutController {
    protected static final Logger LOG = Logger.getLogger(NovalnetCheckoutController.class);
    /**
     * We use this suffix pattern because of an issue with Spring 3.1 where a Uri value is incorrectly extracted if it
     * contains on or more '.' characters. Please see https://jira.springsource.org/browse/SPR-6164 for a discussion on
     * the issue and future resolution.
     */
    private static final String ORDER_CODE_PATH_VARIABLE_PATTERN = "{orderCode:.*}";

    private static final String CONTINUE_URL_KEY = "continueUrl";

    private static final String CHECKOUT_ORDER_CONFIRMATION_CMS_PAGE_LABEL = "orderConfirmation";

    @Resource(name = "productFacade")
    private ProductFacade productFacade;

    @Resource(name = "orderFacade")
    private OrderFacade orderFacade;

    @Resource(name = "checkoutFacade")
    private CheckoutFacade checkoutFacade;

    @Resource(name = "guestRegisterValidator")
    private GuestRegisterValidator guestRegisterValidator;

    @Resource(name = "autoLoginStrategy")
    private AutoLoginStrategy autoLoginStrategy;

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.CheckoutController#handleModelNotFoundException
     * (javax.servlet.http.HttpServletRequest)
     */
    @ExceptionHandler(ModelNotFoundException.class)
    public String handleModelNotFoundException(final ModelNotFoundException exception, final HttpServletRequest request) {
        request.setAttribute("message", exception.getMessage());
        return FORWARD_PREFIX + "/404";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.CheckoutController#checkout
     * (javax.servlet.http.HttpServletRequest)
     */
    @RequestMapping(method = RequestMethod.GET)
    public String checkout(final RedirectAttributes redirectModel) {
        if (getCheckoutFlowFacade().hasValidCart()) {
            if (validateCart(redirectModel)) {
                return REDIRECT_PREFIX + "/cart";
            } else {
                checkoutFacade.prepareCartForCheckout();
                return getCheckoutRedirectUrl();
            }
        }

        LOG.info("Missing, empty or unsupported cart");

        // No session cart or empty session cart. Bounce back to the cart page.
        return REDIRECT_PREFIX + "/cart";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.CheckoutController#orderConfirmation
     * (javax.servlet.http.HttpServletRequest)
     */
    @RequestMapping(value = "novalnet/orderConfirmation/" + ORDER_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.GET)
    @RequireHardLogIn
    public String orderConfirmation(@PathVariable("orderCode") final String orderCode, final HttpServletRequest request,
                                    final Model model) throws CMSItemNotFoundException {
        SessionOverrideCheckoutFlowFacade.resetSessionOverrides();
        return processOrderCode(orderCode, model, request);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.CheckoutController#orderConfirmation
     * (javax.servlet.http.HttpServletRequest)
     */
    @RequestMapping(value = "novalnet/orderConfirmation/" + ORDER_CODE_PATH_VARIABLE_PATTERN, method = RequestMethod.POST)
    public String orderConfirmation(final GuestRegisterForm form, final BindingResult bindingResult, final Model model,
                                    final HttpServletRequest request, final HttpServletResponse response, final RedirectAttributes redirectModel)
            throws CMSItemNotFoundException {
        getGuestRegisterValidator().validate(form, bindingResult);
        return processRegisterGuestUserRequest(form, bindingResult, model, request, response, redirectModel);
    }

    /**
     * Method used to determine the checkout redirect URL that will handle the checkout process.
     *
     * @return A <code>String</code> object of the URL to redirect to.
     */
    protected String getCheckoutRedirectUrl() {
        if (getUserFacade().isAnonymousUser()) {
            return REDIRECT_PREFIX + "/login/checkout";
        }

        // Default to the multi-step checkout
        return REDIRECT_PREFIX + "/checkout/multi";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.CheckoutController#processRegisterGuestUserRequest
     * (javax.servlet.http.HttpServletRequest)
     */
    protected String processRegisterGuestUserRequest(final GuestRegisterForm form, final BindingResult bindingResult,
                                                     final Model model, final HttpServletRequest request, final HttpServletResponse response,
                                                     final RedirectAttributes redirectModel) throws CMSItemNotFoundException {
        if (bindingResult.hasErrors()) {
            GlobalMessages.addErrorMessage(model, "form.global.error");
            return processOrderCode(form.getOrderCode(), model, request);
        }
        try {
            getCustomerFacade().changeGuestToCustomer(form.getPwd(), form.getOrderCode());
            getAutoLoginStrategy().login(getCustomerFacade().getCurrentCustomer().getUid(), form.getPwd(), request, response);
            getSessionService().removeAttribute(WebConstants.ANONYMOUS_CHECKOUT);
        } catch (final DuplicateUidException e) {
            // User already exists
            LOG.warn("guest registration failed: " + e);
            model.addAttribute(new GuestRegisterForm());
            GlobalMessages.addFlashMessage(redirectModel, GlobalMessages.ERROR_MESSAGES_HOLDER,
                    "guest.checkout.existingaccount.register.error", new Object[]
                            {form.getUid()});
            return REDIRECT_PREFIX + request.getHeader("Referer");
        }

        return REDIRECT_PREFIX + "/my-account";
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.CheckoutController#processOrderCode
     * (javax.servlet.http.HttpServletRequest)
     */
    protected String processOrderCode(final String orderCode, final Model model, final HttpServletRequest request)
            throws CMSItemNotFoundException {
        final OrderData orderDetails;

        try {
            orderDetails = orderFacade.getOrderDetailsForCode(orderCode);
        } catch (final UnknownIdentifierException e) {
            LOG.warn("Attempted to load an order confirmation that does not exist or is not visible. Redirect to home page.");
            return REDIRECT_PREFIX + ROOT;
        }

        addRegistrationConsentDataToModel(model);

        if (orderDetails.isGuestCustomer() && !StringUtils.substringBefore(orderDetails.getUser().getUid(), "|")
                .equals(getSessionService().getAttribute(WebConstants.ANONYMOUS_CHECKOUT_GUID))) {
            return getCheckoutRedirectUrl();
        }

        if (orderDetails.getEntries() != null && !orderDetails.getEntries().isEmpty()) {
            for (final OrderEntryData entry : orderDetails.getEntries()) {
                final String productCode = entry.getProduct().getCode();
                final ProductData product = productFacade.getProductForCodeAndOptions(productCode,
                        Arrays.asList(ProductOption.BASIC, ProductOption.PRICE, ProductOption.CATEGORIES));
                entry.setProduct(product);
            }
        }

        model.addAttribute("orderCode", orderCode);
        model.addAttribute("orderData", orderDetails);
        model.addAttribute("allItems", orderDetails.getEntries());
        model.addAttribute("deliveryAddress", orderDetails.getDeliveryAddress());
        model.addAttribute("deliveryMode", orderDetails.getDeliveryMode());
        model.addAttribute("paymentInfo", orderDetails.getPaymentInfo());
        model.addAttribute("pageType", PageType.ORDERCONFIRMATION.name());
        model.addAttribute("tid", getSessionService().getAttribute("tid"));

        final List<CouponData> giftCoupons = orderDetails.getAppliedOrderPromotions().stream()
                .filter(x -> CollectionUtils.isNotEmpty(x.getGiveAwayCouponCodes())).flatMap(p -> p.getGiveAwayCouponCodes().stream())
                .collect(Collectors.toList());
        model.addAttribute("giftCoupons", giftCoupons);

        processEmailAddress(model, orderDetails);

        final String continueUrl = (String) getSessionService().getAttribute(WebConstants.CONTINUE_URL);
        model.addAttribute(CONTINUE_URL_KEY, (continueUrl != null && !continueUrl.isEmpty()) ? continueUrl : ROOT);

        final ContentPageModel checkoutOrderConfirmationPage = getContentPageForLabelOrId(CHECKOUT_ORDER_CONFIRMATION_CMS_PAGE_LABEL);
        storeCmsPageInModel(model, checkoutOrderConfirmationPage);
        setUpMetaDataForContentPage(model, checkoutOrderConfirmationPage);
        model.addAttribute(ThirdPartyConstants.SeoRobots.META_ROBOTS, ThirdPartyConstants.SeoRobots.NOINDEX_NOFOLLOW);

        return NovalnetcheckoutaddonControllerConstants.Views.Pages.Checkout.CheckoutPage;
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.CheckoutController#getGuestRegisterValidator
     * (javax.servlet.http.HttpServletRequest)
     */
    protected GuestRegisterValidator getGuestRegisterValidator() {
        return guestRegisterValidator;
    }

    protected void processEmailAddress(final Model model, final OrderData orderDetails) {
        final String uid;

        if (orderDetails.isGuestCustomer() && !model.containsAttribute("guestRegisterForm")) {
            final GuestRegisterForm guestRegisterForm = new GuestRegisterForm();
            guestRegisterForm.setOrderCode(orderDetails.getGuid());
            uid = orderDetails.getPaymentInfo().getBillingAddress().getEmail();
            guestRegisterForm.setUid(uid);
            model.addAttribute(guestRegisterForm);
        } else {
            uid = orderDetails.getUser().getUid();
        }
        model.addAttribute("email", uid);
    }

    /*
     * (non-Javadoc)
     *
     * @see
     * de.hybris.platform.storefront.controllers.pages.CheckoutController#getAutoLoginStrategy
     * (javax.servlet.http.HttpServletRequest)
     */
    protected AutoLoginStrategy getAutoLoginStrategy() {
        return autoLoginStrategy;
    }

}
