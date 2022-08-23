$(document).ready(function(e){
    var activateMessage = '<div class="alert alert-danger activation-alert" role="alert">This device has not been activated. <a href="activation.html">Click hede to activate</a></div>';
    if(window.location.href.toString().indexOf('modem-add.html') != -1 || window.location.href.toString().indexOf('modem-update.html') != -1)
    {
        if($('.phone-page .activation-alert').length > 0)
        {
            $('.phone-page .activation-alert').remove();
        }
        $('.phone-page').prepend(activateMessage);
        $('.phone-page input[type="text"], .phone-page input[type="password"], .phone-page input[type="button"], .phone-page input[type="submit"], .phone-page input[type="reset"], .phone-page button').attr('disabled', 'disabled');
    }
    else if(window.location.href.toString().indexOf('modem.html') != -1)
    {
        if($('.phone-page .activation-alert').length > 0)
        {
            $('.phone-page .activation-alert').remove();
        }
        $('.phone-page').prepend(activateMessage);
        $('.phone-page #add-modem, .phone-page #delete-modem').attr('disabled', 'disabled');
    }
   
});