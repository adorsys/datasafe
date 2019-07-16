# This is Datasafe-UI, simple user interface

Use `npm run-script ng:serve:web` for local development
Use `npm run-script start` for electron development

### Notes
- API url and credentials are provided by env.js file (API_URL, API_USERNAME, API_PASSWORD). 
Credentials (API_USERNAME, API_PASSWORD) are intended for local use only. 
This file will be available within `dist` folder after `ng build` so there is no need to
recompile just to change variables.
If one wants to override them he can change [env.js](datasafe-ui/src/env.js)