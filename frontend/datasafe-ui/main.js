const {app, BrowserWindow, ipcMain} = require('electron')
const url = require("url");
const path = require("path");
const log = require('electron-log');
const { autoUpdater } = require("electron-updater");
const tray = require('./tray');

autoUpdater.logger = log;
autoUpdater.logger.transports.file.level = 'info';
log.info('App starting...');

let mainWindow

/**
var express = require('express');
var server = express();
let dir = path.join(__dirname, './dist/datasafe-electron-app');
server.use('/', express.static(dir));
server.listen(4200);
*/

function createWindow () {
  mainWindow = new BrowserWindow({
    width: 800,
    height: 600,
    webPreferences: {
      nodeIntegration: true
    }
  })


  mainWindow.loadURL(
    url.format({
      //pathname:path.join(__dirname, 'dist/index.html'),
      pathname:path.join(__dirname, './dist/datasafe-electron-app/index.html'),
      protocol: "file:",
      slashes: true
    })
  );

  //mainWindow.loadURL('http://localhost:4200');

  //mainWindow.loadURL(`file://${__dirname}/src/index.html#v${app.getVersion()}`);

  tray.create(mainWindow);

  // Open the DevTools.
  mainWindow.webContents.openDevTools()

  mainWindow.on('closed', function () {
    mainWindow = null
  })
}

app.on('ready', createWindow)

// Quit when all windows are closed.
app.on('window-all-closed', function () {
  // On OS X it is common for applications and their menu bar
  // to stay active until the user quits explicitly with Cmd + Q
  if (process.platform !== 'darwin') {
    app.quit()
  }
})

app.on('activate', function () {
  // On OS X it's common to re-create a window in the app when the
  // dock icon is clicked and there are no other windows open.
  if (mainWindow === null) {
    createWindow()
  }
})


ipcMain.on("navigateDirectory", (event, path) => {
  process.chdir(path);
});
