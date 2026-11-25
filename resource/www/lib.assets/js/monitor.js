var mon = new monitor();
$(document).ready(function(e1){
    loadData(0);    
});

function loadData(level)
{
    $.ajax({
        type: 'GET',
        url: '/data/interface/list',
        dataType:'json',
        success: function(data) {
            renderData(data);
            if(data.admin.length == 0 && level == 0)
            {
                setTimeout(function(){
                    loadData(level + 1); 
                }, 1000);
            }
        }
    });
}

function renderData(data)
{                   
    mon.resetEmail();
    for(var i in data.email)
    {
        mon.addEmail(data.email[i]);
    }

    mon.resetModem();
    for(var i in data.modem)
    {
        mon.addModem(data.modem[i]);
    }

    mon.resetAdmin();
    for(var i in data.admin)
    {
        if(i < 2)
        {
            mon.addAdmin(data.admin[i]);
        }
    }
}

function monitor()
{
    this.modems = [];

    this.resetModem = function()
    {
        this.modems = [];
        $('#modem > *').each(function(e){
            $(this)[0].style.display = 'none';
        });
    }
    this.addModem = function(data)
    {
        this.modems.push(data);
        var id = '#modem_'+this.modems.length;
        if($(id).length)
        {
            $(id).get(0).style.display = 'block';
            $(id).get(0).setAttribute('data-id', data.id);
            $(id+" > text").get(0).firstChild.textContent = data.name;
        }
    }

    this.emails = [];

    this.resetEmail = function()
    {
        this.emails = [];
        $('#email > *').each(function(e){
            $(this)[0].style.display = 'none';
        });
    }
    this.addEmail = function(data)
    {
        this.emails.push(data);
        var id = '#email_'+this.emails.length;
        if($(id).length > 0)
        {
            $(id).get(0).style.display = 'block';
            $(id).get(0).setAttribute('data-id', data.id);
            $(id+" > text").get(0).firstChild.textContent = data.name;
        }
    }

    this.admins = [];

    this.resetAdmin = function()
    {
        this.admins = [];
        $('#admin > *').each(function(e){
            $(this)[0].style.display = 'none';
        });
    }
    this.addAdmin = function(data)
    {
        this.admins.push(data);
        var id = '#admin_'+this.admins.length;
        if($(id).length > 0)
        {
            $(id).get(0).style.display = 'block';
            $(id).get(0).setAttribute('data-id', data.id);
            $(id+" > text").get(0).firstChild.textContent = data.name;
        }
    }
}

function domToJSON(dom)
{
    var arr = [];
    $(dom).find(' > *').each(function(e){
        var obj = $(this)[0];
        var json = toJSON(obj);
        arr.push(json);
    });
    return arr;
}

function toJSON(obj)
{
    var dom = {};
    var attr = obj.getAttributeNames();
    dom.nodeName = obj.nodeName;
    if(attr.length > 0)
    {
        dom.attributes = {};
        for(var i in attr)
        {
            console.log(attr[i])
            dom.attributes[attr[i]] = obj.getAttribute(attr[i]);
        }
    }
    if($(obj).children().length > 0)
    {
        dom.children = domToJSON(obj);
    }
    else
    {
        dom.textNode = $(obj).text()
    }
    return dom;
}