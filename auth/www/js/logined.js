ons.ready(function(){
    loginCheck();
});


// ログイン後処理
function loginCheck(){
    var currentUser = ncmb.User.getCurrentUser();
    if(currentUser){
        var userName = currentUser.get("userName");
        $("#currentUser").text(userName);
    }else{
        alert("未ログインまたは取得失敗");
        location.href = "index.html";
    }
}

function getName(){
    var currentUser = ncmb.User.getCurrentUser();
    var userName = currentUser.get("userName");
    $("#currentUser").text(userName);
}

function getVal(){
    alert($("#currentUser").text());
}