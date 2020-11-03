/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var loadTime = new Date(); //global var 
//alert(loadTime);
function onloadHandler() {
    window.alert("Welcome to TotalBuy!");
}

function helloHandler() {
    var now = new Date(); //local var 
    
    var nameInput = document.getElementById("name");
    var welMsg = '歡迎, ' + nameInput.value + '! <br> 載入時間是:'+loadTime+'<br>現在是:' + now;
    
    var helloDiv = document.getElementById("helloDiv");
    helloDiv.innerHTML = welMsg;
    
    //alert('歡迎,載入時間是:'+loadTime+'現在是:' + now);
    
    //now = 100;
    //alert(now);
    //alert(1+2);
}

//document.write('歡迎,載入時間是:'+loadTime);

