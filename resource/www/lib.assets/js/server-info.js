function waitingForServerUp()
{
  $('.waiting-server-up').css({'display':'block'});
}
function updateDashboard(message)
{
  $('.waiting-server-up').css({'display':'none'});
}
$(document).ready(function(e){
    loadData();
    setInterval(function(e2){
      reloadData();
    }, 120000);
  $(document).on('click', '.tile.reboot a', function(e){
    if(confirm('Are you sure you want to reboot the OS?'))
    {
      $.ajax({
        url:'api/reboot', 
        type:'POST',
        data:{'confirm':'yes'},
        dataType:'json',
        success:function(data){
          waitingForServerUp();
        },
        error:function(err){
          waitingForServerUp();
        }
      });
    }
  })
  $(document).on('click', '.tile.restart a', function(e){
    if(confirm('Are you sure you want to restart the service?'))
    {
      $.ajax({
        url:'api/restart', 
        type:'POST',
        data:{'confirm':'yes'},
        dataType:'json',
        success:function(data){
          waitingForServerUp();
        },
        error:function(err){
          waitingForServerUp();
        }
      });
    }
  });

  $(document).on('click', '.tile.cleaner a', function(e){
    if(confirm('Are you sure you want to clean up the service?'))
    {
      $.ajax({
        url:'api/cleanup', 
        type:'POST',
        data:{'confirm':'yes'},
        dataType:'json',
        success:function(data){
          console.log('DATA : '+JSON.stringify(data));
        }
      });
    }
  });
});
  
function loadData()
{
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
function reloadData()
{
  $.ajax({
    type: "GET",
    url: "data/server-info/get",
    dataType: "json",
    success: function (data) {
      $('.waiting-server-up').css({'display':'none'});
      updateCPUUsage(data);
      updateRAMStatus(data);
      updateSWAPStatus(data);
      updateStorageStatus(data);
      updateCPUTemperatureStatus(data);
    }
  });
}

function createCPUCore(data)
{
  $('.cpu-temperature-container').empty();
  for(var i in data.cpu.temperature)
  {
    var core = data.cpu.temperature[i];
    var label = core.label;
    var percent = 100 * core.value.currentTemperature / 150;
    var cls = label.split(' ').join('-').toLowerCase();
    var html = '<div class="server-into-item '+cls+'">\r\n'+
    '<div class="info-label">'+label+'</div>\r\n'+
    '<div class="info-value">'+core.raw.currentTemperature+'</div>\r\n'+
    '<div class="info-progress">\r\n'+
    '  <div class="info-progress-inner" style="width:'+percent+'%"></div>\r\n'+
    '</div>\r\n'+
    '</div>\r\n';
    $('.cpu-temperature-container').append(html);
  }
}

function updateCPUTemperatureStatus(data)
{
  for(var i in data.cpu.temperature)
  {
    var core = data.cpu.temperature[i];
    var label = core.label;
    var percent = 100 * core.value.currentTemperature / 150;
    var cls = label.split(' ').join('-').toLowerCase();
    $('.cpu-temperature-container').find('.'+cls).find('.info-value').html(core.raw.currentTemperature);
    $('.cpu-temperature-container').find('.'+cls).find('.info-progress-inner').css({'width':percent+'%'});
  }
}

function updateRAMStatus(data)
{
  var percentRAM = 100 * (data.memory.ram.used/data.memory.ram.total);
  var totalRAM = data.memory.ram.total/(1024 * 1024);
  var usedRAM = data.memory.ram.used/(1024 * 1024);
  $('.ram').find('.info-value').text(percentRAM.toFixed(2)+"% ("+usedRAM.toFixed(2)+"GB / "+totalRAM.toFixed(2)+"GB"+")");
  $('.ram').find('.info-progress-inner').css({'width': percentRAM.toFixed(2)+'%'});
}

function updateSWAPStatus(data)
{
  var percentSWAP = 100 * (data.memory.swap.used/data.memory.swap.total);
  var totalSWAP = data.memory.swap.total/(1024 * 1024);
  var usedSWAP = data.memory.swap.used/(1024 * 1024);
  $('.swap').find('.info-value').text(percentSWAP.toFixed(2)+"% ("+usedSWAP.toFixed(2)+"GB / "+totalSWAP.toFixed(2)+"GB"+")");
  $('.swap').find('.info-progress-inner').css({'width': percentSWAP.toFixed(2)+'%'});
}

function updateStorageStatus(data)
{
  var percentStorage = 100 * (data.storage.used/data.storage.total);
  var totalStorage = data.storage.total/(1024 * 1024);
  var usedStorage = data.storage.used/(1024 * 1024);
  $('.storage').find('.info-value').text(percentStorage.toFixed(2)+"% ("+usedStorage.toFixed(2)+"GB / "+totalStorage.toFixed(2)+"GB"+")");
  $('.storage').find('.info-progress-inner').css({'width': percentStorage.toFixed(2)+'%'});
}

function updateCPUUsage(data)
{
  var percentUsage = data.cpu.usage.used;
  $('.cpu-usage').find('.info-value').text(percentUsage.toFixed(2)+"%");
  $('.cpu-usage').find('.info-progress-inner').css({'width': percentUsage.toFixed(2)+'%'});
}