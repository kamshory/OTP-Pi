function addItem(item){
var active = !item.active ? 'icon-cross' : 'icon-check';
var def = !item.defaultModem ? 'icon-cross' : 'icon-check';
var cls = '';
var connected = item.connected || item.internetConnected;
cls += (item.active?' enable':' disable');
cls += (connected?' connected':' disconnected');
cls += (item.internetAccess?' service-modem-internet':' service-modem-sms');
var service = $(
'<div class="service-item service-modem'+cls+'">\r\n'+
    '<div class="service-label"></div>\r\n'+
    '<div class="service-button">\r\n'+
    '<button class="btn btn-sm btn-success connect">Connect</button>\r\n'+
    '<button class="btn btn-sm btn-danger disconnect">Disonnect</button>\r\n'+
    '<button class="btn btn-sm btn-primary test-at">Test</button>\r\n'+
    '</div>\r\n'+
'</div>'
);
service.attr('data-id', item.id);
service.find('.service-label').text(item.name+ ' via '+item.port);
$('.service-wrapper').append(service);
$('.row-table tbody').append(
'<tr data-pk-id="' + item.id + '">\r\n' +
'	<td><input type="checkbox" class="check-all" name="id[]" value="' + item.id + '"></td>\r\n' +
'	<td><a href="modem-update.html?id=' + encodeURIComponent(item.id) + '">' + item.name + '</a></td>\r\n' +
'	<td><a href="modem-update.html?id=' + encodeURIComponent(item.id) + '">' + item.port + '</a></td>\r\n' +
'	<td align="center"><span class="icon ' + def + '"></span></td>\r\n' +
'	<td align="center"><span class="icon ' + active + '"></span></td>\r\n' +
'</tr>\r\n'
);    
}
$(document).ready(function (e1) {
    $.ajax({
        type: "GET",
        url: "data/modem/list",
        dataType: "json",
        success: function (data) {
            for (const key in data) {
            if (data.hasOwnProperty(key)) {
                addItem(data[key])
            }
            }
        }
    });

    $(document).on('click', '.service-modem-sms .connect', function(e2){
        var modemID = $(this).closest('.service-item').attr('data-id');
        $.ajax({
            url:'api/device',
            type:'POST',
            dataType: 'json',
            data:{action:'connect', id:modemID},
            success:function(receivedJSON){
                if(receivedJSON.command == "broadcast-message")
                {
                    for(var i in receivedJSON.data)
                    {
                        showNotif(receivedJSON.data[i].message);
                    }
                }
            }
        });
    });
    $(document).on('click', '.service-item .test-at', function(e2){
        var modemID = $(this).closest('.service-item').attr('data-id');
        $.ajax({
            url:'api/device',
            type:'POST',
            dataType: 'json',
            data:{action:'test-at', id:modemID},
            success:function(receivedJSON){
                if(receivedJSON.command == "broadcast-message")
                {
                    for(var i in receivedJSON.data)
                    {
                        showNotif(receivedJSON.data[i].message);
                    }
                }
            }
        });
    });
    $(document).on('click', '.service-modem-internet .connect', function(e2){
        var modemID = $(this).closest('.service-item').attr('data-id');
        $.ajax({
            url:'api/internet-dial',
            type:'POST',
            dataType: 'json',
            data:{action:'connect', id:modemID},
            success:function(receivedJSON){
                if(receivedJSON.command == "broadcast-message")
                {
                    for(var i in receivedJSON.data)
                    {
                        showNotif(receivedJSON.data[i].message);
                    }
                }
            }
        });
    });
    $(document).on('click', '.service-modem-sms .disconnect', function(e2){
        var modemID = $(this).closest('.service-item').attr('data-id');
        $.ajax({
            url:'api/device/',
            type:'POST',
            dataType: 'json',
            data:{action:'disconnect', id:modemID},
            success:function(data){
                
            }
        });
    });
    $(document).on('click', '.service-modem-internet .disconnect', function(e2){
        var modemID = $(this).closest('.service-item').attr('data-id');
        $.ajax({
            url:'api/internet-dial/',
            type:'POST',
            dataType: 'json',
            data:{action:'disconnect', id:modemID},
            success:function(data){
            }
        });
    });
    });

    function updateModemUI(modemData){
    for(var i in modemData){
        if(modemData.hasOwnProperty(i)){
        var id = i;
        $('.service-modem').filter('[data-id="'+id+'"]').removeClass('disconnected');
        $('.service-modem').filter('[data-id="'+id+'"]').removeClass('connected')
        if(modemData[i].connected || modemData[i].internetConnected){
            $('.service-modem').filter('[data-id="'+id+'"]').addClass('connected');
        }else
        {
            $('.service-modem').filter('[data-id="'+id+'"]').addClass('disconnected');
        }
        }
    }   
}

function handleIncommingMessage(message) {
    var modemData = getModemData();
    updateModemUI(modemData);        
}