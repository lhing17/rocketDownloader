const $ = require('jquery')

$("link[rel='import']").each(function (index) {
    let content = $('.task_template', $(this)[0].import)[0].content
    let $aside = $('.aside')
    $(content).clone().appendTo($aside)
})
