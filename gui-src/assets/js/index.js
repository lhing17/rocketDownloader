const $ = require('jquery')
const settings = require('electron-settings')
const {shell, remote} = require('electron')
const path = require('path')

const {BrowserWindow} = require('electron').remote
// 点击logo跳转到网站
$("#logo").click(() => {
    shell.openExternal(settings.get('officialWebsite'))
})

// 点击help弹出关于信息
$("#help").click(() => {
    const modalPath = path.join('file://', __dirname, '../../sections/windows/information.html')
    let mainWindow = remote.getCurrentWindow()
    let win = new BrowserWindow({
        parent: mainWindow,
        modal: true,
        show: false,
        frame: false,
        width: 600,
        height: 400,
        webPreferences: {
            nodeIntegration: true
        }
    })

    win.on('close', () => {
        win = null
    })

    win.loadURL(modalPath)
    win.once('ready-to-show', () => {
        win.show()
    })
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
