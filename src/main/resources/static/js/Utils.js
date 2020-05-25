// var globle_url = "https://wnd.agri114.cn/cg/"
var globle_url = "http://192.168.71.134:8185/cg/"

function setCookie(key, value, expiredays) {
    var exdate = new Date()
    //默认保存10天
    if(expiredays==null){
        expiredays = 30
    }
    exdate.setDate(exdate.getDate() + expiredays)
    var s = key + "=" + escape(value) +
        ((expiredays == null) ? "" : ";expires=" + exdate.toGMTString())
    document.cookie = s;
}

//取回cookie
function getCookie(key) {
    if (document.cookie.length > 0) {
        c_start = document.cookie.indexOf(key + "=")
        if (c_start != -1) {
            c_start = c_start + key.length + 1
            c_end = document.cookie.indexOf(";", c_start)
            if (c_end == -1) c_end = document.cookie.length
            return unescape(document.cookie.substring(c_start, c_end))
        }
    }
    return ""
}
function removeCookie(key) {
    var exdate = new Date()
    exdate.setDate(exdate.getDate() -1)
    var s = key + "=" + ";expires=" + exdate.toGMTString()
    log("s="+s)
    document.cookie = s;
}

function clearCookie() {

    var domain = '.'+location.host;
    var keys = document.cookie.match(/[^ =;]+(?=\=)/g);
    if(keys) {
        for(var i = keys.length; i--;) {
            var date=new Date();
            date.setTime( new Date((date/1000-86400*11)*1000));
            document.cookie=keys[i]+"=; expire="+date.toUTCString();
        }
    }
}