#!/usr/bin/sh

KEYFILE=path/to/input/private-key.pem
CERTFILE=path/to/input/self-signed.pem

# Generate an RSA private key
openssl genrsa >$KEYFILE
chmod 600 $KEYFILE

# Generate a self-signed certificate based on that key
openssl req -key $KEYFILE -new -x509 -days 365 -out $CERTFILE \
    -subj "/CN=test-self-signed"
