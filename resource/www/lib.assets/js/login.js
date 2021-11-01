$(document).ready(function(evt){
    var next = './';
    var source = '';
    var $phone = $('#phone');
    var $password = $('#password');
    var $register = $('#register');
    var $agree = $('#agree');
    var $errorMessage = $('.error-message');
    var agree = false;

    $.ajax({
        type:'POST',
        url:'tool/date-sync',
        data:{action:'update', 'date':(new Date()).getTime()},
        dataType:'json',
        success:function(data){
            console.log(data);
        }
    });

    $phone.blur(function () {
        var phone = $phone.val().trim();
        if (!verifyPhone(phone)) {
            setErr($phone, $(this).data('text'), $errorMessage);
        } else {
            clearErr($phone);
        }
        });

        $phone.keydown(function (e) {
        if (e.which === 13) {
            $password.focus();
        }
    });

    $password.blur(function () {
        var password = $password.val();
        if (password.length < 6 || password.length > 20) {
            setErr($password, $(this).data('text'), $errorMessage);
        } else {
            clearErr($password);
        }
        });

        $password.keydown(function (e) {
        if (e.which === 13) {
            $cofPassword.focus();
        }
    });

    $agree.click(function () {
        if (agree) {
            $(this).removeClass('agree');
            $(this).find('.iconfont').removeClass('icon_check_tick');
            $(this).find('.iconfont').addClass('icon');
            agree = false;
        } else {
            $(this).addClass('agree');
            $(this).find('.iconfont').addClass('icon_check_tick');
            $(this).find('.iconfont').removeClass('icon');
            agree = true;
        }
    });

    $register.click(function () {
        var username = $phone.val().trim();
        var password = $password.val();

        if (password.length < 6 || password.length > 20) {
            setErr($password, $password.data('text'), $errorMessage);
            return;
        }
        clearErr($password);

        if (!agree) {
            showErr($agree.data('text'));
            return;
        }
        $errorMessage.css({'opacity': 0});

        $(this).attr('disabled', true);

        var regData = {
            username: username,
            password: password,
            next: next,
            source: source
        };
        register(regData);
    });

});

function register(regData) {
    showLoading();
    $.ajax({
        type:"POST",
        url:"login.html",
        dataType:"json",
        data:regData,
        success:function(res)
        {
            console.log(res);
            hideLoading();
            if (res.code === 0) 
            {
                console.log(res.payload.nextURL);
                window.location = res.payload.nextURL;
                return;
            } 
            $register.removeAttr('disabled');
            showErr(res.message);
        }
        
    });
}