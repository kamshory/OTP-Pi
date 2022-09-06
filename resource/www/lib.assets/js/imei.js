function addItem(item){
$('.row-table tbody').append(
    '<tr data-pk-id="' + item.id + '">\r\n' +
    '	<td><a href="#">' + item.name + '</a></td>\r\n' +
    '	<td><a href="#">' + item.port + '</a></td>\r\n' +
    '	<td><a href="#">' + item.imei + '</a></td>\r\n' +
    '</tr>\r\n'
);    
}
var modemList = {};
$(document).ready(function (e1) {
    initTableContent();
    $(document).on('click', '.imei-table tbody tr td a', function(e2){
        e2.preventDefault();
        var id = $(this).closest('tr').attr('data-pk-id');
        var modem = modemList[id];       
        var imei = modem.imei;
        $('#imei-changer').find('input#current').val(imei);
        $('#imei-changer').find('input#new').val(imei);
        $('#imei-changer').attr('data-modem-id', id);
        $('#imei-changer').modal('show');
    });
    $(document).on('click', '#imei-changer button#load', function(e2){
        e2.preventDefault();
        var id = $(this).closest('#imei-changer').attr('data-modem-id');
        var modem = modemList[id];       
        var port = modem.port;
        $.ajax({
            type: "GET",
            url: "data/modem-info/get/" + encodeURIComponent(port),
            dataType: "json",
            success: function(data) {
                $('#imei-changer').find('input#current').val(data.imei);
                $('#imei-changer').find('input#new').val(data.imei);
            }
        });
    });
    $(document).on('click', '#imei-changer button#save', function(e2){
        e2.preventDefault();
        var id = $(this).closest('#imei-changer').attr('data-modem-id');
        var modem = modemList[id];       
        var port = modem.port;
        var currentIMEI = $('#imei-changer').find('input#current').val().trim();
        var newIMEI = $('#imei-changer').find('input#new').val().trim();
        if(currentIMEI != newIMEI && newIMEI != '')
        {
            $.ajax({
                type: "POST",
                url: "tool/imei",
                dataType: "json",
                data: {action:'update', port:port, current_imei:currentIMEI, new_imei:newIMEI},
                success: function(data) {
                    $('#imei-changer').find('input#current').val(data.imei);
                    $('#imei-changer').find('input#new').val(data.imei);
                    initTableContent();
                    $('#imei-changer').modal('hide');
                }
            });
        }
    });

});

function initTableContent()
{
    $.ajax({
        type: "GET",
        url: "data/modem/list",
        dataType: "json",
        success: function (data) 
        {
            modemList = data;
            $('.row-table tbody').empty();
            for (const key in data) 
            {
                if (data.hasOwnProperty(key)) 
                {
                    addItem(data[key])
                }
            }
        }
    });
}

