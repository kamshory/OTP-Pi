

# OTP-Pi

![OTP-Pi](https://raw.githubusercontent.com/kamshory/OTP-Pi/main/otp-pi.png)

OTP atau `One Time Password` adalah sebuah password satu kali pakai yang memiliki masa berlaku tertentu. Umumnya masa berlaku dibuat sangat singkat dan hanya memberikan kesempatan kepada penerimanya untuk memasukkannya ke dalam aplikasi dan mengirimkannya ke server aplikasi yang memerlukannya. OTP bersifat sangat rahasia sehingga `clear text` dari OTP tidak boleh disimpan oleh pihak manapun. Bahkan, server aplikasi hanya menyimpan `hash` atau `token` yang cocok dengan OTP tersebut. `Clear text` hanya dibuat lalu dikirim ke penerima. Dengan kata lain, `clear text` hanya diketahui oleh penerima OTP saja.

OTP yang paling populer dikirimkan melalui SMS atau Short Message Service. Penggunaan SMS memiliki kelebihan sebagai berikut:

1. Hanya dapat diterima oleh perangkat di mana SIM card dari nomor penerima terpasang. Hal ini tentu saja berkaitan dengan `What you have` pada `multifactor authentication`
2. Dapat dengan mudah dibaca pada hampir semua merek dan model perangkat telepon seluler
3. Umumnya perangkat langsung memberitahukan adanya pesan masuk tanpa memerlukan aksi pengguna
4. Pada perangkat telepon seluler pintar, aplikasi pengguna OTP dapat langsung membaca pesan masuk apabila diijinkan oleh penggunanya. Dengan demikian, aplikasi dapat langsung memverifikasi OTP tanpa memerlukan tindakan manual dari penggunanya. Hal ini akan menghemat waktu dan mengurangi kesalahan 
5. Tidak memerlukan credit penerima sehingga penerima pesan tidak perlu membayar apapun untuk dapat menerima pesan tersebut
6. Memiliki jangkauan yang sangat luas
7. Perangkat penerima SMS tersedia dalam berbagai kelas harga sehingga dapat dijangkau oleh hampir semua kalangan 

Dari sekian banyak kelebihan di atas, ternyata SMS memiliki keterbatasan secara teknis. SMS harus dikirim melalui operator telekomunikasi yang secara legal terdaftar di negara pengirim. SMS dapat dikirm menggunakan perangkat GSM yang tersambung ke operator telekomunikasi. Perangkat yang paling murah untuk mengirimkan SMS adalah telepon seluler dan modem GSM.

Cara lain untuk mengirimkan SMS yaitu dengan bekerjasama langsung dengan operator telekomunikasi atau menggunakan jasa pihak ketiga. Kerjasama dengan operator telekomunikasi tentu saja tidak mudah. Selain harus berbadan hukum, biaya yang diperlukan tentu saja tidak sedikit. Selain itu, volume pengiriman SMS juga menjadi bahan pertimbangan kerjasama tersebut diterima atau tidak oleh operator telekomunikasi. Penggunaan jasa pihak ketiga adalah opsi lain. Faktor keamanan tentu saja menjadi pertimbangan. Penyedia layanan tentu saja harus bisa dipercaya untuk menjaga kerahasiaan OTP yang dikirimkan.

Bagi perusahaan berskala kecil yang ingin membangun sendiri sistem pengiriman OTP dapat menggunakan berbagai macam aplikasi yang tersedia secara gratis maupun berbayar di pasaran. Beberapa pertimbangan dalam memilih aplikasi OTP adalah sebagai berikut:

1. Kecepatan pengiriman
2. Keamanan/kerahasiaan
3. Kemudahan dalam instalasi dan integrasi
4. Biaya awal dan biaya operasional

Aplikasi yang menggunakan database untuk menghubungkan antara sisi penerima pesan dan modem GSM tentu saja tidak aman. Data di dalam database dapat dibaca oleh administrator. Bahkan, administrator dapat meminta OTP pada saat pengguna sedang tidak menyadarinya dan sedang tidak bertransaksi.

Aplikasi yang mewajibkan penggunaan public IP address tentu saja tidak memberikan keleluasaan bagi pengguna. Perusahaan atau perorangan yang tidak berlangganan internet dengan IP public tidak dapat menggunakan palikasi tersebut. Pengguna perlu menempatkan server pada jaringan dengan IP public.

Aplikasi yang berjalan pada desktop dan laptop tentu saja memerlukan biaya investasi dan biaya operasional yang tinggi. Laptop atau desktop yang digunakan harus beroperasi selama 24 jam sehari dan 7 hari dalam seminggu. Listrik yang digunakan tentu saja tidak sedikit.

OTP-Pi menjawab semua tantangan di atas. Dengan perangkat yang sangat murah, pengguna dapat memiliki sebuah SMS gateway yang memberikan banyak fitur serta dapat dioperasikan dengan biaya yang sangat murah.

OTP-Pi adalah server untuk mengirimkan SMS melalui protokol HTTP, WebSocket dan Message Broker. Pengguna dapat memasang OTP-Pi pada server dengan IP address statis yang diapat diakses oleh klien yang akan mengirimkan SMS. Selain itu, pengguna juga dapat memasang OTP-Pi pada server dengan IP address dinamis. Server ini kemudian mengakses sebuah server websocket atau server RabbitMQ. OTP-Pi bertindak sebagai consumer yang akan mengirimkan semua SMS yang diterimanya.

# System Requirement

OTP-Pi membutuhkan Raspberry Pi 3 Model B atau yang lebih tinggi. RAM minimum adalah 1 GB dan SD Card minimum 16 GB. SD Card 32 GB lebih direkomendasikan. Power supplay 5 Volt 3 Ampre untuk mencegah Raspberry Pi beroperasi dengan under voltage.

# Feature


## Multiple Device

Modem adalah daftar modem yang terpasang pada OTP-Pi. Modem diberi nama berdasarkan merek dan model perangkat serta koneksi yang digunakan. Modem dapat diaktifkan dan dinonaktirkan kapan saja. Modem yang tidak aktif tidak akan digunakan untuk mengirimkan SMS meskipun secara fisik terpasang pada OTP-Pi dan menerima aliran daya.

OTP-Pi dapat menggunakan beberapa modem sekaligus. Pengiriman SMS akan menggunakan algoritma Round-Robin di mana semua modem yang aktif akan digunakan secara bergilir.

## Prefix-Based Routing

Dalam kenyataannya, efisiensi biaya menjadi hal yang sangat penting bagi pengguna. Biaya pengiriman SMS harus dapat ditekan semaksimal mungkin. Operator telekomunikasi biasannya menerapkan biaya yang lebih rendah saat mengirim SMS ke nomor pelanggan dari operator telekomunikasi yang sama. Sebaliknya, biaya pengiriman SMS akan lebih tinggi saat mengirim SMS ke nomor pelanggan dari operator lain.

OTP-Pi memungkinkan pengguna mengatur pengiriman SMS. Sebagai contoh: pengguna menggunakan 4 modem dengan 4 SIM card yang berbeda dari operator telekomunikasi yang berbeda. 

1. Modem 1 dengan SIM Card dari Operator Telekomunikasi 1
2. Modem 2 dengan SIM Card dari Operator Telekomunikasi 2
3. Modem 3 dengan SIM Card dari Operator Telekomunikasi 3
4. Modem 4 dengan SIM Card dari Operator Telekomunikasi 4

Operator Telekomunikasi 1 menerapkan biaya Rp 50 untuk nomor dengan prefix `62871` dan `62872` dan menerapkan biaya Rp 350 untuk nomor selain itu.
Operator Telekomunikasi 2 menerapkan biaya Rp 100 untuk nomor dengan prefix `62835`, `62856`, dan `62837` dan menerapkan biaya Rp 350 untuk nomor selain itu.
Operator Telekomunikasi 3 menerapkan biaya Rp 60 untuk nomor dengan prefix `62845` dan menerapkan biaya Rp 250 untuk nomor selain itu.
Operator Telekomunikasi 4 menerapkan biaya Rp 90 untuk nomor dengan prefix `62848` dan `62849` dan menerapkan biaya Rp 200 untuk nomor selain itu.

Dari kasus di atas, biaya paling rendah untuk operator lain adalah Rp 200. Pengguna dapat mengatur modem 4 sebagai modem default. Semua SMS selain dari prefix `62871`, `62872`, `62835`, `62856`, `62837` dan `62845` akan menggunakan modem ini dan dikirim melalui Operator Telekomunikasi 4. Semua SMS untuk nomor dengan prefix `62871` dan `62872` menggunakan modem 1 dan dikirim melalui Operator Telekomunikasi 1. Semua SMS untuk nomor dengan prefix `62835`, `62856`, dan `62837` menggunakan modem 2 dan dikirim melalui Operator Telekomunikasi 2. Semua SMS untuk nomor dengan prefix `62845` menggunakan modem 3 dan dikirim melalui Operator Telekomunikasi 3. Dengan demikian, biaya pengiriman SMS akan dapat ditekan. 

Pengguna dapat menggunakan 2 atau lebih SIM Card dari satu operator yang sama. Modem akan digunakan secara bergantian dengan algoritma Round-Robin saat OTP-Pi mengirimkan SMS ke nomor dengan prefix yang sama.

Biaya pengiriman SMS akan lebih murah lagi ketika pengguna memanfaatkan promo dari operator telekomunikasi yang bersangkutan. Beberapa operator akan menerapkan biaya pengiriman SMS yang sangat rendah setelah pengguna mengirimkan beberapa SMS dengan ketentuan tertentu.

Pengaturan prefix menggunakan MSISDN. Dengan demikian, pada contoh di atas, saat pengguna di Indonesia mengatur prefix 0871, maka yang tersimpan adalah 62871. Dengan demikian, pengguna harus menggunakan panjang prefix 5 alih-alih 4.

## USSD Support

OTP-Pi memungkinkan pengguna melakukan perintah USSD pada masing-masing modem. Perintah USSD tentu saja tergantung dari masing-masing operator seluler yang digunakan pada masing-masing SIM card yang terpasang pada masing-masing modem.

## Manual SMS

Manual SMS digunakan untuk menguji apakah masing-masing modem dapat mengirimkan SMS. 

## Internet Mobile

OTP-Pi dapat terhubung ke internet menggunakan modem mobile. Dengan demikian, pengguna tidak perlu menghubungkan OTP-Pi dengan internet kabel atau optik. Ini memberikan alternatif bagi pengguna dalam memilih koneksi internet.

## Administrator Setting

Administrator Setting adalah menu untuk melakukan konfigurasi administrator. Perangkat OTP-Pi baru belum memiliki administrator. Pengguna harus membuat administrator terlebih dahulu sebelum menggunakannya. Silakan masuk ke akses poin OTP-Pi sesuai dengan SSID dan password yang tertera pada brosur dan pindai QR Code pada brosur menggunakan smartphone.

Alamat bawaan dari web manajemen adalah http://192.168.0.11:8888 

**Username**
Username adalah pengenal administrator saat login ke OTP-Pi

**Password**
Username adalah pengaman administrator saat login ke OTP-Pi

**Phone Number**
Phone number dapat digunakan jika administrator lupa password. Password akan dikirim melalui SMS. Tentu saja ini baru bisa dilakukan ketika OTP-Pi telah terkonfigurasi dengan benar.

**Email**
Email dapat digunakan jika administrator lupa password. Password akan dikirim melalui email. Tentu saja ini baru bisa dilakukan ketika OTP-Pi telah terkonfigurasi dengan benar.

## API Setting

API Setting adalah konfigurasi REST API untuk mengirimkan SMS.
1. **HTTP Port** adalah port server untuk HTTP
2. **Enable HTTP** adalah pengaturan untuk mengaktifkan atau menonaktifkan port HTTP
3. **HTTPS Port** adalah port server untuk HTTPS
4. **Enable HTTPS** adalah pengaturan untuk mengaktifkan atau menonaktifkan port HTTPS
5. **Message Path** adalah path untuk mengirimkan SMS dan email
6. **Blocking Path** adalah path untuk memblokir nomor telepon agar OTP-Pi tidak mengirimkan SMS ke nomor tersebut
7. **Unblocking Path** adalah path untuk membuka blokir nomor telepon agar OTP-Pi dapat kembali mengirimkan SMS ke nomor tersebut

## API User

API User adalah akun pengirim SMS melalui REST API.

**Username**
Username adalah pengenal pengirim saat mengirimkan SMS ke OTP-Pi

**Password**
Username adalah pengaman pengirim saat mengirimkan SMS ke OTP-Pi

**Phone Number**
Phone number adalah informasi kontak berupa nomor telepon dari pengguna API

**Email**
Email adalah informasi kontak berupa alamat email dari pengguna API

## Subscriber Setting

OTP-Pi memberikan pilihan apabila perangkat ini dipasang pada jaringan internet mobile atau pada jaringan di mana perangkat pengirim tidak mungkin dapat menjangkau alamat dari OTP-Pi. Pengguna dapat mengirimkan OTP dengan message broker sebagai berikut:

1. WSMessageBroker (WebSocket)
2. RabbitMQ (AMQP)
3. Mosquito (MQTT)

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

**Mosquito**

| Field | Description |
| ----- | ----------- |
| Enable | Enable or disable Mosquito |
| SSL | SSL connection |
| Host | Mosquito host name or IP address |
| Port | Port number |
| Client ID | Client ID |
| Username | Username on basic authorzation |
| Password | Password on basic authorzation |
| Topic | Topic of subscribsion | 
| Timeout | Request time out |
| Refresh Connection | Refresh Mosquito connection |



## SMS Setting

SMS Setting adalah konfigurasi pengiriman SMS oleh OTP-Pi.

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

Bloking list adalah daftar nomor telepon yang diblokir. Nomor yang diblokir tidak akan menerima SMS dari modem manapun. Daftar ini dapat ditambah dan diputihkanmelalui REST API, RabbitMQ dan WebSocket. Daftar ini juga dapat secara manual ditambah, diputihkan, atau dihapus melalui web admin.

## Multiple Email Account

OTP-Pi mendukung multiple email account. Email account adalah konfigurasi yang berisi alamat SMTP Server, port SMTP server, username, password, dan konfigurasi lainnya.

SMTP Server digunakan untuk mengirimkan OTP ke alamat email dan dapat digunakan untuk melakukan reset password.

Penggunaan banyak akun email lebih direkomendasikan karena jumlah email yang dikirim oleh setiap akun email akan berkurang. Sebagai contoh: sebuah SMTP server membatasi 500 email perhari. Dengan menggunakan 20 akun email, OTP-Pi dapat mengirim hingga 10.000 email perhari.

Untuk menggunakan SMTP Gmail, gunakan konfigurasi berikut:

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

OPT Broker dilengkapi dengan local SMTP server untuk mengirim email. Dengan adanya local SMTP server ini, pengguna dapat mengirimkan email tanpa memerlukan external SMTP server. Meskipun demikian, local SMTP server tidak direkomendasikan karena mail server tujuan dapat saja memblokir email yang dikirim dari local SMTP server. 

## DDNS Record

DDNS Record adalah data untuk melakukan pengaturan DNS secara dinamis. DDNS atau Dymanic Domain Name System adalah sebuah mekanisme pengaturan DNS yang dilakukan secara berulang-ulang disebabkan karena alamat IP publik dari server yang selalu berubah-ubah.

OTP-Pi menyediakan pengaturan DDNS menggunakan vendor DDNS. Beberapa vendor DDNS yang didukung adalah sebagai berikut:

1. Cloudflare - https://www.cloudflare.com/
2. NoIP - https://www.noip.com/
3. Dynu Dyn DNS - https://www.dynu.com/
4. Free DNS Afraid - https://freedns.afraid.org/

## Network Setting 

Network Setting adalah konfigurasi untuk mengatur jaringan dari OTP-Pi. OTP-Pi dilengkapi dengan akses poin sehingga dapat diakses langsung menggunakan laptop atau handphone tanpa harus memasangnya ke jaringan kabel. Hal ini akan memudahkan pengguna karena konfigurasi jaringan LAN pengguna berbeda-beda. Pengguna cukup mengatur alamat IP pada jaringan ethernet sesuai dengan konfigurasi jaringan LAN yang digunakan.

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

Konfigurasi Wireless LAN akan mengatur alamat IP pada jaringan wireless OTP-Pi. Alamat IP bawaan dari OTP-Pi adalah 192.168.0.11

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

Konfigurasi Ethernet akan mengatur alamat IP ethernet pada OTP-Pi.

| Field | Value |
| --- | ------ |
| IP Address | IP address of the device if client connect via Ethernet |
| Prefix | Nework prefix |
| Netmask | Netmask |
| Gateway | Gateway |
| DNS1 | DNS 1 |
| DNS2 | DNS 2 |

## Firewall

OTP-Pi dapat membuka dan mentutup port dari sistem operasi yang digunakan oleh SMS Broker. Penutupan port ini akan membuat firewall di lapis sistem operasi. Dalam hal ini, apabila port ditutup, maka equest tidak akan sampai masuk ke aplikasi.

## Monitor

Monitor dapat digunakan untuk melihat aktivitas modem yang terpasang di OTP-Pi. Pada saat OTP-Pi mengirimkan SMS, OTP-Pi akan menunjukkan dari mana request SMS masuk dan modem mana yang digunakan.  Selain itu, status dari modem akan ditunjukkan sehingga akan terlihat modem mana saja yang terhubung dengan benar.

## Cloudflare

Modul Cloudflare adalah modul untuk mengatur akun Cloudflare yang digunakan.

## NoIP

Modul NoIP adalah modul untuk mengatur akun NoIP yang digunakan.

## Dynu

Modul Dynu adalah modul untuk mengatur akun Dynu Dyn DNS yang digunakan.

## Afraid

Modul Afraid adalah modul untuk mengatur akun Free DNS Afraid yang digunakan.

## Real Time Clock

Modul Real Time Clock atau RTC diperlukan untuk menjaga waktu perangkat saat perangkat dimatikan dan tidak terhubung dengan internet. Perangkat harus menggunakan waktu yang benar agar cookie yang dikirim oleh perangkat tidak dihapus oleh browser karena kedaluarsa sehingga administrator dapat mengelola perangkat termasuk melakukan pengaturan. Selain itu, perangkat perlu mencatat waktu saat terjadi kesalahan sehingga catatan kesalahan dapat dibaca oleh pengguna untuk dapat dilakukan dimitigasi.

Modul Real Time Clock tertanam di dalam perangkat dan sudah diprogram untuk dapat bekerja sebagaimana mestinya. Apabila perangkat mati dalam waktu yang lama sehingga menyebabkan daya pada battery RTC habis, maka perangkat dapat menyesuaikan waktu dengan browser yang mengaksesnya tanpa mempedulikan daerah waktu yang digunakan. Selanjutnya, administrator dapat mengatur waktu perangkat jika diperlukan.

## Reset Device

Pada kasus pengguna tidak dapat mengakses web administrator, baik karena lupa password, atau karena konfigurasi jaringan yang kacau, pengguna dapat melakukan reset device. Reset device dilakukan dengan menancapkan sebuah flash disk yang berisi file sebagai berikut:

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

Untuk menggunakan Message Broker, silakan gunakan RabbitMQ dengan link https://www.rabbitmq.com/

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
Content-length: 129
Authorization: Basic dXNlcjpwYXNzd29yZA==

{
	"command": "send-sms",
	"data": {
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
Content-length: 138
Authorization: Basic dXNlcjpwYXNzd29yZA==

{
	"command": "send-email",
	"data": {
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
Content-length: 70
Authorization: Basic dXNlcjpwYXNzd29yZA==

{
	"command": "block-msisdn",
	"data": {
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.msisdn | String | Nomor MSISDN yang akan diblokir |

**Unblock Number Request**

```http
POST /api/unblock HTTP/1.1
Host: sub.domain.tld
Connection: close
User-agent: KSPS
Content-type: application/json
Content-length: 81
Authorization: Basic dXNlcjpwYXNzd29yZA==

{
	"command": "unblock-msisdn",
	"data": {
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.receiver | String | Nomor MSISDN yang akan dibuka blokir |

### Sekenario 2 - OTP-Pi Tidak Dapat Diakses App Server

Pada skenario ini, App Server dapat mengirimkan OTP ke RabbitMQ Server atau WSMessageBroker. WSMessageBroker menggunakan protokol WebSoket dan Basic Authentication. Baik App Server maupun OTP-Pi bertindak sebagai client dari WSMessageBroker.

App Server bertindak sebagai publisher dan OTP-Pi menjadi consumer dari RabbitMQ Server dan WSMessageBroker. Keduanya harus menggunakan channel yang sama agar semua OTP yang dikirimkan oleh App Server dapat diterima oleh OTP-Pi.

![OTP-Pi Topology Skenario 2](https://raw.githubusercontent.com/kamshory/OTP-Broker/main/src/main/resources/static/www/lib.assets/images/topology-2.png)

Dari kedua skenario di atas, OTP-Pi akan mengirmkan SMS menggunakan modem GSM yang terpasang secara fisik pada perangkat OTP-Pi. Pengguna dapat menggunakan salah satu dari RabbitMQ Server atau WSMessageBroker dan dapat pula menggunakan keduanya dalam waktu bersamaan. Akan tetapi, apabila App Server mengirimkan sebuah OTP yang sama ke RabbitMQ Server dan WSMessageBroker, maka OTP-Pi akan mengirimkan SMS tersebut dua kali ke nomor penerima.

Pada skenario ini, pengguna tidak memerlukan IP public. Pengguna hanya memerlukan:

1. OTP-Pi
2. Koneksi internet (tidak memerlukan IP public dan port forwarding)
3. Server RabbitMQ, MQTT atau WSMessageBroker

**1. RabbitMQ**

**Send SMS Request**

```json
{
	"command": "send-sms",
	"data": {
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
| `data`.id | String | ID SMS |
| `data`.receiver | String | Nomor MSISDN penerima |
| `data`.message| String | Pesan SMS |

**Send Email Request**

```json
{
	"command":"send-sms",
	"data": {
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
| `data`.id | String | ID SMS |
| `data`.receiver | String | Alamat email penerima |
| `data`.message| String | Pesan SMS |

**Block Number Request**

```json
{
	"command": "block-msisdn",
	"data": {
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.receiver | String | Nomor MSISDN yang akan diblokir |

**Unblock Number Request**

```json
{
	"command": "unblock-msisdn",
	"data":{
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.receiver | String | Nomor MSISDN yang akan dibuka blokir |

**2. MQTT**

**Send SMS Request**

```json
{
	"command":"send-sms",
	"data": {
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
| `data`.id | String | ID SMS |
| `data`.receiver | String | Nomor MSISDN penerima |
| `data`.message| String | Pesan SMS |

**Send Email Request**

```json
{
	"command": "send-sms",
	"data": {
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
| `data`.id | String | ID SMS |
| `data`.receiver | String | Alamat email penerima |
| `data`.message| String | Pesan SMS |

**Block Number Request**

```json
{
	"command": "block-msisdn",
	"data": {
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.receiver | String | Nomor MSISDN yang akan diblokir |

**Unblock Number Request**

```json
{
	"command": "unblock-msisdn",
	"data": {
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.receiver | String | Nomor MSISDN yang akan dibuka blokir |

**3. WSMessageBroker**

**Send SMS Request**

```json
{
	"command":"send-sms",
	"data": {	
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
| `data`.id | String | ID SMS |
| `data`.receiver | String | Nomor MSISDN penerima |
| `data`.message| String | Pesan SMS |

**Send Email Request**

```json
{
	"command": "send-sms",
	"data": {
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
| `data`.id | String | ID SMS |
| `data`.receiver | String | Alamat email penerima |
| `data`.message| String | Pesan SMS |

**Block Number Request**

```json
{
	"command": "block-msisdn",
	"data": {
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
| `data`.receiver | String | Nomor MSISDN yang akan diblokir |

**Unblock Number Request**

```json
{

	"command":"unblock-msisdn",
	"data":{
		"receiver": "08126666666",
	}
}
```

| Parameter | Tipe | Deskripsi |
| --------- | ---- | ----------|
| command | String | Perintah ke OTP-Pi |
| data | Objek | Data untuk OTP-Pi | 
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


