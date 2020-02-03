const $ = require('jquery')
const settings = require('electron-settings')
const {shell, remote} = require('electron')
const path = require('path')

const {BrowserWindow} = require('electron').remote
// 点击logo跳转到网站
$("#logo").click(() => {
    shell.openExternal(settings.get('officialWebsite'))
})

function openNewModal(filePath, width, height) {
    const modalPath = path.join('file://', __dirname, filePath)
    let mainWindow = remote.getCurrentWindow()
    let win = new BrowserWindow({
        parent: mainWindow,
        modal: true,
        show: false,
        frame: false,
        width: width || 600,
        height: height || 400,
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
}

$("#new_task").click(() => {
    openNewModal('../../sections/windows/new_task.html', 600, 300);
})

// 点击help弹出关于信息
$("#help").click(() => {
    openNewModal('../../sections/windows/information.html', 600, 400)
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
