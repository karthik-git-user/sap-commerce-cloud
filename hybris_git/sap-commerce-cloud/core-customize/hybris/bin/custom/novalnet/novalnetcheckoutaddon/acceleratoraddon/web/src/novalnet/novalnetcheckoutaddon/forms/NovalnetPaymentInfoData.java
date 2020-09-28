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
package novalnet.novalnetcheckoutaddon.forms;

import de.hybris.platform.commercefacades.order.data.CardTypeData;
import de.hybris.platform.commercefacades.user.data.AddressData;

/**
 * NovalnetPaymentInfoData
 */
public class NovalnetPaymentInfoData implements java.io.Serializable {
    private String transactionDetails;

    private String paymentName;

    private AddressData billingAddress;

    private String paymentEmailAddress;

    private String orderHistoryNotes;

    private String paymentGatewayStatus;

    private String id;


    public NovalnetPaymentInfoData() {
        // default constructor
    }

    /**
     * @param transactionDetails the transactionDetails to set
     */
    public void setTransactionDetails(final String transactionDetails) {
        this.transactionDetails = transactionDetails;
    }

    /**
     * @return the transactionDetails
     */
    public String getTransactionDetails() {
        return transactionDetails;
    }

    /**
     * @param paymentName the paymentName to set
     */
    public void setPaymentName(final String paymentName) {
        this.paymentName = paymentName;
    }

    /**
     * @return the paymentName
     */
    public String getPaymentName() {
        return paymentName;
    }

    /**
     * @param billingAddress the billingAddress to set
     */
    public void setBillingAddress(final AddressData billingAddress) {
        this.billingAddress = billingAddress;
    }

    /**
     * @return the billingAddress
     */
    public AddressData getBillingAddress() {
        return billingAddress;
    }

    /**
     * @param paymentEmailAddress the paymentEmailAddress to set
     */
    public void setPaymentEmailAddress(final String paymentEmailAddress) {
        this.paymentEmailAddress = paymentEmailAddress;
    }

    /**
     * @return the paymentEmailAddress
     */
    public String getPaymentEmailAddress() {
        return paymentEmailAddress;
    }

    /**
     * @param orderHistoryNotes the orderHistoryNotes to set
     */
    public void setOrderHistoryNotes(final String orderHistoryNotes) {
        this.orderHistoryNotes = orderHistoryNotes;
    }

    /**
     * @return the orderHistoryNotes
     */
    public String getPaymentGatewayStatus() {
        return paymentGatewayStatus;
    }

    public void setPaymentGatewayStatus(final String paymentGatewayStatus) {
        this.paymentGatewayStatus = paymentGatewayStatus;
    }

    /**
     * @return the orderHistoryNotes
     */
    public String getOrderHistoryNotes() {
        return orderHistoryNotes;
    }

    /**
     * @param orderHistoryNidotes the id to set
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * @return the id
     */
    public String getId() {
        return id;
    }
}
