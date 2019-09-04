# Download application and create new profile transcripts
- Download CLI application (MacOS in this example, change curl url accordingly if other OS)

```bash
curl -L https://github.com/adorsys/datasafe/releases/download/v0.6.0/datasafe-cli-osx-x64 > datasafe-cli && chmod +x datasafe-cli
```
- Create file with your credentials (they also can be passed through commandline)

```bash
echo '{"username": "john", "password": "Doe", "systemPassword": "password"}' > john.credentials
```
- Create your new user profile (credentials come from john.credentials). You can enter value or click enter to accept 
the default value when prompted.

```bash
./datasafe-cli -c john.credentials profile create
```
