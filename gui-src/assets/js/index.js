const $ = require('jquery')

$("#test").click(() => {
    console.log("点了一下")
    $.ajax({
        url: 'http://localhost:8080/',
        method: 'post',
        data: {name: "zs"},
        success: function () {
            console.log("请求成功");
        }
    })
})