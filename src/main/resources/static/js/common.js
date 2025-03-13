$util = {};

$util.serializeObject = (formObj) => {
    let rs = {};

    if (formObj === null || formObj === 'undefined') { return rs;}

    $(formObj).serializeArray().forEach( (data) => {
        rs[data.name] = data.value;
    });

    return rs;
}


$util.alert = (title, msg, iconType) => {
    swal({
        title : title,
        text : msg,
        icon : iconType,        // warning, success
        timer : 10000,
        customClass : 'sweet-size',
        showConfirmButton : true
    });

}