// ログイン
function login(){
    var email = $("#l_email").val();
    var password = $("#l_password").val();
    
    var user = new ncmb.User({mailAddress: email, password: password});
    
    ncmb.User.loginWithMailAddress(user)
        .then(function(data){
            location.href = "user.html";
        })
        .catch(function(err){
            alert(err.message);
        });
}

