function addItem(item){
$('.row-table tbody').append(
    '<tr data-pk-id="' + item.id + '">\r\n' +
    '	<td><a href="#">' + item.name + '</a></td>\r\n' +
    '	<td><a href="#">' + item.port + '</a></td>\r\n' +
    '	<td><a href="#">' + item.iccid + '</a></td>\r\n' +
    '</tr>\r\n'
);    
}
var modemList = {};
$(document).ready(function (e1) {
    $.ajax({
        type: "GET",
        url: "data/modem/list",
        dataType: "json",
        success: function (data) 
        {
            modemList = data;
            for (const key in data) 
            {
                if (data.hasOwnProperty(key)) 
                {
                    addItem(data[key])
                }
            }
        }
    });
    $(document).on('click', '.imei-table tbody tr td a', function(e2){
        e2.preventDefault();
        var id = $(this).closest('tr').attr('data-pk-id');
        var modem = modemList[id];       
        var imsi = modem.imsi;
        var iccid = modem.iccid;
        var copsOperator = modem.copsOperator;
        var msisdn = modem.msisdn;
        $('#sim-tool').find('input#imsi').val(imsi);
        $('#sim-tool').find('input#iccid').val(iccid);
        $('#sim-tool').find('input#operator').val(copsOperator);
        $('#sim-tool').find('input#msisdn').val(msisdn);
        $('#sim-tool').attr('data-modem-id', id);
        $('#sim-tool').modal('show');
    });
    $(document).on('click', '#sim-tool button#load', function(e2){
        e2.preventDefault();
        var id = $(this).closest('#sim-tool').attr('data-modem-id');
        var modem = modemList[id];       
        var port = modem.port;
        var copsOperator = modem.copsOperator;
        var msisdn = modem.msisdn;
        $.ajax({
            type: "GET",
            url: "data/modem-info/get/" + encodeURIComponent(port),
            dataType: "json",
            success: function(data) {
                $('#sim-tool').find('input#imsi').val(data.imsi);
                $('#sim-tool').find('input#iccid').val(data.iccid);
                $('#sim-tool').find('input#operator').val(copsOperator);
                $('#sim-tool').find('input#msisdn').val(msisdn);
            }
        });
    });
    $(document).on('click', '#sim-tool button#add-pin', function(e2){
        e2.preventDefault();
        var id = $(this).closest('#sim-tool').attr('data-modem-id');
        var modem = modemList[id];       
        var port = modem.port;
        var pin1 = $('#sim-tool').find('input#pin1').val();
        var pin2 = $('#sim-tool').find('input#pin2').val();
        if(pin1 != newIMEI && newIMEI != '')
        {
            $.ajax({
                type: "POST",
                url: "tool/sim",
                dataType: "json",
                data: {action:'add-pin', port:port, pin1:pin1, pin2:pin2},
                success: function(data) {
                }
            });
        }
    });

    $(document).on('click', '#sim-tool button#remove-pin', function(e2){
        e2.preventDefault();
        var id = $(this).closest('#sim-tool').attr('data-modem-id');
        var modem = modemList[id];       
        var port = modem.port;
        if(pin1 != newIMEI && newIMEI != '')
        {
            $.ajax({
                type: "POST",
                url: "tool/sim",
                dataType: "json",
                data: {action:'remove-pin', port:port},
                success: function(data) {
                }
            });
        }
    });

});

