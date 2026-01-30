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

$(document).ready(() => {
    $('#reservationButton').click(() => {
        reservation();
    });

    $('a[data-bs-toggle="modal"]').bind('click', () => {
        $('#modalDiv').css('display','block')
    });

    $('button[data-bs-dismiss="modal"]').bind('click', () => {
         $('#modalDiv').css('display','none')
    });
});

$(document).keydown(function(event) {
    if ( event.keyCode == 27 || event.which == 27 ) {
        $('#modalDiv').css('display','none')
    }
});

var phoneCheck = (phoneNumber) => {
  phoneNumber.value = phoneNumber.value.replace(/[^0-9]/g, '').replace(/(^02.{0}|^01.{1}|[0-9]{3,4})([0-9]{3,4})([0-9]{4})/g, "$1-$2-$3");
}


var reservation = () => {

  let formData = $util.serializeObject($('#contactForm'));

  $.ajax({
      type : 'post',
      url : '/api/v1/reservation.do',
      async : true,
      headers : {
          "Content-Type" : "application/json",
          "X-HTTP-Method-Override" : "POST"
      },
      dataType : 'text',
      data : JSON.stringify(formData),
      success : function(result){
        debugger;
        result == 'Y' ? $util.alert('', '최대한 빠르게 연락드리겠습니다. 감사합니다.', 'success') : $util.alert('다시 시도해주세요.', '', 'warning');
      },
      error : function (request, status, error) {
        $util.alert('다시 시도해주세요.', '', 'warning');
      }
  });
}
