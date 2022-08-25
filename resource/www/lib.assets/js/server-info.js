
$(document).ready(function (e3) {
    loadData();
    setInterval(function (e2) {
        reloadData();
    }, 10000);
    $(document).on('click', '.service-item.service-modem .button-connect, .service-item.service-modem-sms .button-connect, .service-item.service-modem-internet .button-connect', function (e) {
        var url = '';
        if ($(this).closest('.service-item').hasClass('service-modem-sms')) {
            url = 'api/modem-sms';
        }
        else if ($(this).closest('.service-item').hasClass('service-modem-internet')) {
            url = 'api/modem-internet';
        }
        else {
            url = 'api/modem';
        }
        $.ajax({
            url: url,
            type: 'POST',
            data: {
                'action': 'connect'
            },
            dataType: 'json',
            success: function (receivedJSON) {
                if (receivedJSON.command == "broadcast-message") {
                    for (var i in receivedJSON.data) {
                        showNotif(receivedJSON.data[i].message);
                    }
                }
            },
            error: function (err) {
            }
        });
        e.preventDefault();
    });
    $(document).on('click', '.service-item.service-modem .button-disconnect, .service-item.service-modem-sms .button-disconnect, .service-item.service-modem-internet .button-disconnect', function (e) {
        var url = '';
        if ($(this).closest('.service-item').hasClass('service-modem-sms')) {
            url = 'api/modem-sms';
        }
        else if ($(this).closest('.service-item').hasClass('service-modem-internet')) {
            url = 'api/modem-internet';
        }
        else {
            url = 'api/modem';
        }
        if (confirm('Area you sure you want to disconnect all modems?')) {

            $.ajax({
                url: url,
                type: 'POST',
                data: {
                    'action': 'disconnect'
                },
                dataType: 'json',
                success: function (receivedJSON) {
                    if (receivedJSON.command == "broadcast-message") {
                        for (var i in receivedJSON.data) {
                            showNotif(receivedJSON.data[i].message);
                        }
                    }
                },
                error: function (err) {
                }
            });
        }
        e.preventDefault();
    });
    $(document).on('click', '.service-item.service-ws .button-connect', function (e) {
        $.ajax({
            url: 'api/subscriber-ws',
            type: 'POST',
            data: {
                'action': 'start'
            },
            dataType: 'json',
            success: function (data) {
            },
            error: function (err) {
            }
        });
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-ws .button-disconnect', function (e) {
        if (confirm('Are you sure you want to stop WebSocket client?')) {
            $.ajax({
                url: 'api/subscriber-ws',
                type: 'POST',
                data: {
                    'action': 'stop'
                },
                dataType: 'json',
                success: function (data) {
                },
                error: function (err) {
                }
            });
        }
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-amqp .button-connect', function (e) {
        $.ajax({
            url: 'api/subscriber-amqp',
            type: 'POST',
            data: {
                'action': 'start'
            },
            dataType: 'json',
            success: function (data) {
            },
            error: function (err) {
            }
        });

        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-amqp .button-disconnect', function (e) {
        if (confirm('Are you sure you want to stop RabbitMQ client?')) {
            $.ajax({
                url: 'api/subscriber-amqp',
                type: 'POST',
                data: {
                    'action': 'stop'
                },
                dataType: 'json',
                success: function (data) {
                },
                error: function (err) {
                }
            });
        }
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-redis .button-connect', function (e) {
        $.ajax({
            url: 'api/subscriber-redis',
            type: 'POST',
            data: {
                'action': 'start'
            },
            dataType: 'json',
            success: function (data) {
            },
            error: function (err) {
            }
        });
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-redis .button-disconnect', function (e) {
        if (confirm('Are you sure you want to stop Redis client?')) {
            $.ajax({
                url: 'api/subscriber-redis',
                type: 'POST',
                data: {
                    'action': 'stop'
                },
                dataType: 'json',
                success: function (data) {
                },
                error: function (err) {
                }
            });
        }
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-mqtt .button-connect', function (e) {
        $.ajax({
            url: 'api/subscriber-mqtt',
            type: 'POST',
            data: {
                'action': 'start'
            },
            dataType: 'json',
            success: function (data) {
            },
            error: function (err) {
            }
        });
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-mqtt .button-disconnect', function (e) {
        if (confirm('Are you sure you want to stop Mosquitto client?')) {
            $.ajax({
                url: 'api/subscriber-mqtt',
                type: 'POST',
                data: {
                    'action': 'stop'
                },
                dataType: 'json',
                success: function (data) {
                },
                error: function (err) {
                }
            });
        }
        e.preventDefault();
    });


    $(document).on('click', '.service-item.service-activemq .button-connect', function (e) {
        $.ajax({
            url: 'api/subscriber-activemq',
            type: 'POST',
            data: {
                'action': 'start'
            },
            dataType: 'json',
            success: function (data) {
            },
            error: function (err) {
            }
        });
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-activemq .button-disconnect', function (e) {
        if (confirm('Are you sure you want to stop ActiveMQ client?')) {
            $.ajax({
                url: 'api/subscriber-activemq',
                type: 'POST',
                data: {
                    'action': 'stop'
                },
                dataType: 'json',
                success: function (data) {
                },
                error: function (err) {
                }
            });
        }
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-stomp .button-connect', function (e) {
        $.ajax({
            url: 'api/subscriber-stomp',
            type: 'POST',
            data: {
                'action': 'start'
            },
            dataType: 'json',
            success: function (data) {
            },
            error: function (err) {
            }
        });
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-stomp .button-disconnect', function (e) {
        if (confirm('Are you sure you want to stop Stomp client?')) {
            $.ajax({
                url: 'api/subscriber-stomp',
                type: 'POST',
                data: {
                    'action': 'stop'
                },
                dataType: 'json',
                success: function (data) {
                },
                error: function (err) {
                }
            });
        }
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-http .button-connect', function (e) {
        $.ajax({
            url: 'api/subscriber-http',
            type: 'POST',
            data: {
                'action': 'start'
            },
            dataType: 'json',
            success: function (data) {
            },
            error: function (err) {
            }
        });
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-http .button-disconnect', function (e) {
        if (confirm('Are you sure you want to stop HTTP server?')) {
            $.ajax({
                url: 'api/subscriber-http',
                type: 'POST',
                data: {
                    'action': 'stop'
                },
                dataType: 'json',
                success: function (data) {
                },
                error: function (err) {
                }
            });
        }
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-https .button-connect', function (e) {
        $.ajax({
            url: 'api/subscriber-https',
            type: 'POST',
            data: {
                'action': 'start'
            },
            dataType: 'json',
            success: function (data) {
            },
            error: function (err) {
            }
        });
        e.preventDefault();
    });

    $(document).on('click', '.service-item.service-https .button-disconnect', function (e) {
        if (confirm('Are you sure you want to stop HTTPS server?')) {
            $.ajax({
                url: 'api/subscriber-https',
                type: 'POST',
                data: {
                    'action': 'stop'
                },
                dataType: 'json',
                success: function (data) {
                },
                error: function (err) {
                }
            });
        }
        e.preventDefault();
    });

    $(document).on('click', '.tile.reboot a', function (e) {
        if (confirm('Are you sure you want to reboot the OS?')) {
            waitingForServerUp();
            $.ajax({
                url: 'api/reboot',
                type: 'POST',
                data: {
                    'confirm': 'yes'
                },
                dataType: 'json',
                success: function (data) {
                },
                error: function (err) {
                }
            });
        }
    })
    $(document).on('click', '.tile.restart a', function (e) {
        if (confirm('Are you sure you want to restart the service?')) {
            waitingForServerUp();
            $.ajax({
                url: 'api/restart',
                type: 'POST',
                data: {
                    'confirm': 'yes'
                },
                dataType: 'json',
                success: function (data) {
                },
                error: function (err) {
                }
            });
        }
    });
    $(document).on('click', '.tile.expand a', function (e) {
        if (confirm('Are you sure you want expand the storage?')) {
            $.ajax({
                url: 'api/expand',
                type: 'POST',
                data: {
                    'confirm': 'yes'
                },
                dataType: 'json',
                success: function (data) {
                    updateCPUUsage(data);
                    updateRAMStatus(data);
                    updateSWAPStatus(data);
                    updateStorageStatus(data);
                    updateCPUTemperatureStatus(data);
                },
                error: function (err) {
                }
            });
        }
    });

    $(document).on('click', '.tile.cleaner a', function (e) {
        if (confirm('Are you sure you want to clean up the service?')) {
            $.ajax({
                url: 'api/cleanup',
                type: 'POST',
                data: {
                    'confirm': 'yes'
                },
                dataType: 'json',
                success: function (data) {
                    console.log('DATA : ' + JSON.stringify(data));
                }
            });
        }
    });
});

