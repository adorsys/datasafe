# Encrypting/decrypting data animation transcript

- Create some unencrypted content

```bash
echo "Hello world" > unencrypted.txt
```
- Encrypt and store file from above in privatespace. In privatespace it will have decrypted name `secret.txt`
```bash
./datasafe-cli -c john.credentials private cp unencrypted.txt secret.txt
```
- Show that filename is encrypted in privatespace:

```bash
ls private
```

- Show that file content is encrypted too:

```bash
cat private/encrypted_file_name_from_above
```

- Decrypt file content:

```bash
./datasafe-cli -c john.credentials private cat secret.txt
```
