function newDateString(tm) {
    return moment(tm).format();
}
$(document).ready(function(e){
    
    prepareTimeSelector();
    $(document).on('change', '#time', function(e){
        loadChart();
    });
    
    loadChart();
});
function prepareTimeSelector()
{
    var days = ['Sunday', 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday'];
    var months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
    var d = new Date();
    d.setHours(0);
    d.setMinutes(0);
    d.setSeconds(0);
    d.setMilliseconds(0);
    var now = d.getTime();
    var oneday = 86400000;
    var maxDay = 7;
    for(var i = 0; i<maxDay; i++)
    {
        var dif1 = now - ((i + 0) * oneday);
        var dif2 = now - ((i - 1) * oneday);
        d.setTime(dif1);
        var str = days[d.getDay()] + ' '+d.getDate()+' '+months[d.getMonth()]+' '+d.getFullYear();
        var option = '';
        if(i > 0)
        {
            option = '<option value="'+dif1+','+dif2+'">'+str+'</option>';
        }
        else
        {
            option = '<option data-reload-data="true" value="'+dif1+','+dif2+'">'+str+'</option>';
        }
        $('#time').append(option);
    }
}
function loadChart()
{
    $.ajax({
        type: "GET",
        url: "data/server-status/get",
        dataType: "json",
        data:{time:$('#time').val()},
        success: function (data) {
            applyData(data);
            var ctx = document.getElementById('canvas').getContext('2d');
            window.myLine = new Chart(ctx, config);
            myLine.resize(600, 1200);
        }
    });
    
}
function applyData(data)
{
    var cpu = {label:'CPU', fill:false, data:[]};
    var ram = {label:'RAM', fill:false, data:[]};
    var swap = {label:'SWAP', fill:false, data:[]};
    var storage = {label:'Storage', fill:false, data:[]};
    var i;
    var j;
    for(i in data)
    {
        cpu.data.push({x:data[i].datetime,y:data[i].cpu})
        ram.data.push({x:data[i].datetime,y:data[i].ram})
        swap.data.push({x:data[i].datetime,y:data[i].swap})
        storage.data.push({x:data[i].datetime,y:data[i].storage})
    }
    config.data.datasets = [cpu, ram, swap, storage];
    config.data.datasets[0].backgroundColor = color(chartColors.red).alpha(0.5).rgbString();
    config.data.datasets[0].borderColor = chartColors.red;
    
    config.data.datasets[1].backgroundColor = color(chartColors.orange).alpha(0.5).rgbString();
    config.data.datasets[1].borderColor = chartColors.orange;
    
    config.data.datasets[2].backgroundColor = color(chartColors.yellow).alpha(0.5).rgbString();
    config.data.datasets[2].borderColor = chartColors.yellow;
    
    config.data.datasets[3].backgroundColor = color(chartColors.green).alpha(0.5).rgbString();
    config.data.datasets[3].borderColor = chartColors.green;
    
    /**
    config.data.datasets[4].backgroundColor = color(chartColors.blue).alpha(0.5).rgbString();
    config.data.datasets[4].borderColor = chartColors.blue;
    config.data.datasets[5].backgroundColor = color(chartColors.grey).alpha(0.5).rgbString();
    config.data.datasets[5].borderColor = chartColors.grey;
    */
    
    for(i = 0; i<config.data.datasets.length; i++)
    {
        for(j = 0; j < config.data.datasets[i].data.length; j++)
        {
            config.data.datasets[i].data[j].x = newDateString(config.data.datasets[i].data[j].x);
        }
    }
}
var chartColors = {
    red: 'rgb(255, 99, 132)',
    orange: 'rgb(255, 159, 64)',
    yellow: 'rgb(255, 205, 86)',
    green: 'rgb(75, 192, 192)',
    blue: 'rgb(54, 162, 235)',
    purple: 'rgb(153, 102, 255)',
    grey: 'rgb(201, 203, 207)'
};

var color = Chart.helpers.color;
var config = {
    type: 'line',
    data: {
        datasets: []
    },
    options: {
        responsive: true,
        title: {
            display: true,
            text: 'Server Status'
        },
        scales: {
            xAxes: [{
                type: 'time',
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Time'
                },
                ticks: {
                    major: {
                        fontStyle: 'bold',
                        fontColor: '#FF0000'
                    }
                }
            }],
            yAxes: [{
                display: true,
                scaleLabel: {
                    display: true,
                    labelString: 'Usage (%)'
                }
            }]
        }
    }
};
