$(document).ready(function (e) {
    $('.ipv4').each(function(e2){
        $(this).attr("placeholder", "xxx.xxx.xxx.xxx");
        $(this).inputmask({
            alias: "ip",
            greedy: false //The initial mask shown will be "" instead of "-____".
        });
    });
    loadDHCPSetting("data/network-dhcp-setting/get");
    loadWLANSetting("data/network-wlan-setting/get");
    loadEthernetSetting("data/network-ethernet-setting/get");

    $(document).on('click', '#load_dhcp_setting', function(e){
        e.preventDefault();
        loadDHCPSetting("data/network-dhcp-sys-setting/get");
    });
    $(document).on('click', '#load_wlan_setting', function(e){
        e.preventDefault();
        loadWLANSetting("data/network-wlan-sys-setting/get");
    });
    $(document).on('click', '#load_eth_setting', function(e){
        e.preventDefault();
        loadEthernetSetting("data/network-ethernet-sys-setting/get");
    });
});

function loadDHCPSetting(url)
{
    $.ajax({
        type: "GET",
        url: url,
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
}

function loadWLANSetting(url)
{
    $.ajax({
        type: "GET",
        url: url,
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
}

function loadEthernetSetting(url)
{
    $.ajax({
        type: "GET",
        url: url,
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
}