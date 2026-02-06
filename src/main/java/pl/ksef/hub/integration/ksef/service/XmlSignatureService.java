package pl.ksef.hub.integration.ksef.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.xml.security.Init;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

/**
 * Serwis do podpisywania XML kwalifikowanym certyfikatem elektronicznym
 * Zgodnie z wymaganiami KSeF (XMLDSig)
 */
@Service
@Slf4j
public class XmlSignatureService {

    static {
        // Inicjalizacja Apache Santuario XMLSec
        Init.init();
    }

    @Value("${ksef.signature.keystore.path:#{null}}")
    private String keystorePath;

    @Value("${ksef.signature.keystore.password:#{null}}")
    private String keystorePassword;

    @Value("${ksef.signature.key.alias:#{null}}")
    private String keyAlias;

    @Value("${ksef.signature.key.password:#{null}}")
    private String keyPassword;

    @Value("${ksef.signature.enabled:false}")
    private boolean signatureEnabled;

    /**
     * Podpisuje dokument XML kwalifikowanym certyfikatem
     * 
     * @param xmlContent Treść XML do podpisania
     * @return Podpisany XML
     * @throws Exception w przypadku błędu podpisywania
     */
    public String signXml(String xmlContent) throws Exception {
        if (!signatureEnabled) {
            log.warn("XML signature is disabled. Returning unsigned XML.");
            log.warn("To enable signature, configure ksef.signature.* properties and set ksef.signature.enabled=true");
            return xmlContent;
        }

        if (keystorePath == null || keystorePassword == null || keyAlias == null || keyPassword == null) {
            throw new IllegalStateException(
                "Certificate configuration is incomplete. Please configure: " +
                "ksef.signature.keystore.path, ksef.signature.keystore.password, " +
                "ksef.signature.key.alias, ksef.signature.key.password"
            );
        }

        log.debug("Signing XML document with certificate from keystore: {}", keystorePath);

        // 1. Wczytaj certyfikat i klucz prywatny
        KeyStore keyStore = loadKeyStore();
        PrivateKey privateKey = (PrivateKey) keyStore.getKey(keyAlias, keyPassword.toCharArray());
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate(keyAlias);

        if (privateKey == null || certificate == null) {
            throw new IllegalStateException("Certificate or private key not found in keystore for alias: " + keyAlias);
        }

        log.debug("Certificate loaded successfully. Subject: {}", certificate.getSubjectX500Principal().getName());

        // 2. Parse XML do Document
        Document doc = parseXmlToDocument(xmlContent);

        // 3. Utwórz XMLSignature
        XMLSignature signature = new XMLSignature(
            doc,
            "", // Base URI
            XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256 // Algorytm zgodny z KSeF
        );

        // 4. Dodaj sygnaturę jako ostatni element w root
        Element root = doc.getDocumentElement();
        root.appendChild(signature.getElement());

        // 5. Dodaj transformacje (enveloped signature)
        Transforms transforms = new Transforms(doc);
        transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
        transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

        // 6. Dodaj Reference do całego dokumentu
        signature.addDocument(
            "", // Referencja do całego dokumentu (pusty URI = cały dokument)
            transforms,
            "http://www.w3.org/2001/04/xmlenc#sha256" // SHA-256 dla digest
        );

        // 7. Dodaj KeyInfo z certyfikatem
        signature.addKeyInfo(certificate);

        // 8. Podpisz dokument
        signature.sign(privateKey);

        log.info("XML document signed successfully");

        // 9. Konwertuj z powrotem do String
        return documentToString(doc);
    }

    /**
     * Weryfikuje podpis XML
     * 
     * @param signedXml Podpisany XML
     * @return true jeśli podpis jest poprawny
     */
    public boolean verifySignature(String signedXml) {
        try {
            Document doc = parseXmlToDocument(signedXml);
            
            // Znajdź element Signature
            Element signatureElement = (Element) doc.getElementsByTagNameNS(
                Constants.SignatureSpecNS, 
                "Signature"
            ).item(0);

            if (signatureElement == null) {
                log.warn("No signature element found in XML");
                return false;
            }

            XMLSignature signature = new XMLSignature(signatureElement, "");
            
            // Pobierz certyfikat z KeyInfo
            X509Certificate cert = signature.getKeyInfo().getX509Certificate();
            
            if (cert == null) {
                log.warn("No certificate found in signature");
                return false;
            }

            // Weryfikuj podpis
            boolean isValid = signature.checkSignatureValue(cert);
            
            if (isValid) {
                log.info("Signature verification: VALID");
            } else {
                log.warn("Signature verification: INVALID");
            }
            
            return isValid;
        } catch (Exception e) {
            log.error("Error verifying signature: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Sprawdza czy certyfikat jest skonfigurowany i dostępny
     */
    public boolean isCertificateConfigured() {
        if (!signatureEnabled) {
            return false;
        }
        try {
            KeyStore keyStore = loadKeyStore();
            return keyStore.containsAlias(keyAlias);
        } catch (Exception e) {
            log.error("Error checking certificate configuration: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Pobiera informacje o certyfikacie
     */
    public String getCertificateInfo() {
        if (!signatureEnabled || keystorePath == null) {
            return "Certificate not configured";
        }
        
        try {
            KeyStore keyStore = loadKeyStore();
            X509Certificate certificate = (X509Certificate) keyStore.getCertificate(keyAlias);
            
            if (certificate == null) {
                return "Certificate not found for alias: " + keyAlias;
            }
            
            return String.format(
                "Subject: %s\nIssuer: %s\nValid from: %s to: %s\nSerial: %s",
                certificate.getSubjectX500Principal().getName(),
                certificate.getIssuerX500Principal().getName(),
                certificate.getNotBefore(),
                certificate.getNotAfter(),
                certificate.getSerialNumber()
            );
        } catch (Exception e) {
            return "Error reading certificate: " + e.getMessage();
        }
    }

    // === Metody pomocnicze ===

    private KeyStore loadKeyStore() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            keyStore.load(fis, keystorePassword.toCharArray());
        }
        return keyStore;
    }

    private Document parseXmlToDocument(String xmlContent) throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true); // Ważne dla XML Signature
        DocumentBuilder builder = factory.newDocumentBuilder();
        return builder.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));
    }

    private String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }
}
