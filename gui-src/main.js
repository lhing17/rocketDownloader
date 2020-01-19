const path = require('path')
const {app, BrowserWindow} = require('electron')
const settings = require('electron-settings')


const debug = /--debug/.test(process.argv[2])

if (process.mas) app.setName('Electron APIs')

// GUI的主窗口
let mainWindow = null

function initialize() {
    makeSingleInstance()

    function createWindow() {
        const windowOptions = {
            width: 1280,
            minWidth: 680,
            height: 768,
            minHeight: 400,
            title: app.getName(),
            webPreferences: {
                nodeIntegration: true
            }
        }

        // 配置linux环境下的图标
        // if (process.platform === 'linux') {
        //     windowOptions.icon =
        // }

        mainWindow = new BrowserWindow(windowOptions)
        mainWindow.loadURL(path.join('file://', __dirname, '/index.html'))

        if (debug) {
            mainWindow.webContents.openDevTools()
            mainWindow.maximize()
            require('devtron').install()
        }

        mainWindow.on('closed', () => {
            mainWindow = null
        })
    }

    app.on('ready', () => {
        // 软件相关的设置
        settings.set('officialWebsite', 'https://github.com/lhing17/rocketDownloader')

        createWindow()
    })

    app.on('window-all-closed', () => {
        if (process.platform !== 'darwin') {
            app.quit()
        }
    })

    app.on('activate', () => {
        if (mainWindow === null) {
            createWindow()
        }
    })
}

function makeSingleInstance() {
    // MAC APP STORE
    if (process.mas) return

    app.requestSingleInstanceLock()
    app.on('second-instance', () => {
        if (mainWindow) {
            if (mainWindow.isMinimized()) mainWindow.restore()
            mainWindow.focus()
        }
    })
}

initialize()