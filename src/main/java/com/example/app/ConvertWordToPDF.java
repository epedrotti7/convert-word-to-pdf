package com.example.app;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.json.JSONObject;

import io.javalin.Javalin;
import io.javalin.http.UploadedFile;

public class ConvertWordToPDF {
    public static void main(String[] args) {

        Javalin app = Javalin.create().start(8080);

        app.post("/convert", ctx -> {

            UploadedFile file = ctx.uploadedFile("wordFile");

            if (file != null) {
                try (InputStream is = file.getContent();
                        XWPFDocument doc = new XWPFDocument(is);
                        PDDocument pdfDoc = new PDDocument()) {

                    PDPage page = new PDPage();
                    pdfDoc.addPage(page);

                    try (PDPageContentStream contentStream = new PDPageContentStream(pdfDoc, page)) {
                        contentStream.beginText();
                        contentStream.setFont(PDType1Font.TIMES_ROMAN, 12);
                        contentStream.setLeading(14.5f);
                        contentStream.newLineAtOffset(50, 700);

                        for (XWPFParagraph p : doc.getParagraphs()) {
                            String text = p.getText();
                            contentStream.showText(text);
                            contentStream.newLine();
                        }

                        contentStream.endText();
                    }

                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    pdfDoc.save(out);
                    byte[] pdfBytes = out.toByteArray();

                    String base64Encoded = Base64.getEncoder().encodeToString(pdfBytes);

                    JSONObject responseJson = new JSONObject();
                    responseJson.put("result", base64Encoded);

                    ctx.contentType("application/json");
                    ctx.result(responseJson.toString());

                } catch (Exception e) {
                    ctx.status(500).result("Error converting Word to PDF: " + e.getMessage());
                }
            } else {
                ctx.status(400).result("No file uploaded");
            }
        });
    }
}
