/*
 * [y] hybris Platform
 *
 * Released under the GNU General Public License
 * This free contribution made by request.
 * If you have found this script useful a small
 * recommendation as well as a comment on merchant form
 * would be greatly appreciated.
 *
 *
 */
package novalnet.novalnetcheckoutaddon.forms;

import de.hybris.platform.acceleratorstorefrontcommons.forms.AddressForm;

import javax.validation.constraints.NotNull;
import java.util.Locale;

import java.util.Map;

/**
 * NovalnetPaymentDetailsForm
 */
public class NovalnetPaymentDetailsForm {
    private String paymentId;

    private String amount;
    private String billToCountry;
    private String comments;
    private String currency;
    private Map<String, String> parameters;

    private boolean saveInAccount;


    private String selectedPaymentMethodId;
    private String creditCardOneClickData1;
    private String creditCardOneClickData2;
    private String directDebitSepaOneClickData1;
    private String directDebitSepaOneClickData2;
    private String payPalOneClickData1;
    private String paypalOneClickData2;

    private String bankCountry;
    private String accountIban;
    private String novalnetCreditCardPanHash;
    private String novalnetCreditCardUniqueId;
    private String do_redirect;
    private String previousSelectedPayment;
    private boolean novalnetCreditCardOneClickProcess;
    private boolean novalnetDirectDebitSepaOneClickProcess;
    private boolean novalnetPayPalOneClickProcess;

    private String billTo_city; // NOSONAR
    private String billTo_country; // NOSONAR
    private String billTo_customerID; // NOSONAR
    private String billTo_email; // NOSONAR
    private String billTo_firstName; // NOSONAR
    private String billTo_lastName; // NOSONAR
    private String billTo_phoneNumber; // NOSONAR
    private String billTo_postalCode; // NOSONAR
    private String billTo_titleCode; // NOSONAR
    private String billTo_state; // NOSONAR
    private String billTo_street1; // NOSONAR
    private String billTo_street2; // NOSONAR

    private String novalnetCreditCardOneClickCardType;
    private String novalnetCreditCardOneClickCardHolder;
    private String novalnetCreditCardOneClickMaskedCardNumber;
    private String novalnetCreditCardOneClickToken1;
    private String novalnetCreditCardOneClickCardExpiry;
    private String creditCardOneClickNewDeatails;

    private String novalnetDirectDebitSepaOneClickAccountHolder;
    private String novalnetDirectDebitSepaOneClickMaskedAccountIban;

    private String novalnetGuaranteedDirectDebitSepaDateOfBirth;
    private boolean novalnetDirectDebitSepaGuaranteeProcess;
    private String novalnetGuaranteedInvoiceDateOfBirth;
    private boolean novalnetInvoiceGuaranteeProcess;

    private String novalnetPaypalOneClickPpTransactionId;
    private String novalnetPaypalOneClickRefTransactionId;

    private AddressForm billingAddress;
    private boolean newBillingAddress;
    private boolean useDeliveryAddress;
    private boolean savePaymentInfo;


