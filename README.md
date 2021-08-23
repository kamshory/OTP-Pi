# OTP-Pi

![OTP-Pi](https://raw.githubusercontent.com/kamshory/OTP-Pi/main/otp-pi.png)

OTP or `One Time Password` is a one-time use password that has a certain validity period. Generally the validity period is made very short and only gives the opportunity to the recipient to enter it into the application and send it to the application server that requires it. OTP is strictly confidential so the `clear text` of the OTP should not be stored by any party. In fact, the application server only stores the `hash` or `token` that matches the OTP. `Clear text` is only generated and then sent to the recipient. In other words, `clear text` is only known to the OTP recipient.

The most popular OTPs are sent via SMS or Short Message Service. The use of SMS has the following advantages:

1. Can only be received by the device where the SIM card of the recipient number is installed. This of course has to do with `What you have` in `multifactor authentication`
2. Easily readable on almost all makes and models of mobile phone devices
3. Generally the device immediately notifies the presence of an incoming message without requiring user action
4. On smart cellular phone devices, OTP user applications can directly read incoming messages if permitted by the user. Thus, the application can directly verify the OTP without requiring manual action from the user. This will save time and reduce errors
5. Does not require the recipient's credit so that the recipient of the message does not have to pay anything to be able to receive the message
6. Has a very wide range
7. SMS receiving devices are available in various price classes so that they can be reached by almost all people

Of the many advantages above, it turns out that SMS has limitations. SMS must be sent through a telecommunications operator legally registered in the sending country. SMS can be sent using a GSM device connected to a telecommunications operator. The cheapest devices for sending SMS are cell phones and modems. In addition, the sender must pay a service fee to the telecommunications operator. This fee will be shared to all operators involved in sending SMS.

Another way to send SMS is by working directly with telecommunications operators or using third party services. Cooperation with telecommunications operators is certainly not easy. In addition to having a legal entity, the costs required are of course not small. In addition, the volume of SMS sending is also a consideration for whether the cooperation is accepted or not by the telecommunications operator. The use of third party services is another option. The safety factor is of course a consideration. The service provider must of course be trusted to maintain the confidentiality of the OTP sent.

For small-scale companies who want to build their own OTP delivery system, they can use a variety of applications that are available for free or paid in the market. Some considerations in choosing an OTP application are as follows:

1. Delivery speed
2. Security/confidentiality
3. Ease of installation and integration
4. Initial costs and operational costs

Applications that use a database to connect between the receiving side of the message and the GSM modem are of course not secure. The data in the database can be read by the administrator. In fact, administrators can request OTP when the user is not aware of it and is not transacting.

Applications that require the use of a public IP address of course do not provide flexibility for users. Companies or individuals who do not subscribe to the internet with a public IP cannot use the application. Users need to place the server on a network with a public IP.

Applications that run on desktops and laptops of course require high investment and operational costs. The laptop or desktop that is used must operate 24 hours a day and 7 days a week. Of course, the electricity used is not small.

OTP-Pi answers all of the above challenges. With a very cheap device, users can have an SMS gateway that provides many features and can be operated at a very low cost.

OTP-Pi is a server for sending SMS via HTTP, WebSocket and Message Broker protocols. Users can install OTP-Pi on the server with a static IP address that can be accessed by clients who will send SMS. In addition, users can also install OTP-Pi on servers with dynamic IP addresses. This server then accesses a websocket server, RabbitMQ server or Mosquitto server. OTP-Pi acts as a consumer that will send all SMS it receives.

# System Requirement

OTP-Pi requires Raspberry Pi 3 Model B or higher. Minimum RAM is 1 GB and SD Card minimum is 16 GB. 32GB SD Card is recommended. Power supply 5 Volt 3 Ampre to prevent the Raspberry Pi from operating with under voltage.

# Feature


## Multiple Device

Modem is a list of modems installed on the OTP-Pi. Modems are named based on the make and model of the device and the connection used. The modem can be turned on and off at any time. An inactive modem will not be used to send SMS even if it is physically attached to the OTP-Pi and receiving power.

OTP-Pi can use multiple modems at once. Sending SMS will use the Round-Robin algorithm where all active modems will be used in rotation.

## Prefix-Based Routing

In reality, cost efficiency becomes very important for users. The cost of sending SMS should be reduced as much as possible. Telecommunications operators usually apply lower fees when sending SMS to subscriber numbers of the same telecommunications operator. On the other hand, the cost of sending SMS will be higher when sending SMS to customer numbers from other operators.

OTP-Pi allows users to set up SMS sending. For example: a user uses 4 modems with 4 different SIM cards from different telecommunication operators.

1. Modem 1 with SIM Card from Telecommunications Operator 1
2. Modem 2 with SIM Card from Telecommunication Operator 2
3. Modem 3 with SIM Card from Telecommunication Operator 3
4. Modem 4 with SIM Card from Telecommunications Operator 4

Telecommunications Operator 1 applies a fee of IDR 50 for numbers with prefixes `62871` and `62872` and applies a fee of IDR 350 for numbers other than that.
Telecommunications Operator 2 applies a fee of Rp 100 for numbers with prefixes `62835`, `62856`, and `62837` and applies a fee of Rp 350 for numbers other than that.
Telecommunications Operator 3 applies a fee of Rp. 60 for numbers with the prefix `62845` and applies a fee of Rp. 250 for numbers other than that.
Telecommunications Operator 4 applies a fee of Rp 90 for numbers with prefixes `62848` and `62849` and applies a fee of Rp 200 for numbers other than that.

From the case above, the lowest cost for other operators is Rp. 200. Users can set modem 4 as the default modem. All SMS other than the prefixes `62871`, `62872`, `62835`, `62856`, `62837` and `62845` will use this modem and are sent via Telecommunications Operator 4. All SMS for numbers with prefixes `62871` and ` 62872` uses modem 1 and is sent via Telecommunication Operator 1. All SMS for numbers with prefix `62835`, `62856`, and `62837` use modem 2 and are sent via Telecommunication Operator 2. All SMS for numbers with prefix `62845` use modem 3 and sent via Telecommunication Operator 3. Thus, the cost of sending SMS will be reduced.

Users can use 2 or more SIM cards from the same operator. The modem will be used interchangeably with the Round-Robin algorithm when OTP-Pi sends SMS to numbers with the same prefix.

The cost of sending SMS will be even lower when the user takes advantage of the promo from the telecommunications operator in question. Some operators will apply very low SMS sending fees after the user sends several SMS with certain conditions.

Prefix setting using MSISDN. Thus, in the example above, when a user in Indonesia sets the prefix 0871, it stores 62871. Thus, the user must use the prefix length of 5 instead of 4.

## USSD Support

OTP-Pi allows users to perform USSD commands on individual modems. The USSD command, of course, depends on each cellular operator used on each SIM card installed on each modem.

## Manual SMS

Manual SMS is used to test whether each modem can send SMS.

## Internet Mobile

The OTP-Pi can connect to the internet using a mobile modem. Thus, users do not need to connect the OTP-Pi with wired or optical internet. It provides an alternative for users in choosing an internet connection.

## Administrator Setting

Administrator Settings is a menu to configure administrator. New OTP-Pi devices don't have an administrator yet. The user must create an administrator first before using it. Please enter the OTP-Pi access point according to the SSID and password listed on the brochure and scan the QR Code on the brochure using a smartphone.

The default address of web management is http://192.168.0.11:8888

**Username**
Username is administrator identifier when logging in to OTP-Pi

**Password**
Username is administrator's security when logging into OTP-Pi

**Phone Number**
The phone number can be used if the administrator forgets the password. Password will be sent via SMS. Of course this can only be done when the OTP-Pi has been configured correctly.

**E-mail**
Email can be used if the administrator forgets the password. Password will be sent via email. Of course this can only be done when the OTP-Pi has been configured correctly.

## API Settings

API Setting is a REST API configuration for sending SMS.
1. **HTTP Port** is the server port for HTTP
2. **Enable HTTP** is a setting to enable or disable HTTP port
3. **HTTPS Port** is the server port for HTTPS
4. **Enable HTTPS** is a setting to enable or disable HTTPS port
5. **Message Path** is the path to send SMS and email
6. **Blocking Path** is a path to block a phone number so that OTP-Pi does not send SMS to that number
7. **Unblocking Path** is a path to unblock a phone number so that OTP-Pi can send SMS to that number again

## API Users

API User is an account that sends SMS via REST API.

**Username**
Username is the sender identifier when sending SMS to OTP-Pi

**Password**
Username is the sender's security when sending SMS to OTP-Pi

**Phone Number**
Phone number is contact information in the form of phone numbers from API users

**E-mail**
Email is contact information in the form of email addresses of API users

## Subscriber Settings

OTP-Pi provides an option if this device is installed on a mobile internet network or on a network where the sending device may not be able to reach the address of the OTP-Pi. Users can send OTP with message broker as follows:

1. WSMessageBroker (WebSocket)
2. RabbitMQ (AMQP)
3. Mosquitto (MQTT)

**WSMessageBroker**

| Field | Description |
| ----- | ----------- |
| Enable | Enable or disable WSMessageBroker |
| SSL | SSL connection |
| Host | WSMessageBroker host name or IP address |
| Port | Port number |
| Path | Context path |
| Username | Username on basic authorzation |
| Password | Password on basic authorzation |
| Topic | Topic of subscribsion | 
| Timeout | Request time out |
| Reconnect Delay | Delay to reconnect |
| Refresh Connection | Refresh WebSocket connection |

**RabbitMQ**

| Field | Description |
| ----- | ----------- |
| Enable | Enable or disable RabbitMQ |
| SSL | SSL connection |
| Host | RabbitMQ host name or IP address |
| Port | Port number |
| Username | Username on basic authorzation |
| Password | Password on basic authorzation |
| Queue | Topic of subscribsion | 
| Timeout | Request time out |
| Refresh Connection | Refresh RabbitMQ connection |

**Mosquitto**

| Field | Description |
| ----- | ----------- |
| Enable | Enable or disable Mosquitto |
| SSL | SSL connection |
| Host | Mosquitto host name or IP address |
| Port | Port number |
| Client ID | Client ID |
| Username | Username on basic authorzation |
| Password | Password on basic authorzation |
| Topic | Topic of subscribsion | 
| Timeout | Request time out |
| Refresh Connection | Refresh Mosquitto connection |


## SMS Setting

SMS Setting is the configuration of sending SMS by OTP-Pi.

| Field | Description |
| ----- | ----------- |
| Country Code | Phone country code (numeric). See https://countrycode.org/ |
| Recipient Prefix Length | For Indonesia, use 5 |
| Log Outgoing SMS | Flag to log outgoing SMS |
| Monitor SMS Traffic | Flag to monitor SMS traffic |


## Modem Setting

| Field | Description |
| ----- | ----------- |
| Name | Modem Name |
| Port | Serial port used |
| SMS Center | SMS center |
| IMEI | Modem IMEI |
| MSISDN | MSISDN of the SIM Card used |
| IMSI | IMSI of the SIM Card used |
| Recipient Prefix | Recipient prefix that will receive SMS from this modem. See _Prefix-Based Routing_ secton |
| SIM Card PIN | SIM Card PIN if exists |
| Baud Rate | Modem baud rate |
| Parity Bit | Modem parity bit |
| Start Bits | Modem start bits |
| Stop Bits | Modem stop bits |
| Internet Access | Use modem to connect to the internet |
| APN | Access Point Name |
| APN Username | Username of the APN to connect to the internet |
| APN Password | Password of the APN to connect to the internet |
| Dial Number | Number to be dialed to connect to the internet |
| Init Dial 1 | Initial AT command to be executed to connect to the internet |
| Init Dial 2 | Initial AT command to be executed to connect to the internet |
| Init Dial 3 | Initial AT command to be executed to connect to the internet |
| Init Dial 4 | Initial AT command to be executed to connect to the internet |
| Init Dial 5 | Initial AT command to be executed to connect to the internet |
| Dial Command | Command to be executed to connect to the internet |
| Send SMS from API | Flag that the modem will be used to send SMS from API or not. If it set to `false`, modem will not send incomming SMS from API but user can send SMS manualy from the management web. |
| Default Modem | Flag that the modem is default or not. The default modem will be used to send SMS that the prefix is not exists on `Recipient Prefix` of the other modems. |
| Active | Flag for the modem that will be used or not |


### APN Setting for Indonesian Telco

| Operator | APN | Username | Password | Dial UP |
| -- | -- | -- | -- | -- |
| Telkmonsel | internet | | | *99# |
| XL | internet | | | *99# |
| Three | 3data | 3data | 3data | *99# |
| Smartfren | smart | smart | smart | #777 |
| Indosat Ooredoo | indosatgprs | | | *99# |
| Axis | axis | axis | 123456 | *99# |

## Blocking List

Blocking list is a list of blocked phone numbers. Blocked numbers will not receive SMS from any modem. This list can be added and whitened via the REST API, abbitMQ, Mosquitto and WebSocket. This list can also be manually added, whitened, or deleted via the web admin.

## Multiple Email Account

OTP-Pi supports multiple email accounts. Email account is a configuration that contains SMTP Server address, SMTP server port, username, password, and other configurations.

SMTP Server is used to send OTP to email addresses and can be used to reset passwords.

The use of multiple email accounts is recommended because the number of emails sent by each email account will decrease. For example: an SMTP server limits 500 emails per day. Using 20 email accounts, OTP-Pi can send up to 10,000 emails per day.

To use Gmail SMTP, use the following configuration:

**Use Port 587**

| Field | Value |
| --- | ------ |
| SMTP Host | smtp.gmail.com |
| SMTP Port | 587 |
| SSL Enable | No |
| Start TLS Before Send | Yes |
| Authentication | Yes |
| Sender Account | Gmail Account
| Sender Password | Gmail Password |
| Active | Yes |

**Use Port 465**

| Field | Value |
| --- | ------ |
| SMTP Host | smtp.gmail.com |
| SMTP Port | 465 |
| SSL Enable | Yes |
| Start TLS Before Send | Yes |
| Authentication | Yes |
| Sender Account | Gmail Account
| Sender Password | Gmail Password |
| Active | Yes |

## Local SMTP Server

The OTP-Pi is equipped with a local SMTP server for sending email. With this local SMTP server, users can send email without needing an external SMTP server. However, a local SMTP server is not recommended because the destination mail server may block email sent from the local SMTP server.

## DDNS Record

DDNS Records are records for dynamic DNS settings. DDNS or Dymanic Domain Name System is a DNS setting mechanism that is done repeatedly because the public IP address of the server is always changing.

OTP-Pi provides DDNS setup using DDNS vendor. Some of the supported DDNS vendors are as follows:

1. Cloudflare - https://www.cloudflare.com/
2. NoIP - https://www.noip.com/
3. Dynu Dyn DNS - https://www.dynu.com/
4. Free DNS Afraid - https://freedns.afraid.org/

## Network Setting 

Network Setting is a configuration to manage network from OTP-Pi. OTP-Pi is equipped with an access point so that it can be accessed directly using a laptop or cellphone without having to plug it into a wired network. This will make it easier for users because the user's LAN network configuration is different. Users simply set the IP address on the ethernet network according to the LAN network configuration used.

### DHCP

Konfigurasi DHCP akan mengatur DHCP pada akses poin OTP-Pi.

| Field | Value |
| --- | ------ |
| Domain Name | Domain name of DHCP |
| Domain Name Servers | Domain name servers of DHCP |
| Router Address | Router address of DHCP |
| Netmask | Netmask of DHCP |
| Subnetmask | Subnetmask of DHCP |
| Domain Name Servers Address | Domain Name Servers Address of DHCP |
| Default Lease Time | Default lease time of the clients |
| Maximum Lease Time | Maximum lease time of the clients |
| Range | Allocated IP address range | 

### Wireless LAN

Wireless LAN configuration will set the IP address on the OTP-Pi wireless network. The default IP address of the OTP-Pi is 192.168.0.11

| Field | Value |
| --- | ------ |
| SSID Name | SSID name |
| SSID Password | SSID password |
| Key Management | Key management |
| IP Address | IP address of the device if client connect via Wifi |
| Prefix | Nework prefix |
| Netmask | Netmask |
| Gateway | Gateway |
| DNS | DNS |

### Ethernet

The Ethernet configuration will set the ethernet IP address on the OTP-Pi.

| Field | Value |
| --- | ------ |
| IP Address | IP address of the device if client connect via Ethernet |
| Prefix | Nework prefix |
| Netmask | Netmask |
| Gateway | Gateway |
| DNS1 | DNS 1 |
| DNS2 | DNS 2 |

## Firewalls

OTP-Pi can open and close ports of the operating system used by SMS Broker. Closing this port will create a firewall at the operating system layer. In this case, if the port is closed, the equest will not reach the application.

## Monitor

The monitor can be used to view the activity of the modem installed on the OTP-Pi. When the OTP-Pi sends an SMS, the OTP-Pi will show where the SMS request came from and which modem was used. In addition, the status of the modem will be shown so that it will be seen which modems are connected correctly.

## Cloudflare

The Cloudflare module is a module to manage the Cloudflare account used.

## NoIP

The NoIP module is a module to manage the NoIP account used.

## Dynu

The Dynu module is a module for managing the Dynu Dyn DNS account used.

## Afraid

The Afraid module is a module to manage the Free DNS Afraid account used.

## Real Time Clock

The Real Time Clock or RTC module is required to keep the device time when the device is turned off and not connected to the internet. The device must use the correct time so that the cookies sent by the device are not deleted by the browser due to expiration so that the administrator can manage the device including making settings. In addition, the device needs to record the time when an error occurred so that the error log can be read by the user for mitigation.

![OTP-Pi](https://raw.githubusercontent.com/kamshory/OTP-Pi/main/ds3231.png)

The Real Time Clock module is built into the device and is programmed to work as intended. If the device is off for a long time causing the RTC battery to run out, the device can adjust the time according to the browser that accesses it regardless of the time zone used. Furthermore, the administrator can set the device time if needed.

## Reset Device

In case the user cannot access the web administrator, either because he forgot his password, or because the network configuration is messed up, the user can reset the device. Reset the device is done by plugging in a flash disk containing the following files:

`/optb/reset.txt`

Nama direktori, nama file dan ekstensi harus ditulis dengan benar dalam huruf kecil (lower case). File `/optb/reset.txt` harus beriksi tesk sebagai berikut:

```txt
????????????????
```

Reset device akan melakukan hal-hal sebagai berikut:

1. Mengembalikan konfigurasi DHCP ke awal
2. Mengembalikan konfigurasi Wireless LAN ke awal
3. Mengembalikan konfigurasi LAN ke awal
4. Menghapus semua akun administrator

Untuk melakukan reset device, lakukan langkah-langkah sebagai berikut:

1. Persiapkan sebuah flash disk dengan file seperti dijelaskan di atas
2. Cabut semua modem yang terpasang pada perangkat
3. Pasang flash disk di sembarang port USB perangkat
4. Cabut catu daya (power supplay) ke perangkat
5. Pasang kembali catu daya (power supplay) ke perangkat
6. Tunggu hingga 10 menit
7. Cabut flash disk dari perangkat
8. Pasang kembali catu daya (power supplay) ke perangkat
9. Masuk ke akses poin dengan SSID dan password seperti tertera pada brosur menggunakan smartphone atau PC
10. Buka halaman 192.168.0.8888 dengan browser menggunakan smartphone atau PC yang telah terhubung ke akses poin di atas
11. Login ke halaman yang disediakan
12. Buat administrator baru untuk perangkat

# Topology

Baik WebSocket maupun Message Broker menggunakan sebuah channel yang dapat diseting dari kedua sisi (pengirim dan penerima).

Untuk menggunakan WebSocket, silakan gunakan library WSMessageBrocker dengan link https://github.com/kamshory/Messenger atau anda dapat membuatnya sendiri. 

Untuk menggunakan RabbitMQ, silakan buka link https://www.rabbitmq.com/

Untuk menggunakan Mosquitto, silakan buka link https://mosquitto.org/


![OTP-Pi Topology](https://raw.githubusercontent.com/kamshory/OTP-Broker/main/src/main/resources/static/www/lib.assets/images/topology.png)



### Sekenario 1 - OTP-Pi Dapat Diakses App Server

Pada skenario ini, App Server dapat langsung mengirimkan OTP ke OTP-Pi melalui HTTP.

![OTP-Pi Topology Skenario 1](https://raw.githubusercontent.com/kamshory/OTP-Broker/main/src/main/resources/static/www/lib.assets/images/topology-1.png)

Pengguna dapat menggunakan sebuah domain murah dan menggunakan Dynamic Domain Name System gratis. Dengan penggunaan port forwarding pada router, OTP-Pi dapat diakses dari manapun dengan menggunakan domain atau subdomain. Dalam skenario ini, pengguna membutuhkan:

1. OTP-Pi
2. Koneksi internet fix dengan IP public (statis atau dinamis)
3. Router yang dapat melakukan port forwarding
4. Domain yang name servernya dapat diatur
5. Layanan Dynamic DNS (gratis maupun berbayar)

**1. REST API**

**Send SMS Request**

```http
POST /api/sms HTTP/1.1
Host: sub.domain.tld
Connection: close
User-agent: KSPS
Content-type: application/json
Content-length: 157
Authorization: Basic dXNlcjpwYXNzd29yZA==

{
	"command": "send-sms",
	"data": {
		"date_time": 1629685778,
		"id": 123456,
		"receiver": "08126666666",
		"message": "OTP Anda adalah 1234"
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.id | String | ID SMS |
| `data`.receiver | String | Nomor MSISDN penerima |
| `data`.message| String | Pesan SMS |

**Send Email Request**

```http
POST /api/sms HTTP/1.1
Host: sub.domain.tld
Connection: close
User-agent: KSPS
Content-type: application/json
Content-length: 166
Authorization: Basic dXNlcjpwYXNzd29yZA==

{
	"command": "send-email",
	"data": {
		"date_time": 1629685778,
		"id": 123456,
		"receiver": "someone@domain.tld",
		"message": "OTP Anda adalah 1234"
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.id | String | ID SMS |
| `data`.receiver | String | Alamat email penerima |
| `data`.message| String | Pesan SMS |


**Block Number Request**

```http
POST /api/block HTTP/1.1
Host: sub.domain.tld
Connection: close
User-agent: KSPS
Content-type: application/json
Content-length: 107
Authorization: Basic dXNlcjpwYXNzd29yZA==

{
	"command": "block-msisdn",
	"data": {
		"date_time": 1629685778,
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.msisdn | String | Nomor MSISDN yang akan diblokir |

**Unblock Number Request**

```http
POST /api/unblock HTTP/1.1
Host: sub.domain.tld
Connection: close
User-agent: KSPS
Content-type: application/json
Content-length: 109
Authorization: Basic dXNlcjpwYXNzd29yZA==

{
	"command": "unblock-msisdn",
	"data": {
		"date_time": 1629685778,
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.receiver | String | Nomor MSISDN yang akan dibuka blokir |

### Sekenario 2 - OTP-Pi Tidak Dapat Diakses App Server

Pada skenario ini, App Server dapat mengirimkan OTP ke RabbitMQ Server, Mosquitto Server atau WSMessageBroker. WSMessageBroker menggunakan protokol WebSoket dan Basic Authentication. Baik App Server maupun OTP-Pi bertindak sebagai client dari WSMessageBroker.

App Server bertindak sebagai publisher dan OTP-Pi menjadi consumer dari RabbitMQ Server, Mosquitto Server dan WSMessageBroker. Keduanya harus menggunakan channel yang sama agar semua OTP yang dikirimkan oleh App Server dapat diterima oleh OTP-Pi.

![OTP-Pi Topology Skenario 2](https://raw.githubusercontent.com/kamshory/OTP-Broker/main/src/main/resources/static/www/lib.assets/images/topology-2.png)

Dari kedua skenario di atas, OTP-Pi akan mengirmkan SMS menggunakan modem GSM yang terpasang secara fisik pada perangkat OTP-Pi. Pengguna dapat menggunakan salah satu dari RabbitMQ Server, Mosquitto Server atau WSMessageBroker dan dapat pula menggunakan keduanya dalam waktu bersamaan. Akan tetapi, apabila App Server mengirimkan sebuah OTP yang sama ke RabbitMQ Server, Mosquitto Server dan WSMessageBroker, maka OTP-Pi akan mengirimkan SMS tersebut dua kali ke nomor penerima.

Pada skenario ini, pengguna tidak memerlukan IP public. Pengguna hanya memerlukan:

1. OTP-Pi
2. Koneksi internet (tidak memerlukan IP public dan port forwarding)
3. Server RabbitMQ, Mosquitto atau WSMessageBroker

**1. RabbitMQ**

**Send SMS Request**

```json
{
	"command": "send-sms",
	"data": {
		"date_time": 1629685778,
		"id": 123456,
		"receiver": "08126666666",
		"message": "OTP Anda adalah 1234"
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.id | String | ID SMS |
| `data`.receiver | String | Nomor MSISDN penerima |
| `data`.message| String | Pesan SMS |

**Send Email Request**

```json
{
	"command":"send-sms",
	"data": {
		"date_time": 1629685778,
		"id": 123456,
		"receiver": "someone@domain.tld",
		"message": "OTP Anda adalah 1234"
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.id | String | ID SMS |
| `data`.receiver | String | Alamat email penerima |
| `data`.message| String | Pesan SMS |

**Block Number Request**

```json
{
	"command": "block-msisdn",
	"data": {
		"date_time": 1629685778,
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.receiver | String | Nomor MSISDN yang akan diblokir |

**Unblock Number Request**

```json
{
	"command": "unblock-msisdn",
	"data":{
		"date_time": 1629685778,
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.receiver | String | Nomor MSISDN yang akan dibuka blokir |

**2. Mosquitto**

**Send SMS Request**

```json
{
	"command":"send-sms",
	"data": {
		"date_time": 1629685778,
		"id": 123456,
		"receiver": "08126666666",
		"message": "OTP Anda adalah 1234"
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.id | String | ID SMS |
| `data`.receiver | String | Nomor MSISDN penerima |
| `data`.message| String | Pesan SMS |

**Send Email Request**

```json
{
	"command": "send-sms",
	"data": {
		"date_time": 1629685778,
		"id": 123456,
		"receiver": "someone@domain.tld",
		"message": "OTP Anda adalah 1234"
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.id | String | ID SMS |
| `data`.receiver | String | Alamat email penerima |
| `data`.message| String | Pesan SMS |

**Block Number Request**

```json
{
	"command": "block-msisdn",
	"data": {
		"date_time": 1629685778,
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.receiver | String | Nomor MSISDN yang akan diblokir |

**Unblock Number Request**

```json
{
	"command": "unblock-msisdn",
	"data": {
		"date_time": 1629685778,
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.receiver | String | Nomor MSISDN yang akan dibuka blokir |

**3. WSMessageBroker**

**Send SMS Request**

```json
{
	"command":"send-sms",
	"data": {	
		"date_time": 1629685778,
		"id": 123456,
		"receiver": "08126666666",
		"message": "OTP Anda adalah 1234"
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.id | String | ID SMS |
| `data`.receiver | String | Nomor MSISDN penerima |
| `data`.message| String | Pesan SMS |

**Send Email Request**

```json
{
	"command": "send-sms",
	"data": {
		"date_time": 1629685778,
		"id": 123456,
		"receiver": "someone@domain.tld",
		"message": "OTP Anda adalah 1234"
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.id | String | ID SMS |
| `data`.receiver | String | Alamat email penerima |
| `data`.message| String | Pesan SMS |

**Block Number Request**

```json
{
	"command": "block-msisdn",
	"data": {
		"date_time": 1629685778,
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.receiver | String | Nomor MSISDN yang akan diblokir |

**Unblock Number Request**

```json
{

	"command":"unblock-msisdn",
	"data":{
		"date_time": 1629685778,
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.date_time | Number | Unix Time Stamp when the message is transmitted by the applications | 
| `data`.receiver | String | Nomor MSISDN yang akan dibuka blokir |

Server WSMessageBroker berbasis menggunakan protokol WebSocket. Silakan download WSMessageBroker di https://github.com/kamshory/Messenger 

**Handhake**

Handshake antara OTP-Pi dengan WSMessageBroker adalah sebagai berikut:
1. OTP-Pi sebagai client dan WSMessageBroker sebagai server
2. OTP-Pi mengirim request ke WSMessageBroker

**Contoh Konfigurasi Feeder WebSocket**

| Parameter | Value |
|--|--|
| Host | domain.example |
| Port | 8000 |
| Path | /ws |
| Username | username |
| Password | password |
| Channel | sms |

**Contoh Handhake WebSocket**

```http
GET /ws?channel=sms HTTP/1.1
Host: domain.example:8000
Authorization: Basic dXNlcm5hbWU6cGFzc3dvcmQ=
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
Sec-WebSocket-Version: 13
```

Server akan memverifikasi apakah username dan password tersebut benar. Jika benar, server akan memasukkan koneksi tersebut ke dalam daftar penerima pesan.

Saat sebuah client mengirimkan pesan, pesan tersebut akan dikirimkan ke semua client dengan channel kecuali si pengirim. Dengan demikian, handshake antara pengirim dan penerima pesan adalah sama.

OTP-Pi tidak pernah mengirim pesan ke server WSMessageBroker. OTP-Pi hanya menerima pesan sesuai dengan channel yang diinginkan.

## Pengujian Modul

**Home** 

1. Service Status : OK
2. Server Status : OK

**USSD**

1. Execute USSD : OK

**SMS**

1. Send SMS : OK

**Administrator**

1. View : OK
2. Add : OK
3. Edit : OK
4. Delete : OK
5. Activate : OK
6. Deactivate : OK
7. Block : OK
8. Unblock : OK
	
**API Setting**

1. Update : OK
2. API User
3. View : OK
4. Add : OK
5. Edit : OK
6. Delete : OK
7. Activate : OK
8. Deactivate : OK
9. Block : OK
10. Unblock : OK
	
**Feeder Setting**

1. WSMessageBroker : OK
2. RabbitMQ : OK
	
**SMS Setting**

1. Send SMS : OK

**Modem**

1. Add : OK
2. Edit : OK
3. Delete : OK
4. Activate : OK
5. Deactivate : OK
	
**Email Setting**

1. Update

**DNS Record**

1. Add : OK
2. Edit : OK
3. Delete : OK
4. Activate : OK
5. Deactivate : OK
6. Proxied : OK
7. Unproxied : OK
	
**Network Setting**

1. DHCP : OK
2. Wireless LAN : OK
3. Ethernet : OK
	
**Cloudflare** 

1. Update : OK

**No IP**

1. Update : OK


## AT Command Documentation

https://docs.rs-online.com/5931/0900766b80bec52c.pdf