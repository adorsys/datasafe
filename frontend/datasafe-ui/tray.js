const electron = require('electron');
const path = require('path');
const app = electron.app;
let tray = null;

exports.create = function(mainWindow) {
  if (process.platform === 'darwin' || tray) {
    return;
  }

  const iconPath = path.join(__dirname, '/assets/angular-logo.png');
  const toggleApp = function(){
    if (mainWindow.isVisible()) {
      mainWindow.hide();
    } else {
      mainWindow.show();
    }
  };

  const contextMenu = electron.Menu.buildFromTemplate([
    {
      label: 'Restore Electron Angular Materiall',
      click() {
        toggleApp();
      }
    },
    {
      type: 'separator'
    },
    {
      label: 'Quit',
      click() {
        app.quit();
      }
    }
  ]);

  tray = new electron.Tray(iconPath);
  tray.setToolTip('Electron Angular Material');
  tray.setContextMenu(contextMenu);
  tray.on('click', toggleApp);
};
