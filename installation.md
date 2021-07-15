# Installation

Attach RTC DS3231 module to Raspberry Pi

```bash
yum install -y nano
yum install -y lm_sensors
yum install -y sysstat
yum install -y zip
yum install -y unzip


echo -e "dtparam=i2c_arm=on" >> /boot/config.txt
echo -e "dtoverlay=i2c-rtc,ds3231" >> /boot/config.txt
echo -e "i2c-dev" >> /etc/modules-load.d/i2c.conf
echo -e '[Unit]' > /usr/lib/systemd/system/rtc.service
echo -e 'Description=rtc' >> /usr/lib/systemd/system/rtc.service
echo -e '' >> /usr/lib/systemd/system/rtc.service
echo -e '[Service]' >> /usr/lib/systemd/system/rtc.service
echo -e 'ExecStart=/sbin/hwclock -s' >> /usr/lib/systemd/system/rtc.service
echo -e '' >> /usr/lib/systemd/system/rtc.service
echo -e '[Install]' >> /usr/lib/systemd/system/rtc.service
echo -e 'WantedBy=multi-user.target' >> /usr/lib/systemd/system/rtc.service
systemctl enable rtc.service
systemctl start rtc.service


yum install -y dhcp
echo -e 'ESSID=PlanetPOS' > /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'MODE=Ap' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'KEY_MGMT=WPA-PSK' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'MAC_ADDRESS_RANDOMIZATION=default' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'TYPE=Wireless' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'PROXY_METHOD=none' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'BROWSER_ONLY=no' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'BOOTPROTO=none' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'IPADDR=192.168.0.11' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'PREFIX=24' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'GATEWAY=192.168.0.1' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'DNS1=8.8.8.8' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'DEFROUTE=yes' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'IPV4_FAILURE_FATAL=no' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'IPV6INIT=yes' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'IPV6_AUTOCONF=yes' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'IPV6_DEFROUTE=yes' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'IPV6_FAILURE_FATAL=no' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'IPV6_ADDR_GEN_MODE=stable-privacy' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'NAME=wlan0' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'UUID=605a8783-c38b-4351-8f28-e82f99fdd0c6' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'ONBOOT=yes' >> /etc/sysconfig/network-scripts/ifcfg-wlan0
echo -e 'WPA_PSK=planetbiru' > /etc/sysconfig/network-scripts/keys-wlan0
echo '' > /etc/dhcp/dhcpd.conf
echo 'default-lease-time 600;' >> /etc/dhcp/dhcpd.conf
echo 'max-lease-time 7200;' >> /etc/dhcp/dhcpd.conf
echo 'authoritative;' >> /etc/dhcp/dhcpd.conf
echo '' >> /etc/dhcp/dhcpd.conf
echo 'subnet 192.168.0.0 netmask 255.255.255.0 {' >> /etc/dhcp/dhcpd.conf
echo '    option routers                  192.168.0.1;' >> /etc/dhcp/dhcpd.conf
echo '    option subnet-mask              255.255.255.0;' >> /etc/dhcp/dhcpd.conf
echo '    option broadcast-address        192.168.0.255;' >> /etc/dhcp/dhcpd.conf
echo '    range 192.168.0.40 192.168.0.254;' >> /etc/dhcp/dhcpd.conf
echo '}' >> /etc/dhcp/dhcpd.conf
echo -e '[Unit]' > /usr/lib/systemd/system/dhcp.service
echo -e 'Description=DHCPv4 Server Daemon' >> /usr/lib/systemd/system/dhcp.service
echo -e 'Documentation=man:dhcpd(8) man:dhcpd.conf(5)' >> /usr/lib/systemd/system/dhcp.service
echo -e 'Wants=network-online.target' >> /usr/lib/systemd/system/dhcp.service
echo -e 'After=network-online.target' >> /usr/lib/systemd/system/dhcp.service
echo -e 'After=time-sync.target' >> /usr/lib/systemd/system/dhcp.service
echo -e '' >> /usr/lib/systemd/system/dhcp.service
echo -e '[Service]' >> /usr/lib/systemd/system/dhcp.service
echo -e 'Type=notify' >> /usr/lib/systemd/system/dhcp.service
echo -e 'ExecStart=/usr/sbin/dhcpd -f -cf /etc/dhcp/dhcpd.conf -user dhcpd -group dhcpd --no-pid wlan0' >> /usr/lib/systemd/system/dhcp.service
echo -e '' >> /usr/lib/systemd/system/dhcp.service
echo -e '[Install]' >> /usr/lib/systemd/system/dhcp.service
echo -e 'WantedBy=multi-user.target' >> /usr/lib/systemd/system/dhcp.service
systemctl enable dhcpd.service
systemctl start dhcpd.service


echo -e 'TYPE="Ethernet"' > /etc/sysconfig/ifcfg-eth0
echo -e 'BOOTPROTO=none' >> /etc/sysconfig/ifcfg-eth0
echo -e 'NM_CONTROLLED="yes"' >> /etc/sysconfig/ifcfg-eth0
echo -e 'DEFROUTE="yes"' >> /etc/sysconfig/ifcfg-eth0
echo -e 'NAME="eth0"' >> /etc/sysconfig/ifcfg-eth0
echo -e 'UUID="a5ae9a6c-3951-4e8a-b99d-a4ea5dc33bf1"' >> /etc/sysconfig/ifcfg-eth0
echo -e 'ONBOOT="yes"' >> /etc/sysconfig/ifcfg-eth0
echo -e 'DNS1=8.8.8.8' >> /etc/sysconfig/ifcfg-eth0
echo -e 'IPV4_FAILURE_FATAL=no' >> /etc/sysconfig/ifcfg-eth0
echo -e 'IPV6INIT=no' >> /etc/sysconfig/ifcfg-eth0
echo -e 'IPADDR=192.168.0.11' >> /etc/sysconfig/ifcfg-eth0
echo -e 'PREFIX=24' >> /etc/sysconfig/ifcfg-eth0
echo -e 'GATEWAY=192.168.0.1' >> /etc/sysconfig/ifcfg-eth0
echo -e 'NETMASK=255.255.255.0' >> /etc/sysconfig/ifcfg-eth0
echo -e 'DNS2=8.8.4.4' >> /etc/sysconfig/ifcfg-eth0


yum install -y wvdial


yum install -y java-1.8.0-openjdk


firewall-cmd --permanent --add-port=8888/tcp 
firewall-cmd --permanent --add-port=8889/tcp 
firewall-cmd --permanent --add-port=80/tcp 
firewall-cmd --permanent --add-port=443/tcp 
firewall-cmd --reload


echo -e '#!/bin/sh' > /var/otp-pi/start.sh
echo -e '' >> /var/otp-pi/start.sh
echo -e 'cd /var/otp-pi' >> /var/otp-pi/start.sh
echo -e '/bin/java -jar /var/otp-pi/otp-pi.jar --start' >> /var/otp-pi/start.sh

echo -e '#!/bin/sh' > /var/otp-pi/restart.sh
echo -e '' >> /var/otp-pi/restart.sh
echo -e 'cd /var/otp-pi' >> /var/otp-pi/restart.sh
echo -e '/bin/java -jar /var/otp-pi/otp-pi.jar --restart' >> /var/otp-pi/restart.sh

echo -e '#!/bin/sh' > /var/otp-pi/stop.sh
echo -e '' >> /var/otp-pi/stop.sh
echo -e 'cd /var/otp-pi' >> /var/otp-pi/stop.sh
echo -e '/bin/java -jar /var/otp-pi/otp-pi.jar --stop' >> /var/otp-pi/stop.sh

echo -e '[Unit]' > /usr/lib/systemd/system/otp-pi.service
echo -e 'Description=otp-pi' >> /usr/lib/systemd/system/otp-pi.service
echo -e '' >> /usr/lib/systemd/system/otp-pi.service
echo -e '[Service]' >> /usr/lib/systemd/system/otp-pi.service
echo -e 'SuccessExitStatus=143' >> /usr/lib/systemd/system/otp-pi.service
echo -e 'User=root' >> /usr/lib/systemd/system/otp-pi.service
echo -e 'Type=simple' >> /usr/lib/systemd/system/otp-pi.service
echo -e 'ExecStart=/bin/bash /var/otp-pi/start.sh' >> /usr/lib/systemd/system/otp-pi.service
echo -e 'ExecReload=/bin/bash /var/otp-pi/restart.sh' >> /usr/lib/systemd/system/otp-pi.service
echo -e 'ExecStop=/bin/bash /var/otp-pi/stop.sh' >> /usr/lib/systemd/system/otp-pi.service
echo -e '' >> /usr/lib/systemd/system/otp-pi.service
echo -e '[Install]' >> /usr/lib/systemd/system/otp-pi.service
echo -e 'WantedBy=multi-user.target' >> /usr/lib/systemd/system/otp-pi.service
systemctl daemon-reload
systemctl enable otp-pi.service
systemctl start otp-pi.service



```




