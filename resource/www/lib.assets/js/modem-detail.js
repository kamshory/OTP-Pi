var setting = {};

var registrationStatus = [
    'Not registered, MT is not currently searching a new operator to register to',
    'Registered, home network',
    'Not registered, but MT is currently searching a new operator to register to',
    'Registration denied',
    'Unknown (e.g. out of GERAN/UTRAN/E-UTRAN coverage)',
    'Registered, roaming',
    'Registered for "SMS only", home network (applicable only when indicates E-UTRAN)',
    'Registered for "SMS only", roaming (applicable only when indicates E-UTRAN)',
    'Attached for emergency bearer services only (see NOTE 2) (not applicable)',
    'Registered for "CSFB not preferred", home network (applicable only when indicates E-UTRAN)',
    'Registered for "CSFB not preferred", roaming (applicable only when indicates E-UTRAN)'
];

var technology = [
    'GSM',
    'GSM Compact',
    'UTRAN',
    'GSM w/EGPRS',
    'UTRAN w/HSDPA',
    'UTRAN w/HSUPA',
    'UTRAN w/HSDPA and HSUPA',
    'E-UTRAN'
];

var to = setTimeout('', 1);

$(document).ready(function(e) {
    $.ajax({
        type: "GET",
        url: "data/sms-setting/get",
        dataType: "json",
        success: function(data) {
            setting = data;
            applyTag();
        }
    });

    $(document).on('blur', '[name="recipient_prefix"]', function(e2) {
        applyTag();
    });

    $(document).on('click', '.list-port', function(e2){
        var sel = $(this);
        $.ajax({
            type: "GET",
            url: "data/serial-port/list",
            dataType: "json",
            success: function(data) {
                if(data.length > 0)
                {
                    sel.closest('tr').find('.list-port-container').css({'display':'block'});
                    sel.closest('tr').find('.list-port-container > ul').empty();
                    for(var i = 0; i<data.length; i++)
                    {
                        var li = $('<li />');
                        var a = $('<a />');
                        a.attr({'data-port': data[i].systemPortName, 'href':'#'});
                        a.text(data[i].descriptivePortName);
                        li.append(a);
                        sel.closest('tr').find('.list-port-container ul').append(li);
                    }
                }
            }
        });      
    });
    $(document).on('mouseover', '.list-port-container', function(e2)
    {
        clearTimeout(to);
    });
    $(document).on('mouseout', '.list-port-container', function(e2)
    {
        var obj = $(this);
        to = setTimeout(function(){
            obj.fadeOut('fast');
        }, 1200);
    });

    $(document).on('click', '.list-port-container ul li a', function(e2)
    {
        var value = $(this).attr('data-port') || '';
        if(value != '')
        {
            $(this).closest('tr').find('input[type="text"]').val(value);
            detect();
        }
        $(this).closest('tr').find('.list-port-container').css({'display':'none'});
        
    });

    $(document).on('click', '#status', function(e2)
    {
        var stausData = ($('#status').attr('data-status') || '').trim();
        var arr = stausData.split(',');
        var v1 = registrationStatus[parseInt(arr[0])] || '';
        var v2 = technology[parseInt(arr[1])] || '';
        $('#resgistration-status .modal-body').empty();
        var html = '<table class="modal-table modal-table-connection"><tbody><tr><td>Status</td><td>'+v1+'</td></tr><tr><td>Technology</td><td>'+v2+'</td></tr></tbody></table>';
        $('#resgistration-status .modal-body').append(html);
        $('#resgistration-status').modal('show');
    });
    
    $(document).on('change', '[name="internet_access"]', function(e2) 
    {
        if($(this).prop('checked')) 
        {
            $('.tr-dial').css('display', 'table-row');
        } 
        else 
        {
            $('.tr-dial').css('display', 'none');
        }
    });

    $(document).on('click', '#detect', function(e2) 
    {
        detect();
    });
});

