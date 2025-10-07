"""TLS helper functions."""
from datetime import datetime, timezone

from cryptography import x509

from crypto import CertificateAuthority


def validate_mutual_tls(ca: CertificateAuthority, server_serial: int, client_serial: int) -> bool:
    """Validate that both certificates exist, are not revoked and are valid for mTLS."""
    server = ca.get(server_serial)
    client = ca.get(client_serial)
    if not server or not client:
        return False
    if server.revoked or client.revoked:
        return False
    now = datetime.now(timezone.utc)
    if server.not_valid_after < now or client.not_valid_after < now:
        return False

    server_cert = x509.load_pem_x509_certificate(server.pem.encode())
    client_cert = x509.load_pem_x509_certificate(client.pem.encode())
    eku_server = server_cert.extensions.get_extension_for_class(x509.ExtendedKeyUsage)
    eku_client = client_cert.extensions.get_extension_for_class(x509.ExtendedKeyUsage)
    has_server_auth = any(oid.dotted_string == "1.3.6.1.5.5.7.3.1" for oid in eku_server.value)
    has_client_auth = any(oid.dotted_string == "1.3.6.1.5.5.7.3.2" for oid in eku_client.value)
    return has_server_auth and has_client_auth