function loadData() {
    $.ajax({
        type: "GET",
        url: "data/server-info/get",
        dataType: "json",
        success: function (data) {
            updateCPUUsage(data);
            updateRAMStatus(data);
            updateSWAPStatus(data);
            updateStorageStatus(data);
            createCPUCore(data);
            updateCPUTemperatureStatus(data);
        }
    });
}

function reloadData() {
    $.ajax({
        type: "GET",
        url: "data/server-info/get",
        dataType: "json",
        success: function (data) {
            $('.waiting-server-up').css({
                'display': 'none'
            });
            updateCPUUsage(data);
            updateRAMStatus(data);
            updateSWAPStatus(data);
            updateStorageStatus(data);
            updateCPUTemperatureStatus(data);
        }
    });
}

function createCPUCore(data) {
    $('.cpu-temperature-container').empty();
    for (var i in data.cpu.temperature) {
        var core = data.cpu.temperature[i];
        var label = core.label;
        var percent = 100 * core.value.currentTemperature / 150;
        var cls = label.split(' ').join('-').toLowerCase();
        var html = '<div class="server-into-item ' + cls + '">\r\n' +
            '<div class="info-label">' + label + '</div>\r\n' +
            '<div class="info-value">' + core.raw.currentTemperature + '</div>\r\n' +
            '<div class="info-progress">\r\n' +
            '  <div class="info-progress-inner" style="width:' + percent + '%"></div>\r\n' +
            '</div>\r\n' +
            '</div>\r\n';
        $('.cpu-temperature-container').append(html);
    }
}

