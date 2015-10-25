/* 新規ユーザーの作成 */
function createUser(){
    var email = $("#c_email").val();
    var password = $("#c_password").val();
    
    //３つの入力項目が入力されてなかった場合はエラーで処理終了
    var err = validateUser(email, password);
    if(err){
        alert(err + "が入力されていません。");
        return;
    }
    
    var user = new ncmb.User();
    
    user.set("mailAddress", email)
        .set("password", password);
        
    user.signUpByAccount()
        .then(function(){
            alert("会員登録完了！");
            ncmb.User.requestSignUpEmail(email)
                .then(function(){
                    alert("確認メールを送信しました。");
                })
                .catch(function(err){
                    alert(err.message);
                });
        })
        .catch(function(err){
            alert("エラーが発生しました！");
        });
}

/* ユーザー作成時の必須チェック */
function validateUser(email, password){
    var errMessageAry = [];
    if(!email){
        errMessageAry.push("メールアドレス");
    }
    if(!password){
        errMessageAry.push("パスワード");
    }
    
    var errMessage = "";
    if(errMessageAry){
        errMessage = errMessageAry.join("・");
    }
    return errMessage;
}