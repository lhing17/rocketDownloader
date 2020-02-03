const $ = require('jquery')

// 将index.html中所有区域加载进来
$("link[rel='import']").each(function (index) {
    let content = $('.task_template', $(this)[0].import)[0].content
    let $aside = $('.aside')
    $(content).clone().appendTo($aside)
})
