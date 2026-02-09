# Konfiguracja certyfikatu kwalifikowanego dla KSeF

## Dla środowiska produkcyjnego

1. **Kup certyfikat kwalifikowany** od zaufanego dostawcy:
   - Certum (Asseco)
   - Szafir (KIR)
   - mSignature (mBank)
   - Inne certyfikaty zgodne z eIDAS

2. **Certyfikat musi być w formacie PKCS12** (.p12 lub .pfx)

3. **Skonfiguruj w application.yml lub zmiennych środowiskowych**:

   ```bash
   export KSEF_KEYSTORE_PATH=/path/to/certificate.p12
   export KSEF_KEYSTORE_PASSWORD=your_keystore_password
   export KSEF_KEY_ALIAS=certificate_alias
   export KSEF_KEY_PASSWORD=your_key_password
   ```

4. **Włącz podpisywanie** w `application.yml`:
   ```yaml
   ksef:
     signature:
       enabled: true
   ```

## Dla środowiska testowego (DEMO)

KSeF DEMO nie wymaga prawdziwego certyfikatu kwalifikowanego.
Możesz wygenerować samopodpisany certyfikat do testów:

### Windows (PowerShell):

```powershell
# Generuj certyfikat testowy
$cert = New-SelfSignedCertificate -Subject "CN=Test KSeF, O=Test Company, C=PL" `
    -CertStoreLocation "Cert:\CurrentUser\My" `
    -KeyExportPolicy Exportable `
    -KeySpec Signature `
    -KeyLength 2048 `
    -KeyAlgorithm RSA `
    -HashAlgorithm SHA256 `
    -NotAfter (Get-Date).AddYears(2)

# Eksportuj do PKCS12
$password = ConvertTo-SecureString -String "test123" -Force -AsPlainText
Export-PfxCertificate -Cert $cert -FilePath "ksef-test.p12" -Password $password

Write-Host "Certyfikat zapisany jako: ksef-test.p12"
Write-Host "Hasło: test123"
```

### Linux/Mac (OpenSSL):

```bash
# Generuj klucz prywatny i certyfikat
openssl req -x509 -newkey rsa:2048 -keyout key.pem -out cert.pem -days 730 -nodes \
  -subj "/C=PL/O=Test Company/CN=Test KSeF"

# Konwertuj do PKCS12
openssl pkcs12 -export -out ksef-test.p12 -inkey key.pem -in cert.pem \
  -password pass:test123

echo "Certyfikat zapisany jako: ksef-test.p12"
echo "Hasło: test123"
```

### Konfiguracja testowego certyfikatu:

```yaml
# application.yml lub application-dev.yml
ksef:
  signature:
    enabled: true
    keystore:
      path: ./ksef-test.p12
      password: test123
    key:
      alias: 1 # Dla samopodpisanych często to jest "1"
      password: test123
```

## Sprawdzenie certyfikatu

### Windows (PowerShell):

```powershell
# Lista aliasów w keystore
keytool -list -v -keystore ksef-test.p12 -storetype PKCS12 -storepass test123
```

### Linux/Mac:

```bash
# Informacje o certyfikacie
openssl pkcs12 -info -in ksef-test.p12 -nodes -passin pass:test123
```

## Weryfikacja w aplikacji

Po uruchomieniu aplikacji, możesz sprawdzić status certyfikatu:

```bash
# Endpoint do sprawdzenia certyfikatu (jeśli zaimplementowany)
curl http://localhost:8080/api/ksef/certificate/info
```

## Bezpieczeństwo

⚠️ **NIGDY nie commituj certyfikatów do repozytorium Git!**

Dodaj do `.gitignore`:

```
*.p12
*.pfx
*.pem
*.key
```

Używaj zmiennych środowiskowych lub secret management (Azure Key Vault, AWS Secrets Manager, HashiCorp Vault).

## Troubleshooting

### Problem: "Certificate or private key not found"

- Sprawdź alias certyfikatu: `keytool -list -keystore cert.p12`
- Upewnij się że hasło jest poprawne

### Problem: "Signature verification failed"

- Certyfikat może być wygasły
- Sprawdź `notBefore` i `notAfter` w informacjach o certyfikacie

### Problem: "KeyStore type not supported"

- Upewnij się że plik jest w formacie PKCS12
- Rozszerzenie pliku: `.p12` lub `.pfx`
