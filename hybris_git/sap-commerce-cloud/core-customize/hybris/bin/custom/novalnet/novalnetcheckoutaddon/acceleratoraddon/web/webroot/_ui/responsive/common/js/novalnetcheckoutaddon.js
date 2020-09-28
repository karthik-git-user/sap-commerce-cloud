/*
 * [y] hybris Platform
 *
 * Copyright (c) 2017 SAP SE or an SAP affiliate company.  All rights reserved.
 *
 * This software is the confidential and proprietary information of SAP
 * ("Confidential Information"). You shall not disclose such Confidential
 * Information and shall use it only in accordance with the terms of the
 * license agreement you entered into with SAP.
 */

ACC.novalnetcheckoutaddon = {
	
	spinner: $("<img id='novalnetLoading' src='" + ACC.config.commonResourcePath + "/images/spinner.gif' />"),
		
    initialProcess: function ()
    {
        if($('#previousSelectedPayment') != undefined && $('#previousSelectedPayment').val() != null && $('#previousSelectedPayment').val() != '') {
            $(document).find('input:radio[name=selectedPaymentMethodId][value='+ $('#previousSelectedPayment').val() +']').attr('checked', true);
        }else{
            $(document).find('input:radio[name=selectedPaymentMethodId]:first').attr('checked', true);
        }
		
		ACC.novalnetcheckoutaddon.paymentSelectionProcess();
        $('input:radio[name=selectedPaymentMethodId]').click(function () {
            ACC.novalnetcheckoutaddon.paymentSelectionProcess();
        });
        
        if($('#accountIban').length) {
			$('#accountIban').css('text-transform','uppercase');
		}
        
		// Process hash call.
		if($('#novalnetCreditCard').is(":checked")){
			ACC.novalnetcheckoutaddon.loadIframe();
		}
		    
		$( ".submit_novalnetPaymentDetailsForm").click(function () {
			 if($('#novalnetCreditCard').is(":checked") && $('#novalnetCreditCardPanHash').val() == '' ) {
				 event.preventDefault();
				 event.stopImmediatePropagation();
				if ( $( '#novalnetCreditCardIframe').css('display') != 'none' ) {
					Novalnet.getPanHash();
				} else {
					$('#paymentDetailsForm').submit();
				}
				
			} else {
				$('#paymentDetailsForm').submit();
			}
		});
		
		$('input:radio[name=directDebitSepaOneClickData1]').click(function () {
			 if($('#directDebitSepaOneClickData1AddNew').is(":checked")){
				 $('.novalnetDirectDebitSepaOneClickForm').css('display','block');
				 $('.novalnetCreditCardIframe').css('display','block');
				
			} else {
				$('.novalnetDirectDebitSepaOneClickForm').css('display','none');
				$('.novalnetCreditCardIframe').css('display','none');
			}
		});
		$('input:radio[name=creditCardOneClickData1]').click(function () {
			 if($('#creditCardOneClickNewDeatails').is(":checked")){
				 $('.novalnetCreditCardOneClickForm').css('display','block');
				
			} else {
				$('.novalnetCreditCardOneClickForm').css('display','none');
			}
		});
		$('input:radio[name=payPalOneClickData1]').click(function () {
			 if($('#payPalOneClickData1AddNew').is(":checked")){
				 $('.novalnetPayPalOneClickForm').css('display','block');
				
			} else {
				$('.novalnetPayPalOneClickForm').css('display','none');
			}
		});

	},
    paymentSelectionProcess: function ()
    {
        if($('input:radio[name=selectedPaymentMethodId]') != undefined){
            $.each([ 'novalnetCreditCard', 'novalnetDirectDebitSepa', 'novalnetInvoice', 'novalnetPrepayment', 'novalnetBarzahlen', 'novalnetPayPal', 'novalnetInstantBankTransfer', 'novalnetIdeal', 'novalnetEps', 'novalnetGiropay', 'novalnetPrzelewy24', 'novalnetGuaranteedDirectDebitSepa', 'novalnetGuaranteedInvoice', 'novalnetPostFinance', 'novalnetPostFinanceCard' ], function (index, value) {
                $('input:radio[name=selectedPaymentMethodId]:checked').val() == value ? $('#'+ value +'PaymentForm').show() : $('#'+ value +'PaymentForm').hide();
            });

            if($('#novalnetDirectDebitSepa').is(":checked") || $('#novalnetGuaranteedDirectDebitSepa').is(":checked")) {

                $( '#accountIban' ).keypress(
                    function (event) {
                        return ACC.novalnetcheckoutaddon.allowAlphanumericKey( event );
                    }
                );
            }
				
			if($('#novalnetCreditCardIframe').attr("id") != undefined) {
				
				$( '#novalnetCreditCardPaymentOption' ).on(
					'click', function (event) {
						
						if ( $( '#novalnetCreditCardOneClickElements').css('display') == 'none' ) {
							ACC.novalnetcheckoutaddon.showOneClickForm('novalnetCreditCard');
						} else {
							ACC.novalnetcheckoutaddon.showPaymentForm('novalnetCreditCard');
						}
					}
				);
				
				if ( $( '#novalnetCreditCardOneClickProcess' ).val() == '1') {
					ACC.novalnetcheckoutaddon.showOneClickForm('novalnetCreditCard');
				} else {
					ACC.novalnetcheckoutaddon.showPaymentForm('novalnetCreditCard');
				}
			}
			$( '#novalnetDirectDebitSepaPaymentOption' ).on(
				'click', function (event) {
					if ( $( '#novalnetDirectDebitSepaOneClickElements').css('display') == 'none' ) {
						ACC.novalnetcheckoutaddon.showOneClickForm('novalnetDirectDebitSepa');
						$('#novalnetDirectDebitSepaDateOfBirth').parent('div').css('display','none');
					} else {
						ACC.novalnetcheckoutaddon.showPaymentForm('novalnetDirectDebitSepa');
						$('#novalnetDirectDebitSepaDateOfBirth').parent('div').css('display','block');
					}
				}
			);
        }

		if ( $( '#novalnetDirectDebitSepaOneClickProcess' ).val() == '1') {
			ACC.novalnetcheckoutaddon.showOneClickForm('novalnetDirectDebitSepa');
			$('#novalnetDirectDebitSepaDateOfBirth' ).parent('div').css('display','none');
		} else {
			ACC.novalnetcheckoutaddon.showPaymentForm('novalnetDirectDebitSepa');
			$('#novalnetDirectDebitSepaDateOfBirth' ).parent('div').css('display','block');
		}
    },
    
    
	loadIframe : function () {
		
		var inline = 0;
		var testMode;
		var sameAsBilling = 0;
		var bill_to_country;
		
		if($('#novalnetInlineCC').length) {
			inline = 1;
		}
		
		if($('#useDeliveryAddress').is(":checked")) {
			sameAsBilling = 1;
		}
		
		if(sameAsBilling == 1) {
			bill_to_country = $("#address.country").children("option:selected").val();
			bill_to_zip = $("#address.postcode").val();
			bill_to_city = $("#address.townCity").val();
			bill_to_street = $("#address.line1").val() + ' ' + $("#address.line2").val();
		}
		
		Novalnet.setClientKey($('#Clientkey').val());
		
		if($('#novalnetTestMode').val() != undefined && $('#novalnetTestMode').val() == 1) {
			testMode = 1;
		} else{
			testMode = 0;
		}
        var configurationObject = {
        
            // You can handle the process here, when specific events occur.
            callback: {
            
                // Called once the pan_hash (temp. token) created successfully.
                on_success: function (data) {
                    document.getElementById('novalnetCreditCardPanHash').value = data ['hash'];
                    document.getElementById('novalnetCreditCardUniqueId').value = data ['unique_id'];
                    document.getElementById('do_redirect').value = data ['do_redirect'];
                    $('#paymentDetailsForm').submit();
                    return true;
                },
                
                // Called in case of an invalid payment data or incomplete input. 
                on_error:  function (data) {
                    if ( undefined !== data['error_message'] ) {
                        alert(data['error_message']);
                        return false;
                    }
                },
                
                // Called in case the challenge window Overlay (for 3ds2.0) displays 
                on_show_overlay:  function (data) {
                    document.getElementById('novalnetCreditCardIframe').classList.add(".overlay");
                },
                
                // Called in case the Challenge window Overlay (for 3ds2.0) hided
                on_hide_overlay:  function (data) {
                    document.getElementById('novalnetCreditCardIframe').classList.remove(".overlay");
                }
            },
            
            iframe: {
            
                // It is mandatory to pass the Iframe ID here.  Based on which the entire process will took place.
                id: "novalnetCreditCardIframe",
                
                // Set to 1 to make you Iframe input container more compact (default - 0)
                inline: inline,
                
                // Add the style (css) here for either the whole Iframe contanier or for particular label/input field
                style: {
                    // The css for the Iframe container
                    container: $('#novalnetStandardCss').val(),
                    
                    // The css for the input field of the Iframe container
                    input: $('#novalnetStandardInputCss').val(),
                    
                    // The css for the label of the Iframe container
                    label: $('#novalnetStandardLabelCss').val()
                },
                
                // You can customize the text of the Iframe container here
                text: {
                
                    // The End-customers selected language. The Iframe container will be rendered in this Language.
                    lang : $('#lang').val(),
                    
                    // Basic Error Message
                    error: "Your credit card details are invalid",
                    
                    // You can customize the text for the Card Holder here
                    card_holder : {
                    
                        // You have to give the Customized label text for the Card Holder Container here
                        label: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.holder.label.text'],
                        
                        // You have to give the Customized placeholder text for the Card Holder Container here
                        place_holder: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.holder.placeholder.text'],
                        
                        // You have to give the Customized error text for the Card Holder Container here
                        error: "Please enter the valid card holder name"
                    },

                    card_number : {
                    
                        // You have to give the Customized label text for the Card Number Container here
                        label: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.number.label.text'],
                        
                        // You have to give the Customized placeholder text for the Card Number Container here
                        place_holder:  ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.number.placeholder.text'],
                        
                        // You have to give the Customized error text for the Card Number Container here
                        error: "Please enter the valid card number"
                    },
                    expiry_date : {
                    
                        // You have to give the Customized label text for the Expiry Date Container here
                        label: "Expiry date",
                        
                        // You have to give the Customized error text for the Expiry Date Container here
                        error: "Please enter the valid expiry month / year in the given format"
                    },
                    cvc : {
                    
                        // You have to give the Customized label text for the CVC/CVV/CID Container here
                        label: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.cvc.hint.text'],
                        
                        // You have to give the Customized placeholder text for the CVC/CVV/CID Container here
                        place_holder: ACC.addons.novalnetcheckoutaddon['novalnet.creditcard.cvc.placeholder.text'],
                        
                        // You have to give the Customized error text for the CVC/CVV/CID Container here
                        error: "Please enter the valid CVC/CVV/CID"
                    }
                }
            },
            
            // Add Customer data
            customer: {
            
                // Your End-customer's First name which will be prefilled in the Card Holder field
                first_name: $('#ship_firstName').val(),
                
                // Your End-customer's Last name which will be prefilled in the Card Holder field
                last_name: $('#ship_lastName').val(),
                
                // Your End-customer's Email ID. 
                email: $('#email').val(),
                
                // Your End-customer's billing address.
                billing: {
                
                    // Your End-customer's billing street (incl. House no).
                    street: $('#ship_street').val(),
                    
                    // Your End-customer's billing city.
                    city: $('#ship_city').val(),
                    
                    // Your End-customer's billing zip.
                    zip: $('#ship_zip').val(),
                    
                    // Your End-customer's billing country ISO code.
                    country_code: $('#ship_country').val(),
                },
                shipping: {
                
                    // Set to 1 if the billing and shipping address are same and no need to specify shipping details again here.
                    "same_as_billing": 1,
                },
            },
            
            // Add transaction data
            transaction: {
            
                // The payable amount that can be charged for the transaction (in minor units), for eg:- Euro in Eurocents (5,22 EUR = 522).
                amount: $("#orderAmount").val(),
                
                // The three-character currency code as defined in ISO-4217.
                currency: $("#currency").val(),
                
                // Set to 1 for the TEST transaction (default - 0).
                test_mode: testMode
            }
		}
		Novalnet.createCreditCardForm(configurationObject);
	},
    
	showPaymentForm : function (paymentName) {
		// Assigning payment form functionality.
		$('#'+ paymentName +'PaymentFormElements').css('display','block');
		$('#'+ paymentName +'OneClickElements').css('display','none');
		$('#'+ paymentName +'OneClickProcess').val('0');
		$('#'+ paymentName +'PaymentOption').html(ACC.addons.novalnetcheckoutaddon['novalnet.'+ paymentName +'GivenDetails.text']);
	},

	showOneClickForm : function (paymentName) {
		// Assigning one click shop functionality.
		$('#'+ paymentName +'PaymentFormElements' ).css('display','none');
		$('#'+ paymentName +'OneClickElements' ).css('display','block');
		$('#'+ paymentName +'OneClickProcess' ).val('1');
		$('#'+ paymentName +'PaymentOption' ).html(ACC.addons.novalnetcheckoutaddon['novalnet.'+ paymentName +'NewDetails.text']);
		$('#'+ paymentName +'OneClickElements input[type="text"]').prop('readonly', true);
		$('#'+ paymentName +'PaymentOption').removeAttr('class');
	},
	allowAlphanumericKey : function ( event ) {

		var keycode = ( 'which' in event ) ? event.which : event.keyCode,
		reg         = /^(?:[0-9a-zA-Z]+$)/;
		return ( reg.test( String.fromCharCode( keycode ) ) || 0 === keycode || 8 === keycode );
	},
	allowNameKey : function ( event ) {

		var keycode = ( 'which' in event ) ? event.which : event.keyCode,
		reg         = /[^0-9\[\]\/\\#,+@!^()$~%'"=:;<>{}\_\|*?`]/g;
		return ( reg.test( String.fromCharCode( keycode ) ) || 0 === keycode || 8 === keycode );
	},
}

$(document).ready(function ()
{
	if($("#novalnetComments") != undefined && $(".orderBox.payment") != undefined && (parseFloat($("#novalnetComments").css("height")) > parseFloat( $(".orderBox.payment").css("height")))) {
		$(".orderBox.payment").css("height", 
			parseFloat($(".orderBox.payment").css("height")) + parseFloat($("#novalnetComments").css("height"))
		);
	}
	if($('#cancelorderForm') != undefined) {
		$('#cancelorderForm').hide();
	}
	
	if($('.order-payment-data').find('.value-order') != undefined) {
		if($('.order-payment-data').find('.value-order').html() != undefined) {
			var orderCommernts = $('.order-payment-data').find('.value-order').html();
			$('.order-payment-data').find('.value-order').html("<span class='novalnet-transaction-comments' >"+orderCommernts.replace(/&lt;br&gt;/g, "</span>&nbsp;<span class='novalnet-transaction-comments'>")+"</span>");
		}
	}
	
    with (ACC.novalnetcheckoutaddon)
    {
        initialProcess();
        return true;
    }
    
    if($('#paygateurl').length) {
        window.open($('#paygateurl').val(), '_self');
    }

    
});
