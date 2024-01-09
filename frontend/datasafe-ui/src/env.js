(function (window) {
    window.__env = window.__env || {};

    window.__env.apiUrl = 'http://localhost:8080';
    // ideally these are not necessary, but API is protected, so supplying it for docker-local deployment
    window.__env.apiUsername = 'username';
    window.__env.apiPassword = 'password';

}(this));