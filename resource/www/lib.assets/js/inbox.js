function isReplyable(phone)
{
    phone = phone.trim();
    var test2 = phone;
    test2 = test2.split(' ').join('').trim();
    test2 = test2.split('-').join('').trim();
    test2 = test2.split('(').join('').trim();
    test2 = test2.split(')').join('').trim();
    test2 = test2.split('+').join('').trim();
    test2 = test2.replace(/[0-9]/g, '').trim();
    if(test2.length > 0)
    {
        return false;
    }
    return true;
}
$(document).ready(function(e){
    $(document).on('click', '.message-reply .btn', function(e2){
        var obj = $(this).closest('.message-item');
        var modemID = obj.attr('data-modem-id');
        var sender = obj.attr('data-sender');
        modemID = encodeURIComponent(modemID);
        sender = encodeURIComponent(sender);
        window.location = 'sms.html?modem_id='+modemID+'&receiver='+sender;
    });
    $(document).on('click', '.load-sms', function(e2){
        var modemID = $('#modem').val();
        $.ajax({
            type:"GET",
            url:"data/sms/inbox/"+modemID,
            dataType:"json",
            success:function(data){
                $('.message-list').empty();
                for(var i in data)
                {
                    var sms = data[i];

                    var outer = $('<div />');
                    outer.attr({'data-modem-id':sms.modemID, 'data-storage':sms.storage, 'data-sms-id':sms.id, 'data-sender':sms.phoneNumber});
                    outer.addClass('message-item');
                    var inner = $('<div />');
                    inner.addClass('message-item-wrapper');
                    var inner1 = $('<div />');
                    inner1.addClass('message-sender');
                    inner1.text(sms.phoneNumber);

                    var inner2 = $('<div />');
                    inner2.addClass('message-date-time');
                    inner2.text(sms.date.substring(0, 19));

                    var inner6 = $('<div />');
                    inner6.addClass('message-reply');
                    if(isReplyable(sms.phoneNumber))
                    {
                        inner6.html('<button class="btn btn-sm btn-success">Reply</button>');
                    }

                    var inner3 = $('<div />');
                    inner3.addClass('message-sim-card');
                    inner3.text(sms.modemPort+' - '+sms.modemName);

                    var inner4 = $('<div />');
                    inner4.addClass('message-action');

                    var inner5 = $('<div />');
                    inner5.addClass('message-message');
                    inner5.html(sms.content.split('\r\n').join('\r\n<br>'));

                    inner.append(inner2);
                    inner.append(inner1);
                    inner.append(inner6);                    
                    inner.append(inner3);
                    inner.append(inner4);
                    inner.append(inner5);
                    outer.append(inner);

                    $('.message-list').append(outer);
                }
            }
        });
    });
    
    $('.message-item').contextmenu(function(e2) {
        if(!$(this).hasClass('message-item-selected'))
        {
            $(this).addClass('message-item-selected');
        }
        else
        {
            $(this).removeClass('message-item-selected');
        }
    });
    $(document).on('dblclick', '.message-item', function(e2){
        if(!$(this).hasClass('message-item-selected'))
        {
            $(this).addClass('message-item-selected');
        }
        else
        {
            $(this).removeClass('message-item-selected');
        }
    });
    $(document).on('click', '.check-all-sms', function(e2){
        $('.message-item').each(function(e3){
            if(!$(this).hasClass('message-item-selected'))
            {
                $(this).addClass('message-item-selected');
            }
        })
    });
    $(document).on('click', '.uncheck-all-sms', function(e2){
        $('.message-item').each(function(e3){
            $(this).removeClass('message-item-selected');
        })
    });
    $(document).on('click', '.delete-sms', function(e2){
        $('.message-item-selected').each(function(e3){
            var modemID = $(this).attr('data-modem-id');
            var smsID = $(this).attr('data-sms-id');
            var storage = $(this).attr('data-storage');
            $.ajax({
                type:"POST",
                url:"api/delete/sms"+modemID,
                dataType:"json",
                data:{action:'delete-sms', modem_id:modemID, sms_id:smsID, storage:storage},
                success:function(data){
                    
                }
            });
            $(this).slideUp(240, function(e4){
                $(this).remove();
            });
        })
    });
});