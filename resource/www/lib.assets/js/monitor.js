var mon = false;
function handleIncommingMessage(message) {
    var jsonData = JSON.parse(message);
    var command = jsonData.command;
    if(command == 'sms-traffic')
    {
        var modemID = jsonData.data.modemID;
        var senderType = jsonData.data.senderType;
        
        mon = true;
        setModemActive(modemID, senderType, true);
        setTimeout(() => {
            setModemActive(modemID, senderType, false);
            mon = false;
        }, 2000);
    }
    if(command == 'broadcast-message')
    {

    }
    else if(command == 'server-info')
    {
        if(mon)
        {
            setTimeout(() => {
                buildModemData();
            }, 2000);                       
        }
        else
        {
            buildModemData();
        }
    }
}

function buildModemData()
{
    var selector = 'svg g#modem';
    $(selector).empty();
    var index = 0;

    var modemData = getModemData();
    for(var i in modemData)
    {
        if(modemData.hasOwnProperty(i))
        {
            var id = i;
            var name = modemData[i].name;
            if(modemData[i].active)
            {
                addModem(selector, index++, modemData[i]);
            }
        }
    }
    $('#monitor').html($('#monitor').html())
}
$(document).ready(function(e){
    buildModemData();
});

function addModem(selector, index, data)
{
    var id = data.id;
    var name = data.name;
    var connected = data.connected;
    var html = '';
    if(index == 0)
    {
        html = '<g id="'+id+'">\r\n'+
                '   <line x1="48" y1="320" x2="48" y2="280" stroke="#053310" />\r\n'+
                '   <rect x="26" y="261" width="30" height="30" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '   <rect x="19" y="161" width="44" height="110" rx="4" ry="4" fill="#f5f5f5" stroke="#555555" pointer-events="none"/>\r\n'+
                '   <rect x="31.5" y="279" width="5" height="5" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '   <rect x="44.5" y="279" width="5" height="5" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '   <g transform="translate(-0.5 -0.5)rotate(-90 28 238)"><text x="0" y="256" fill="#555555">'+name+'</text></g>\r\n'+
                '   <rect x="20" y="170" width="4" height="10" fill="#cccccc" />\r\n'+
                '   <g data-name="led" fill="none" fill-rule="evenodd" display="none">\r\n'+
                '   <rect id="ledbody'+index+'" x="20" y="170" width="4" height="10" fill="#000000" />\r\n'+
                '   <animate xlink:href="#ledbody'+index+'" attributeType="auto" attributeName="fill"/>\r\n'+
                '   <animate attributeType="CSS" attributeName="opacity" from="1" to="0" dur="0.1s" repeatCount="indefinite" />\r\n'+
                '   </g>\r\n';
        if(!connected)
        {
            html += '   <line x1="26" y1="180" x2="56" y2="260" stroke="#ee0000" />\r\n'+
            '   <line x1="56" y1="180" x2="26" y2="260" stroke="#ee0000" />\r\n';
        }
        html +=
        '</g>\r\n';
    }
    if(index == 1)
    {
        html = '<g id="'+id+'">\r\n'+
                '    <line x1="108" y1="300" x2="108" y2="280" stroke="#053310" />\r\n'+
                '    <line x1="58" y1="300" x2="108" y2="300" stroke="#053310" />\r\n'+
                '    <line x1="58" y1="320" x2="58" y2="300" stroke="#053310" />\r\n'+
                '    <rect x="86" y="261" width="30" height="30" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '    <rect x="79" y="161" width="44" height="110" rx="4" ry="4" fill="#f5f5f5" stroke="#555555" pointer-events="none"/>\r\n'+
                '    <rect x="91.5" y="279" width="5" height="5" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '    <rect x="104.5" y="279" width="5" height="5" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '    <g transform="translate(-0.5 -0.5)rotate(-90 88 238)"><text x="60" y="256" fill="#555555">'+name+'</text></g>\r\n'+
                '   <rect x="80" y="170" width="4" height="10" fill="#cccccc" />\r\n'+
                '   <g data-name="led" fill="none" fill-rule="evenodd" display="none">\r\n'+
                '   <rect id="ledbody'+index+'" x="80" y="170" width="4" height="10" fill="#000000" />\r\n'+
                '   <animate xlink:href="#ledbody'+index+'" attributeType="auto" attributeName="fill"/>\r\n'+
                '   <animate attributeType="CSS" attributeName="opacity" from="1" to="0" dur="0.1s" repeatCount="indefinite" />\r\n'+
                '   </g>\r\n';
        if(!connected)
        {
            html += '   <line x1="86" y1="180" x2="116" y2="260" stroke="#ee0000" />\r\n'+
            '   <line x1="116" y1="180" x2="86" y2="260" stroke="#ee0000" />\r\n';
        }
        html += '</g>\r\n';
    }
    if(index == 2)
    {
        html = '<g id="'+id+'">\r\n'+
                '    <line x1="168" y1="305" x2="168" y2="280" stroke="#053310" />\r\n'+
                '    <line x1="68" y1="305" x2="168" y2="305" stroke="#053310" />\r\n'+
                '    <line x1="68" y1="320" x2="68" y2="305" stroke="#053310" />\r\n'+
                '    <rect x="146" y="261" width="30" height="30" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '    <rect x="139" y="161" width="44" height="110" rx="4" ry="4" fill="#f5f5f5" stroke="#555555" pointer-events="none"/>\r\n'+
                '    <rect x="151.5" y="279" width="5" height="5" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '    <rect x="164.5" y="279" width="5" height="5" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '    <g transform="translate(-0.5 -0.5)rotate(-90 148 238)"><text x="120" y="256" fill="#555555">'+name+'</text></g>\r\n'+
                '   <rect x="140" y="170" width="4" height="10" fill="#cccccc" />\r\n'+
                '   <g data-name="led" fill="none" fill-rule="evenodd" display="none">\r\n'+
                '   <rect id="ledbody'+index+'" x="140" y="170" width="4" height="10" fill="#000000" />\r\n'+
                '   <animate xlink:href="#ledbody'+index+'" attributeType="auto" attributeName="fill"/>\r\n'+
                '   <animate attributeType="CSS" attributeName="opacity" from="1" to="0" dur="0.1s" repeatCount="indefinite" />\r\n'+
                '   </g>\r\n';
        if(!connected)
        {
            html += 
            '   <line x1="146" y1="180" x2="176" y2="260" stroke="#ee0000" />\r\n'+
            '   <line x1="176" y1="180" x2="146" y2="260" stroke="#ee0000" />\r\n';
        }
        html += '</g>\r\n';
    }
    if(index == 3)
    {
        html = '<g id="'+id+'">\r\n'+
                '    <line x1="228" y1="310" x2="228" y2="280" stroke="#053310" />\r\n'+
                '    <line x1="78" y1="310" x2="228" y2="310" stroke="#053310" />\r\n'+
                '    <line x1="78" y1="320" x2="78" y2="310" stroke="#053310" />\r\n'+
                '    <rect x="206" y="261" width="30" height="30" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '    <rect x="199" y="161" width="44" height="110" rx="4" ry="4" fill="#f5f5f5" stroke="#555555" pointer-events="none"/>\r\n'+
                '    <rect x="211.5" y="279" width="5" height="5" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '    <rect x="224.5" y="279" width="5" height="5" fill="#ffffff" stroke="#000000" pointer-events="none"/>\r\n'+
                '    <g transform="translate(-0.5 -0.5)rotate(-90 208 238)"><text x="180" y="256" fill="#555555">'+name+'</text></g>\r\n'+
                '   <rect x="200" y="170" width="4" height="10" fill="#cccccc" />\r\n'+
                '   <g data-name="led" fill="none" fill-rule="evenodd" display="none">\r\n'+
                '   <rect id="ledbody'+index+'" x="200" y="170" width="4" height="10" fill="#000000" />\r\n'+
                '   <animate xlink:href="#ledbody'+index+'" attributeType="auto" attributeName="fill"/>\r\n'+
                '   <animate attributeType="CSS" attributeName="opacity" from="1" to="0" dur="0.1s" repeatCount="indefinite" />\r\n'+
                '   </g>\r\n';
        if(!connected)
        {
            html += 
            '   <line x1="206" y1="180" x2="236" y2="260" stroke="#ee0000" />\r\n'+
            '   <line x1="236" y1="180" x2="206" y2="260" stroke="#ee0000" />\r\n';
        }
            html += 
            '</g>\r\n';
    }
    $(selector).append(html);
    
}
function setModemActive(id, senderType, status)
    {
        var selector = 'svg g#modem g#'+id;
        if(status)
        {
            $(selector).find('line').attr('stroke', '#f80000');
            $(selector).find('[data-name="led"]').attr('display', '');
            $('#phoneled').attr('display', '');
            $('#chipled').attr('display', '');
            $('#'+senderType).find('text').attr('fill', 'red');
            $('#'+senderType).find('rect').attr('stroke', 'red');
            $('#'+senderType).find('line').attr('stroke', 'red');
        }
        else
        {
            $(selector).find('line').attr('stroke', '#053310');
            $(selector).find('[data-name="led"]').attr('display', 'none');
            $('#phoneled').attr('display', 'none');
            $('#chipled').attr('display', 'none');
            $('#'+senderType).find('text').attr('fill', 'black');
            $('#'+senderType).find('rect').attr('stroke', 'black');
            $('#'+senderType).find('line').attr('stroke', 'black');
        }
    }