function updateCPUTemperatureStatus(data) {
    for (var i in data.cpu.temperature) {
        var core = data.cpu.temperature[i];
        var label = core.label;
        var percent = 100 * core.value.currentTemperature / 150;
        var cls = label.split(' ').join('-').toLowerCase();
        $('.cpu-temperature-container').find('.' + cls).find('.info-value').html(core.raw.currentTemperature);
        $('.cpu-temperature-container').find('.' + cls).find('.info-progress-inner').css({
            'width': percent + '%'
        });
    }
}

function updateRAMStatus(data) {
    if (typeof data.memory.ram != 'undefined') {
        if (typeof data.memory.ram.total == 'undefined' || data.memory.ram.total <= 0) {
            data.memory.ram.total = 0;
        }
        var percentRAM = data.memory.ram.total == 0 ? 0 : (100 * (data.memory.ram.used / data.memory.ram.total));
        var totalRAM = data.memory.ram.total / (1024 * 1024);
        var usedRAM = data.memory.ram.used / (1024 * 1024);
        $('.ram').find('.info-value').text(percentRAM.toFixed(2) + "% (" + usedRAM.toFixed(2) + "GB / " + totalRAM.toFixed(2) + "GB" + ")");
        $('.ram').find('.info-progress-inner').css({
            'width': percentRAM.toFixed(2) + '%'
        });
    }
}

function updateSWAPStatus(data) {
    if (typeof data.memory.swap != 'undefined') {
        if (typeof data.memory.swap.total == 'undefined' || data.memory.swap.total <= 0) {
            data.memory.swap.total = 0;
        }
        var percentSWAP = data.memory.swap.total == 0 ? 0 : (100 * (data.memory.swap.used / data.memory.swap.total));
        var totalSWAP = data.memory.swap.total / (1024 * 1024);
        var usedSWAP = data.memory.swap.used / (1024 * 1024);
        $('.swap').find('.info-value').text(percentSWAP.toFixed(2) + "% (" + usedSWAP.toFixed(2) + "GB / " + totalSWAP.toFixed(2) + "GB" + ")");
        $('.swap').find('.info-progress-inner').css({
            'width': percentSWAP.toFixed(2) + '%'
        });
    }
}

function updateStorageStatus(data) {
    if (typeof data.storage.total == 'undefined' || data.storage.total <= 0) {
        data.storage.total = 0;
    }
    var percentStorage = data.storage.total == 0 ? 0 : (100 * (data.storage.used / data.storage.total));
    var totalStorage = data.storage.total / (1024 * 1024);
    var usedStorage = data.storage.used / (1024 * 1024);
    $('.storage').find('.info-value').text(percentStorage.toFixed(2) + "% (" + usedStorage.toFixed(2) + "GB / " + totalStorage.toFixed(2) + "GB" + ")");
    $('.storage').find('.info-progress-inner').css({
        'width': percentStorage.toFixed(2) + '%'
    });
}

function updateCPUUsage(data) {
    var percentUsage = data.cpu.usage.used;
    $('.cpu-usage').find('.info-value').text(percentUsage.toFixed(2) + "%");
    $('.cpu-usage').find('.info-progress-inner').css({
        'width': percentUsage.toFixed(2) + '%'
    });
}