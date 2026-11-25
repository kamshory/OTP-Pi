var count = 0;
var flag = false;
var $errorMessage = $('.error-message');

function showErr(message) {
  $errorMessage.text(message).css({'opacity': 1});
}

function setErr(dom, message) {
  $errorMessage.text(message).css({'opacity': 1});
  dom.addClass('input-error');
}

function clearErr(dom) {
  $errorMessage.css({'opacity': 0});
  dom.removeClass('input-error');
}

function getUrlParam(name) {
  var reg = new RegExp("(^|&)" + name + "=([^&]*)(&|$)");
  var url = window.location.search.substr(1).match(reg);
  if (url !== null) {
    return unescape(url[2]);
  }
  return null;
}

function validEmail(email) {
  const reg = /^(([^<>()[\]\\.,;:\s@\"]+(\.[^<>()[\]\\.,;:\s@\"]+)*)|(\".+\"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/;
  return reg.test(email);
}

function verifyPhone(phone) {
  const reg = /^[\+]?[(]?[0-9]{3}[)]?[-\s\.]?[0-9]{3}[-\s\.]?[0-9]{4,9}$/im
  return reg.test(phone);
}

function showMessage(message) {
  var $msg = $('<div class="msg-box">' +
    '<div class="warning">!</div>' +
    '<p>' + message + '</p>' +
    '</div>');
  $msg.appendTo(document.body);
  setTimeout(function () {
    $msg.remove();
  }, 2e3);
}

function showLoading() {
  var $loading = $('<div class="loading" id="loading">' +
    '<img src="lib.assets/images/loading.gif">' +
    '</div>');
  setTimeout(function () {
    $loading.appendTo(document.body);
  }, 200);
}

function hideLoading() {
  setTimeout(function () {
    var child = document.getElementById('loading');
    document.body.removeChild(child);
  }, 500);
}

