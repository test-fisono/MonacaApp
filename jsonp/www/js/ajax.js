function addScript(){
    var script = document.createElement('script');
    script.src = 'http://192.168.3.1:3000/ajax_json/json?callback=displayCustomer';
    script.type = "application/javascript";
    document.body.appendChild(script);
}

function displayCustomer(data){
    for(i = 0; i < data.length; i++){
        $("#ajax").append("<div>" + data[i].name + "</div>");
    }
}
