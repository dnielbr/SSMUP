package com.br.ssmup.pdf.service;

import com.br.ssmup.pdf.component.PdfBuilder;
import com.br.ssmup.empresa.cadastro.dto.EmpresaResponseDto;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaResponseDto;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.properties.HorizontalAlignment;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class GeradorPdfService {

    private final PdfBuilder pdfBuilder;

    public GeradorPdfService(PdfBuilder pdfBuilder) {
        this.pdfBuilder = pdfBuilder;
    }

    public byte[] gerarLicensaSanitariaPdf(EmpresaResponseDto empresa, LicensaSanitariaResponseDto licensa){

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdfDocument = new PdfDocument(writer);
        Document document = new Document(pdfDocument, PageSize.A4);
        document.setMargins(60,50,70,50);

        PdfFont font = pdfBuilder.getFont();
        PdfFont boldFont = pdfBuilder.getBoldFont();

//        Image logo = pdfBuilder.loadImage("/static/images/logo.png", 150, 140, HorizontalAlignment.LEFT);
//        if (logo != null){
//            logo.setFixedPosition(30,750);
//            document.add(logo);
//        }

        Image brasao = pdfBuilder.loadImage("/static/images/brasao.png", 150, 150, HorizontalAlignment.CENTER);
        if (brasao != null){
            document.add(brasao);
            brasao.setMarginBottom(0);
        }

        document.add(pdfBuilder.buildHeader(font));
        document.add(pdfBuilder.buildTitle(boldFont));
        document.add(pdfBuilder.buildBody(empresa, licensa, font));
        document.add(pdfBuilder.buildFooter(licensa.dataEmissao(), font, boldFont));

        document.close();
        return baos.toByteArray();
    }

}
