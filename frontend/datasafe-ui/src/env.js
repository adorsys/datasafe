(function (window) {
    window.__env = window.__env || {};

    window.__env.apiUrl = '${API_URL:http://localhost:8080}';
    // ideally these are not necessary, but API is protected, so supplying it for docker-local deployment
    window.__env.apiUsername = '${API_USERNAME:username}';
    window.__env.apiPassword = '${API_PASSWORD:password}';

}(this));