function detect()
{
    var port = $('[name="port"]').val().trim();
    if(port == '') 
    {
        $('[name="port"]').select();
    } 
    else 
    {
        $('.detecting').empty().append('<span class="animation-pressure"><span></span>');
        $.ajax({
            type: "GET",
            url: "data/modem-info/get/" + encodeURIComponent(port),
            dataType: "json",
            success: function(data) {
                $('[name="manufacturer"]').val(data.manufacturer || '');
                $('[name="model"]').val(data.model || '');
                $('[name="revision"]').val(data.revision || '');
                $('[name="imei"]').val(data.imei || '');
                $('[name="operator_select"]').val(data.operatorSelect);
                $('[name="msisdn"]').val(data.msisdn || '');
                $('[name="imsi"]').val(data.imsi || '');
                $('[name="iccid"]').val(data.iccid || '');
                $('[name="sms_center"]').val(data.smsCenter || '');
                $('.detecting').empty();
                var networkRegistration = data.networkRegistration || '';
                if(networkRegistration.indexOf(',') > 0)
                {
                    $('#status').attr('data-status', networkRegistration);
                    $('#status').removeAttr('disabled');
                }
            }
        });
    }
}

function applyTag() {
    if(setting.countryCode != '' && setting.recipientPrefixLength > 0) {
        var value = $('[name="recipient_prefix"]').val();
        var arr = value.split(',');
        for(var i in arr) {
            arr[i] = arr[i].trim();
            if(arr[i].indexOf('0') == 0 && arr[i].length > 1) {
                arr[i] = setting.countryCode + arr[i].substring(1);
            }
        }
        value = arr.join(', ');
        $('[name="recipient_prefix"]').val(value);
    }
}

function loadDetail()
{
    var urlParams = new URLSearchParams(window.location.search);
    var id = urlParams.get('id') || '';
    if(id.length > 0) 
    {
        $.ajax({
            type: "GET",
            url: "data/modem/detail/" + id,
            dataType: "json",
            success: function(data) {
               $('[name="id"]').val(data.id);
                $('[name="name"]').val(data.name);
                $('[name="port"]').val(data.port);
                $('[name="sms_center"]').val(data.smsCenter);
                $('[name="manufacturer"]').val(data.manufacturer || '');
                $('[name="model"]').val(data.model || '');
                $('[name="revision"]').val(data.revision || '');
                $('[name="imei"]').val(data.imei);
                $('[name="operator_select"]').val(data.operatorSelect);
                $('[name="msisdn"]').val(data.msisdn);
                $('[name="imsi"]').val(data.imsi);
                $('[name="iccid"]').val(data.iccid);
                $('[name="recipient_prefix"]').val(data.recipientPrefix);
                $('[name="baud_rate"]').val(data.baudRate);
                $('[name="parity_bit"]').val(data.parityBit);
                $('[name="start_bits"]').val(data.startBits);
                $('[name="stop_bits"]').val(data.stopBits);
                $('[name="internet_access"]').prop('checked', data.internetAccess);
                $('[name="apn"]').val(data.apn);
                $('[name="apn_username"]').val(data.apnUsername);
                $('[name="apn_password"]').val(data.apnPassword);
                $('[name="dial_number"]').val(data.dialNumner);
                $('[name="init_dial_1"]').val(data.initDial1);
                $('[name="init_dial_2"]').val(data.initDial2);
                $('[name="init_dial_3"]').val(data.initDial3);
                $('[name="init_dial_4"]').val(data.initDial4);
                $('[name="init_dial_5"]').val(data.initDial5);
                $('[name="dial_command"]').val(data.dialCommand);
                $('[name="autoreconnect"]').prop('checked', data.autoreconnect);
                $('[name="sms_api"]').prop('checked', data.smsAPI);
                $('[name="delete_sent_sms"]').prop('checked', data.deleteSentSMS);
                $('[name="default_modem"]').prop('checked', data.defaultModem);
                $('[name="active"]').prop('checked', data.active);
                if(data.internetAccess) {
                    $('.tr-dial').css('display', 'table-row');
                }
                applyTag();
            }
        });
    } 
    else 
    {
        window.location = "modem.html";
    }
}