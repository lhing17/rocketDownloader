const $ = require('jquery')
const settings = require('electron-settings')
const {shell} = require('electron')

// 点击logo跳转到网站
$("#logo").click(() => {
    shell.openExternal(settings.get('officialWebsite'))
})

// 点击左侧导航条按钮切换列表项
$("button").click(function () {
    let section = $(this).data('section')
    if (section) {
        let sectionId = section + "_list"
        hideAllListSections()
        $("#" + sectionId).addClass("is-shown")
    }
})

function hideAllListSections() {
    $(".list-section").removeClass("is-shown")
}


function activeDefaultSection() {
    $("button[data-section='task']").click()
}

activeDefaultSection()
