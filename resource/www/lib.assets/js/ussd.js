var dialed = [];
function renderNumber()
{
var dialed2 = JSON.parse(JSON.stringify(dialed))
var shifted = false;
while(dialed2.length > 14)
{
    dialed2.shift();
    shifted = true;
}
var rendered = '';
if(shifted)
{
    rendered = '&#8230; '+dialed2.join('');
}
else
{
    rendered = dialed2.join('');
}
$('.dialed-number-text').html(rendered);
}
$(document).ready(function(e1){
$(document).on('click', '.number-button', function(e2){
    var val = $(this).attr('data-value');
    dialed.push(val);
    renderNumber();
});
$(document).on('click', '.dialed-number-delete button', function(e2){
    if(dialed.length > 0)
    {
    dialed.pop();
    }
    renderNumber();
});
$(document).on('click', '#phone', function(e){
    var dialedNumber = dialed.join('').toString();
    
    dialed = [];
    renderNumber();
    dialing(dialedNumber)
});
});

function dialing(number)
{
if(number.length > 0)
{
    $('.dialed-number-result').text(number)
    $('.ussd').css({'display':'block'});
    $('.ussd-response').css({'display':'none'});
    $('.ussd-progress').css({'display':'block'});

    $('.ussd-response-inner').html('&nbsp;');
    $.ajax({
    url:'api/ussd',
    type:"POST",
    data:{ussd:number, execute:'execute'},
    dataType:"json",
    success(data)
    {
        console.log(data);
        if(data.response_code == "0000")
        {
        $('.ussd-response-inner').html(data.data.message);
        }
        else
        {
        $('.ussd-response-inner').html(data.response_text);
        }
        
        $('.ussd-progress').css({'display':'none'});
        $('.ussd-response').css({'display':'block'});
    }
    })
}
}