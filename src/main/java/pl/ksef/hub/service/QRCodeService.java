package pl.ksef.hub.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Service for generating QR codes for invoices
 */
@Slf4j
@Service
public class QRCodeService {

    private static final int QR_CODE_WIDTH = 300;
    private static final int QR_CODE_HEIGHT = 300;

    /**
     * Generate QR code as Base64 encoded PNG image
     *
     * @param data Data to encode in QR code
     * @return Base64 encoded PNG image
     */
    public String generateQRCodeBase64(String data) throws WriterException, IOException {
        byte[] qrCodeBytes = generateQRCodeBytes(data);
        return Base64.getEncoder().encodeToString(qrCodeBytes);
    }

    /**
     * Generate QR code as byte array (PNG format)
     *
     * @param data Data to encode in QR code
     * @return PNG image bytes
     */
    public byte[] generateQRCodeBytes(String data) throws WriterException, IOException {
        BitMatrix bitMatrix = generateBitMatrix(data);
        BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            ImageIO.write(image, "PNG", outputStream);
            return outputStream.toByteArray();
        }
    }

    /**
     * Generate BitMatrix for QR code
     *
     * @param data Data to encode
     * @return BitMatrix representation of QR code
     */
    private BitMatrix generateBitMatrix(String data) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return qrCodeWriter.encode(data, BarcodeFormat.QR_CODE, QR_CODE_WIDTH, QR_CODE_HEIGHT, hints);
    }

    /**
     * Generate QR code for KSeF invoice
     *
     * @param ksefNumber KSeF reference number
     * @param invoiceNumber Invoice number
     * @param grossAmount Gross amount
     * @return Base64 encoded QR code
     */
    public String generateInvoiceQRCode(String ksefNumber, String invoiceNumber, String grossAmount) 
            throws WriterException, IOException {
        String qrData = String.format("KSeF:%s|Invoice:%s|Amount:%s", ksefNumber, invoiceNumber, grossAmount);
        return generateQRCodeBase64(qrData);
    }
}