    public String getPaymentId() {
        return this.paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public boolean isNewBillingAddress() {
        return this.newBillingAddress;
    }


    public String getAmount() {
        return amount;
    }

    public void setAmount(final String amount) {
        this.amount = amount;
    }

    public String getBillToCountry() {
        return billToCountry;
    }

    public void setBillToCountry(String billToCountry) {
        this.billToCountry = billToCountry;
    }

    public String getComments() {
        return this.comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCurrency() {
        return this.currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }


    public Map<String, String> getParameters() {
        return this.parameters;
    }

    public void setParameters(Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public boolean isSaveInAccount() {
        return saveInAccount;
    }

    public void setSaveInAccount(final boolean saveInAccount) {
        this.saveInAccount = saveInAccount;
    }

    /**
     * @return billingAddress
     */
    public AddressForm getBillingAddress() {
        return billingAddress;
    }

    /**
     * @param billingAddress the billingAddress to set
     */
    public void setBillingAddress(final AddressForm billingAddress) {
        this.billingAddress = billingAddress;
    }

    /**
     * @return the newBillingAddress
     */
    public Boolean getNewBillingAddress() {
        return newBillingAddress;
    }

    /**
     * @param newBillingAddress the newBillingAddress to set
     */
    public void setNewBillingAddress(final Boolean newBillingAddress) {
        this.newBillingAddress = newBillingAddress;
    }

    /**
     * @return the saveInAccount
     */
    public Boolean getSaveInAccount() {
        return saveInAccount;
    }

    /**
     * @param saveInAccount the saveInAccount to set
     */
    public void setSaveInAccount(final Boolean saveInAccount) {
        this.saveInAccount = saveInAccount;
    }

    /**
     * @return the selectedPaymentMethodId
     */
    public String getSelectedPaymentMethodId() {
        return selectedPaymentMethodId;
    }

    /**
     * @return the selectedPaymentMethodId
     */
    public String getCreditCardOneClickData1() {
        return creditCardOneClickData1;
    }
    
    public String getDirectDebitSepaOneClickData1() {
        return directDebitSepaOneClickData1;
    }
    
    public String getPayPalOneClickData1() {
        return payPalOneClickData1;
    }
    
    public void setPayPalOneClickData1(final String payPalOneClickData1) {
        this.payPalOneClickData1 = payPalOneClickData1;
    }


    /**
     * @return the selectedPaymentMethodId
     */
    public String getDirectDebitSepaOneClickData2() {
        return directDebitSepaOneClickData2;
    }
    
    public String getPayPalOneClickData2() {
        return paypalOneClickData2;
    }
    
    public String getCreditCardOneClickData2() {
        return creditCardOneClickData2;
    }

    /**
     * @param selectedPaymentMethodId the selectedPaymentMethodId to set
     */
    public void setCreditCardOneClickData2(final String creditCardOneClickData2) {
        this.creditCardOneClickData2 = creditCardOneClickData2;
    }
    
    public void setDirectDebitSepaOneClickData2(final String directDebitSepaOneClickData2) {
        this.directDebitSepaOneClickData2 = directDebitSepaOneClickData2;
    }
    
    public void setPayPalOneClickData2(final String paypalOneClickData2) {
        this.paypalOneClickData2 = paypalOneClickData2;
    }

    /**
     * @param selectedPaymentMethodId the selectedPaymentMethodId to set
     */
    public void setCreditCardOneClickData1(final String creditCardOneClickData1) {
        this.creditCardOneClickData1 = creditCardOneClickData1;
    }
    
    public void setDirectDebitSepaOneClickData1(final String directDebitSepaOneClickData1) {
        this.directDebitSepaOneClickData1 = directDebitSepaOneClickData1;
    }
    
    
    /**
     * @param selectedPaymentMethodId the selectedPaymentMethodId to set
     */
    public void setSelectedPaymentMethodId(final String selectedPaymentMethodId) {
        this.selectedPaymentMethodId = selectedPaymentMethodId;
    }


    /**
     * @return the accountIban
     */
    public String getAccountIban() {
        return accountIban;
    }

    /**
     * @param accountIban the accountIban to set
     */
    public void setAccountIban(final String accountIban) {
        this.accountIban = accountIban;
    }

    /**
     * @return the novalnetCreditCardPanHash
     */
    public String getNovalnetCreditCardPanHash() {
        return novalnetCreditCardPanHash;
    }

    /**
     * @param novalnetCreditCardPanHash the novalnetCreditCardPanHash to set
     */
    public void setNovalnetCreditCardPanHash(final String novalnetCreditCardPanHash) {
        this.novalnetCreditCardPanHash = novalnetCreditCardPanHash;
    }

    /**
     * @return the novalnetCreditCardUniqueId
     */
    public String getNovalnetCreditCardUniqueId() {
        return novalnetCreditCardUniqueId;
    }
    
    public String getdo_redirect() {
        return do_redirect;
    }

    /**
     * @param novalnetCreditCardUniqueId the novalnetCreditCardUniqueId to set
     */
    public void setNovalnetCreditCardUniqueId(final String novalnetCreditCardUniqueId) {
        this.novalnetCreditCardUniqueId = novalnetCreditCardUniqueId;
    }
    
    public void setdo_redirect(final String do_redirect) {
        this.do_redirect = do_redirect;
    }

    /**
     * @return the previousSelectedPayment
     */
    public String getPreviousSelectedPayment() {
        return previousSelectedPayment;
    }

    /**
     * @param previousSelectedPayment the previousSelectedPayment to set
     */
    public void setPreviousSelectedPayment(final String previousSelectedPayment) {
        this.previousSelectedPayment = previousSelectedPayment;
    }

    /**
     * @return the novalnetCreditCardOneClickProcess
     */
    public boolean isNovalnetCreditCardOneClickProcess() {
        return novalnetCreditCardOneClickProcess;
    }

    /**
     * @param novalnetCreditCardOneClickProcess the novalnetCreditCardOneClickProcess to set
     */
    public void setNovalnetCreditCardOneClickProcess(final boolean novalnetCreditCardOneClickProcess) {
        this.novalnetCreditCardOneClickProcess = novalnetCreditCardOneClickProcess;
    }

    /**
     * @return the novalnetDirectDebitSepaOneClickProcess
     */
    public boolean isNovalnetDirectDebitSepaOneClickProcess() {
        return novalnetDirectDebitSepaOneClickProcess;
    }

    /**
     * @param novalnetDirectDebitSepaOneClickProcess the novalnetDirectDebitSepaOneClickProcess to set
     */
    public void setNovalnetDirectDebitSepaOneClickProcess(final boolean novalnetDirectDebitSepaOneClickProcess) {
        this.novalnetDirectDebitSepaOneClickProcess = novalnetDirectDebitSepaOneClickProcess;
    }

    /**
     * @return the novalnetPaypalOneClickProcess
     */
    public boolean isNovalnetPayPalOneClickProcess() {
        return novalnetPayPalOneClickProcess;
    }

    /**
     * @param novalnetPaypalOneClickProcess the novalnetPaypalOneClickProcess to set
     */
    public void setNovalnetPayPalOneClickProcess(final boolean novalnetPayPalOneClickProcess) {
        this.novalnetPayPalOneClickProcess = novalnetPayPalOneClickProcess;
    }

    /**
     * @return the novalnetCreditCardOneClickCardType
     */
    public String getNovalnetCreditCardOneClickCardType() {
        return novalnetCreditCardOneClickCardType;
    }

    public String getCreditCardOneClickNewDeatails() {
        return creditCardOneClickNewDeatails;
    }

    /**
     * @param novalnetCreditCardOneClickCardType the novalnetCreditCardOneClickCardType to set
     */
    public void setNovalnetCreditCardOneClickCardType(final String novalnetCreditCardOneClickCardType) {
        this.novalnetCreditCardOneClickCardType = novalnetCreditCardOneClickCardType;
    }

    /**
     * @param novalnetCreditCardOneClickCardType the novalnetCreditCardOneClickCardType to set
     */
    public void setCreditCardOneClickNewDeatails(final String creditCardOneClickNewDeatails) {
        this.creditCardOneClickNewDeatails = creditCardOneClickNewDeatails;
    }


    /**
     * @return the novalnetCreditCardOneClickCardHolder
     */
    public String getNovalnetCreditCardOneClickCardHolder() {
        return novalnetCreditCardOneClickCardHolder;
    }

    /**
     * @param novalnetCreditCardOneClickCardHolder the novalnetCreditCardOneClickCardHolder to set
     */
    public void setNovalnetCreditCardOneClickCardHolder(final String novalnetCreditCardOneClickCardHolder) {
        this.novalnetCreditCardOneClickCardHolder = novalnetCreditCardOneClickCardHolder;
    }

    /**
     * @return the novalnetCreditCardOneClickMaskedCardNumber
     */
    public String getNovalnetCreditCardOneClickMaskedCardNumber() {
        return novalnetCreditCardOneClickMaskedCardNumber;
    }

    /**
     * @return the novalnetCreditCardOneClickMaskedCardNumber
     */
    public String getNovalnetCreditCardOneClickToken1() {
        return novalnetCreditCardOneClickToken1;
    }

    /**
     * @param novalnetCreditCardOneClickMaskedCardNumber the novalnetCreditCardOneClickMaskedCardNumber to set
     */
    public void setNovalnetCreditCardOneClickMaskedCardNumber(final String novalnetCreditCardOneClickMaskedCardNumber) {
        this.novalnetCreditCardOneClickMaskedCardNumber = novalnetCreditCardOneClickMaskedCardNumber;
    }

    /**
     * @param novalnetCreditCardOneClickMaskedCardNumber the novalnetCreditCardOneClickMaskedCardNumber to set
     */
    public void setNovalnetCreditCardOneClickToken1(final String novalnetCreditCardOneClickToken1) {
        this.novalnetCreditCardOneClickToken1 = novalnetCreditCardOneClickToken1;
    }

    /**
     * @return the novalnetCreditCardOneClickCardExpiry
     */
    public String getNovalnetCreditCardOneClickCardExpiry() {
        return novalnetCreditCardOneClickCardExpiry;
    }

    /**
     * @param novalnetCreditCardOneClickCardExpiry the novalnetCreditCardOneClickCardExpiry to set
     */
    public void setNovalnetCreditCardOneClickCardExpiry(final String novalnetCreditCardOneClickCardExpiry) {
        this.novalnetCreditCardOneClickCardExpiry = novalnetCreditCardOneClickCardExpiry;
    }

    /**
     * @return useDeliveryAddress
     */
    public boolean isUseDeliveryAddress() {
        return useDeliveryAddress;
    }

    /**
     * @param useDeliveryAddress the useDeliveryAddress to set
     */
    public void setUseDeliveryAddress(final boolean useDeliveryAddress) {
        this.useDeliveryAddress = useDeliveryAddress;
    }

    /**
     * @return billTo_city
     */
    public String getBillTo_city() // NOSONAR
    {
        return billTo_city;
    }

    /**
     * @param billTo_city the billTo_city to set
     */
    public void setBillTo_city(final String billTo_city) // NOSONAR
    {
        this.billTo_city = billTo_city;
    }

    /**
     * @return billTo_country
     */
    public String getBillTo_country() // NOSONAR
    {
        if (billTo_country != null) {
            return billTo_country.toUpperCase(Locale.US);
        }
        return billTo_country;
    }

    /**
     * @param setBillTo_country the BillTo_country to set
     */
    public void setBillTo_country(final String billTo_country) // NOSONAR
    {
        this.billTo_country = billTo_country;
    }

    /**
     * @return billTo_customerID
     */
    public String getBillTo_customerID() // NOSONAR
    {
        return billTo_customerID;
    }

    /**
     * @param billTo_customerID the billTo_customerID to set
     */
    public void setBillTo_customerID(final String billTo_customerID) // NOSONAR
    {
        this.billTo_customerID = billTo_customerID;
    }

    /**
     * @return billTo_email
     */
    public String getBillTo_email() // NOSONAR
    {
        return billTo_email;
    }

    /**
     * @param billTo_email the billTo_email to set
     */
    public void setBillTo_email(final String billTo_email) // NOSONAR
    {
        this.billTo_email = billTo_email;
    }

    /**
     * @return billTo_firstName
     */
    public String getBillTo_firstName() // NOSONAR
    {
        return billTo_firstName;
    }

    /**
     * @param billTo_firstName the billTo_firstName to set
     */
    public void setBillTo_firstName(final String billTo_firstName) // NOSONAR
    {
        this.billTo_firstName = billTo_firstName;
    }

    /**
     * @return billTo_lastName
     */
    public String getBillTo_lastName() // NOSONAR
    {
        return billTo_lastName;
    }

    /**
     * @param billTo_lastName the billTo_lastName to set
     */
    public void setBillTo_lastName(final String billTo_lastName) // NOSONAR
    {
        this.billTo_lastName = billTo_lastName;
    }

    /**
     * @return billTo_phoneNumber
     */
    public String getBillTo_phoneNumber() // NOSONAR
    {
        return billTo_phoneNumber;
    }

    /**
     * @param billTo_phoneNumber the billTo_phoneNumber to set
     */
    public void setBillTo_phoneNumber(final String billTo_phoneNumber) // NOSONAR
    {
        this.billTo_phoneNumber = billTo_phoneNumber;
    }

    /**
     * @return billTo_postalCode
     */
    public String getBillTo_postalCode() // NOSONAR
    {
        return billTo_postalCode;
    }

    /**
     * @param billTo_postalCode the billTo_postalCode to set
     */
    public void setBillTo_postalCode(final String billTo_postalCode) // NOSONAR
    {
        this.billTo_postalCode = billTo_postalCode;
    }

    /**
     * @return billTo_titleCode
     */
    public String getBillTo_titleCode() // NOSONAR
    {
        return billTo_titleCode;
    }

    /**
     * @param billTo_titleCode the billTo_titleCode to set
     */
    public void setBillTo_titleCode(final String billTo_titleCode) // NOSONAR
    {
        this.billTo_titleCode = billTo_titleCode;
    }

    /**
     * @return billTo_state
     */
    public String getBillTo_state() // NOSONAR
    {
        return billTo_state;
    }

    /**
     * @param billTo_state the billTo_state to set
     */
    public void setBillTo_state(final String billTo_state) // NOSONAR
    {
        this.billTo_state = billTo_state;
    }

    /**
     * @return billTo_street1
     */
    public String getBillTo_street1() // NOSONAR
    {
        return billTo_street1;
    }

    /**
     * @param billTo_street1 the billTo_street1 to set
     */
    public void setBillTo_street1(final String billTo_street1) // NOSONAR
    {
        this.billTo_street1 = billTo_street1;
    }

    /**
     * @return billTo_street2
     */
    public String getBillTo_street2() // NOSONAR
    {
        return billTo_street2;
    }

    /**
     * @param billTo_street2 the billTo_street2 to set
     */
    public void setBillTo_street2(final String billTo_street2) // NOSONAR
    {
        this.billTo_street2 = billTo_street2;
    }

    /**
     * @return savePaymentInfo
     */
    public boolean isSavePaymentInfo() // NOSONAR
    {
        return savePaymentInfo;
    }

    /**
     * @param savePaymentInfo the savePaymentInfo to set
     */
    public void setSavePaymentInfo(final boolean savePaymentInfo) // NOSONAR
    {
        this.savePaymentInfo = savePaymentInfo;
    }

    /**
     * @return novalnetDirectDebitSepaOneClickAccountHolder
     */
    public String getNovalnetDirectDebitSepaOneClickAccountHolder() // NOSONAR
    {
        return novalnetDirectDebitSepaOneClickAccountHolder;
    }

    /**
     * @param novalnetDirectDebitSepaOneClickAccountHolder the novalnetDirectDebitSepaOneClickAccountHolder to set
     */
    public void setNovalnetDirectDebitSepaOneClickAccountHolder(final String novalnetDirectDebitSepaOneClickAccountHolder) // NOSONAR
    {
        this.novalnetDirectDebitSepaOneClickAccountHolder = novalnetDirectDebitSepaOneClickAccountHolder;
    }

    /**
     * @return novalnetDirectDebitSepaOneClickMaskedAccountIban
     */
    public String getNovalnetDirectDebitSepaOneClickMaskedAccountIban() // NOSONAR
    {
        return novalnetDirectDebitSepaOneClickMaskedAccountIban;
    }

    /**
     * @param novalnetDirectDebitSepaOneClickMaskedAccountIban the novalnetDirectDebitSepaOneClickMaskedAccountIban to set
     */
    public void setNovalnetDirectDebitSepaOneClickAccountIban(final String novalnetDirectDebitSepaOneClickMaskedAccountIban) // NOSONAR
    {
        this.novalnetDirectDebitSepaOneClickMaskedAccountIban = novalnetDirectDebitSepaOneClickMaskedAccountIban;
    }

    /**
     * @return novalnetPaypalOneClickPpTransactionId
     */
    public String getNovalnetPaypalOneClickPpTransactionId() // NOSONAR
    {
        return novalnetPaypalOneClickPpTransactionId;
    }

    /**
     * @return novalnetPaypalOneClickRefTransactionId
     */
    public String getNovalnetPaypalOneClickRefTransactionId() // NOSONAR
    {
        return novalnetPaypalOneClickRefTransactionId;
    }

    /**
     * @param novalnetPaypalOneClickPpTransactionId the novalnetPaypalOneClickPpTransactionId to set
     */
    public void setNovalnetPaypalOneClickPpTransactionId(final String novalnetPaypalOneClickPpTransactionId) // NOSONAR
    {
        this.novalnetPaypalOneClickPpTransactionId = novalnetPaypalOneClickPpTransactionId;
    }

    /**
     * @param NovalnetPaypalOneClickRefTransactionId the NovalnetPaypalOneClickRefTransactionId to set
     */
    public void setNovalnetPaypalOneClickRefTransactionId(final String novalnetPaypalOneClickRefTransactionId) // NOSONAR
    {
        this.novalnetPaypalOneClickRefTransactionId = novalnetPaypalOneClickRefTransactionId;
    }


    /**
     * @return novalnetDirectDebitSepaGuaranteeProcess
     */
    public boolean isNovalnetDirectDebitSepaGuaranteeProcess() // NOSONAR
    {
        return novalnetDirectDebitSepaGuaranteeProcess;
    }

    /**
     * @param novalnetDirectDebitSepaGuaranteeProcess the novalnetDirectDebitSepaGuaranteeProcess to set
     */
    public void setNovalnetDirectDebitSepaGuaranteeProcess(final boolean novalnetDirectDebitSepaGuaranteeProcess) // NOSONAR
    {
        this.novalnetDirectDebitSepaGuaranteeProcess = novalnetDirectDebitSepaGuaranteeProcess;
    }

    /**
     * @return novalnetInvoiceDateOfBirth
     */
    public String getNovalnetGuaranteedInvoiceDateOfBirth() // NOSONAR
    {
        return novalnetGuaranteedInvoiceDateOfBirth;
    }

    /**
     * @return novalnetInvoiceDateOfBirth
     */
    public String getNovalnetGuaranteedDirectDebitSepaDateOfBirth() // NOSONAR
    {
        return novalnetGuaranteedDirectDebitSepaDateOfBirth;
    }

    /**
     * @param novalnetInvoiceDateOfBirth the novalnetInvoiceDateOfBirth to set
     */
    public void setNovalnetGuaranteedInvoiceDateOfBirth(final String novalnetGuaranteedInvoiceDateOfBirth) // NOSONAR
    {
        this.novalnetGuaranteedInvoiceDateOfBirth = novalnetGuaranteedInvoiceDateOfBirth;
    }

    /**
     * @param novalnetInvoiceDateOfBirth the novalnetInvoiceDateOfBirth to set
     */
    public void setNovalnetGuaranteedDirectDebitSepaDateOfBirth(final String novalnetGuaranteedDirectDebitSepaDateOfBirth) // NOSONAR
    {
        this.novalnetGuaranteedDirectDebitSepaDateOfBirth = novalnetGuaranteedDirectDebitSepaDateOfBirth;
    }

    /**
     * @return novalnetInvoiceGuaranteeProcess
     */
    public boolean isNovalnetInvoiceGuaranteeProcess() // NOSONAR
    {
        return novalnetInvoiceGuaranteeProcess;
    }

    /**
     * @param novalnetInvoiceGuaranteeProcess the novalnetInvoiceGuaranteeProcess to set
     */
    public void setNovalnetInvoiceGuaranteeProcess(final boolean novalnetInvoiceGuaranteeProcess) // NOSONAR
    {
        this.novalnetInvoiceGuaranteeProcess = novalnetInvoiceGuaranteeProcess;
    }

}
