/*
 * Copyright (c) 2019 SAP SE or an SAP affiliate company. All rights reserved.
 */
package novalnet.novalnetcheckoutaddon.constants;

/**
 * Global class for all Novalnetcheckoutaddon web constants. You can add global constants for your extension into this class.
 */
public interface NovalnetcheckoutaddonWebConstants // NOSONAR
{

    String ADDON_PREFIX = "addon:/novalnetcheckoutaddon/";
    String SUMMARY_CHECKOUT_PREFIX = "/checkout/multi/novalnet/summary";
    //empty to avoid instantiating this constant class

    interface Views {

        interface Pages {

            interface MultiStepCheckout {
                String AddEditDeliveryAddressPage = ADDON_PREFIX + "pages/checkout/multi/addEditDeliveryAddressPage"; // NOSONAR
                String ChooseDeliveryMethodPage = ADDON_PREFIX + "pages/checkout/multi/chooseDeliveryMethodPage"; // NOSONAR
                String ChoosePickupLocationPage = ADDON_PREFIX + "pages/checkout/multi/choosePickupLocationPage"; // NOSONAR
                String AddPaymentMethodPage = ADDON_PREFIX + "pages/checkout/multi/addPaymentMethodPage"; // NOSONAR
                String CheckoutSummaryPage = ADDON_PREFIX + "pages/checkout/multi/checkoutSummaryPage"; // NOSONAR
                String HostedOrderPageErrorPage = ADDON_PREFIX + "pages/checkout/multi/hostedOrderPageErrorPage"; // NOSONAR
                String HostedOrderPostPage = ADDON_PREFIX + "pages/checkout/multi/hostedOrderPostPage"; // NOSONAR
                String SilentOrderPostPage = ADDON_PREFIX + "pages/checkout/multi/silentOrderPostPage"; // NOSONAR
                String GiftWrapPage = ADDON_PREFIX + "pages/checkout/multi/giftWrapPage"; // NOSONAR
            }
        }

    }


    // implement here constants used by this extension
}
