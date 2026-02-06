package pl.ksef.hub.integration.ksef.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.xml.security.signature.XMLSignature;
import org.apache.xml.security.transforms.Transforms;
import org.apache.xml.security.utils.Constants;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import pl.ksef.hub.domain.entity.Certificate;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * Service do podpisywania XML certyfikatem kwalifikowanym
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class KsefSignatureService {

    static {
        // Inicjalizacja Apache Santuario
        org.apache.xml.security.Init.init();
    }

    /**
     * Podpisuje XML certyfikatem kwalifikowanym
     * 
     * @param xmlContent XML do podpisania
     * @param certificate Certyfikat z bazy danych
     * @param keystorePassword Hasło do keystore
     * @return Podpisany XML
     */
    public String signXml(String xmlContent, Certificate certificate, String keystorePassword) {
        log.debug("Signing XML with certificate: {}", certificate.getSubjectDn());

        try {
            // Parsuj XML
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(xmlContent.getBytes("UTF-8")));

            // Załaduj certyfikat i klucz prywatny z KeyStore
            KeyStore keyStore = loadKeyStore(certificate, keystorePassword);
            String alias = certificate.getSerialNumber(); // lub inny alias
            
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(alias, keystorePassword.toCharArray());
            X509Certificate cert = (X509Certificate) keyStore.getCertificate(alias);

            // Utwórz podpis XML
            XMLSignature signature = new XMLSignature(doc, "", XMLSignature.ALGO_ID_SIGNATURE_RSA_SHA256);
            Element root = doc.getDocumentElement();
            root.appendChild(signature.getElement());

            // Dodaj transformacje
            Transforms transforms = new Transforms(doc);
            transforms.addTransform(Transforms.TRANSFORM_ENVELOPED_SIGNATURE);
            transforms.addTransform(Transforms.TRANSFORM_C14N_EXCL_OMIT_COMMENTS);

            // Dodaj referencję do całego dokumentu
            signature.addDocument("", transforms, "http://www.w3.org/2001/04/xmlenc#sha256");

            // Dodaj informacje o certyfikacie
            signature.addKeyInfo(cert);

            // Podpisz
            signature.sign(privateKey);

            // Konwertuj z powrotem do String
            return documentToString(doc);

        } catch (Exception e) {
            log.error("Failed to sign XML", e);
            throw new RuntimeException("Failed to sign XML: " + e.getMessage(), e);
        }
    }

    /**
     * Weryfikuje podpis XML
     */
    public boolean verifySignature(String signedXml) {
        log.debug("Verifying XML signature");

        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(new ByteArrayInputStream(signedXml.getBytes("UTF-8")));

            // Znajdź element Signature
            Element signatureElement = (Element) doc.getElementsByTagNameNS(
                    Constants.SignatureSpecNS, "Signature").item(0);

            if (signatureElement == null) {
                log.warn("No signature found in XML");
                return false;
            }

            XMLSignature signature = new XMLSignature(signatureElement, "");
            
            // Weryfikuj z użyciem certyfikatu z podpisu
            return signature.checkSignatureValue(signature.getKeyInfo().getPublicKey());

        } catch (Exception e) {
            log.error("Failed to verify signature", e);
            return false;
        }
    }

    /**
     * Ładuje KeyStore z certyfikatu
     */
    private KeyStore loadKeyStore(Certificate certificate, String password) throws Exception {
        // Dekoduj dane certyfikatu z Base64
        byte[] keystoreBytes = Base64.getDecoder().decode(certificate.getCertificateData());

        // Załaduj KeyStore
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        keyStore.load(new ByteArrayInputStream(keystoreBytes), password.toCharArray());

        return keyStore;
    }

    /**
     * Konwertuje Document do String
     */
    private String documentToString(Document doc) throws Exception {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer = tf.newTransformer();
        StringWriter writer = new StringWriter();
        transformer.transform(new DOMSource(doc), new StreamResult(writer));
        return writer.getBuffer().toString();
    }

    /**
     * Sprawdza czy certyfikat jest ważny
     */
    public boolean isCertificateValid(Certificate certificate) {
        if (certificate.getExpiresAt() == null) {
            return false;
        }
        
        return certificate.getExpiresAt().isAfter(java.time.LocalDateTime.now());
    }
}
