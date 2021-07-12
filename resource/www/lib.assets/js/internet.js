$(document).ready(function (e1) {
    $.ajax({
        type: "GET",
        url: "data/modem/list",
        dataType: "json",
        success: function (data) {
        	var ia = 0;
            for (const key in data) {
                if (data.hasOwnProperty(key)) {
                    var item = data[key];
                    var internetAccess = item.internetAccess;
                    if(internetAccess)
                    {
                        console.log(item);
                        var cls = '';
                        cls += (item.active ? ' enable' : ' disable');
                        cls += (item.internetConnected ? ' connected' : ' disconnected');
                        var service = $('<div class="service-item service-modem-internet service-modem' + cls + '">\r\n' +
                            '<div class="service-label"></div>\r\n' +
                            '<div class="service-button">\r\n' +
                            '<button class="btn btn-sm btn-success connect">Connect</button>\r\n' +
                            '<button class="btn btn-sm btn-danger disconnect">Disonnect</button>\r\n' +
                            '</div>\r\n' +
                            '</div>');
                        service.attr('data-id', item.id);
                        service.find('.service-label').text(item.name + ' via ' + item.port);
                        $('.service-wrapper').append(service);
                        ia++;
                    }
                }
                if(ia == 0)
                {
                	$('.phone-page').append('<div role="alert" class="alert alert-success">No internet modem</div>');
                }
            }
        }
    });

    $(document).on('click', '.service-wrapper .connect', function (e2) {
        var modemID = $(this).closest('.service-item').attr('data-id');
        $.ajax({
            url: 'api/internet-dial/',
            type: 'POST',
            dataType: 'json',
            data: { action: 'connect', id: modemID },
            success: function (data) {
                /**
                do nothing
                */
            }
        });
    });
    $(document).on('click', '.service-wrapper .disconnect', function (e2) {
        var modemID = $(this).closest('.service-item').attr('data-id');
        $.ajax({
            url: 'api/internet-dial/',
            type: 'POST',
            dataType: 'json',
            data: { action: 'disconnect', id: modemID },
            success: function (data) {
                /**
                do nothing
                */
            }
        });
    });
});

function updateModemUI(modemData) {
    for (var i in modemData) {
        if (modemData.hasOwnProperty(i)) {
            var id = i;
            $('.service-modem').filter('[data-id="' + id + '"]').removeClass('disconnected');
            $('.service-modem').filter('[data-id="' + id + '"]').removeClass('connected')
            if (modemData[i].internetConnected) 
            {
                $('.service-modem').filter('[data-id="' + id + '"]').addClass('connected');
            }
            else 
            {
                $('.service-modem').filter('[data-id="' + id + '"]').addClass('disconnected');
            }
        }
    }
}

function handleIncommingMessage(message) {
    var modemData = getModemData();
    updateModemUI(modemData);
}