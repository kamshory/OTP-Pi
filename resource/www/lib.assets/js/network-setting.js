$(document).ready(function (e) {
    $('.ipv4').each(function(e2){
        $(this).attr("placeholder", "xxx.xxx.xxx.xxx");
        $(this).inputmask({
            alias: "ip",
            greedy: false //The initial mask shown will be "" instead of "-____".
        });
    });
    $.ajax({
        type: "GET",
        url: "data/network-dhcp-setting/get",
        dataType: "json",
        success: function (data) {
            $('.dhcp [name="domainName"]').val(data.domainName);
            $('.dhcp [name="domainNameServers"]').val(data.domainNameServers.join(", "));
            $('.dhcp [name="ipRouter"]').val(data.ipRouter);
            $('.dhcp [name="netmask"]').val(data.netmask);
            $('.dhcp [name="subnetMask"]').val(data.subnetMask + "");
            $('.dhcp [name="domainNameServersAddress"]').val(data.domainNameServersAddress + "");
            $('.dhcp [name="defaultLeaseTime"]').val(data.defaultLeaseTime + "");
            $('.dhcp [name="maxLeaseTime"]').val(data.maxLeaseTime + "");
            var rangesObj = data.ranges;
            var obj1 = [];
            if(rangesObj.length > 0)
            {
                for(var i in rangesObj)
                {
                    obj1.push(rangesObj[i].begin+"-"+rangesObj[i].end)
                }
            }
            $('.dhcp input[name="ranges"]').val(obj1.join(", "));
        }
    });
    $.ajax({
        type: "GET",
        url: "data/network-wlan-setting/get",
        dataType: "json",
        success: function (data) {
            $('.wlan [name="essid"]').val(data.essid);
            $('.wlan [name="key"]').val(data.key);
            $('.wlan [name="keyMgmt"]').val(data.keyMgmt);
            $('.wlan [name="ipAddress"]').val(data.ipAddress);
            $('.wlan [name="prefix"]').val(data.prefix);
            $('.wlan [name="netmask"]').val(data.netmask);
            $('.wlan [name="gateway"]').val(data.gateway);
            $('.wlan [name="dns1"]').val(data.dns1);
        }
    });
    $.ajax({
        type: "GET",
        url: "data/network-ethernet-setting/get",
        dataType: "json",
        success: function (data) {
            $('.ethernet [name="ipAddress"]').val(data.ipAddress);
            $('.ethernet [name="prefix"]').val(data.prefix);
            $('.ethernet [name="netmask"]').val(data.netmask);
            $('.ethernet [name="gateway"]').val(data.gateway);
            $('.ethernet [name="dns1"]').val(data.dns1);
            $('.ethernet [name="dns2"]').val(data.dns2);
        }
    });
});