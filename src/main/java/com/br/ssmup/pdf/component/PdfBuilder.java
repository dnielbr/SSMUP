package com.br.ssmup.pdf.component;

import com.br.ssmup.empresa.cadastro.dto.EmpresaResponseDto;
import com.br.ssmup.empresa.licensa.dto.LicensaSanitariaResponseDto;
import com.itextpdf.io.font.constants.StandardFonts;
import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.element.Div;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.IBlockElement;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
public class PdfBuilder {

    private final float FONT_SIZE_DEFAULT = 12f;
    private final float FONT_SIZE_TITLE = 14f;
    private final float FONT_SIZE_SMALL = 11f;

    // CRIA FONTES NOVAS PARA CADA PDF
    public PdfFont getFont() {
        try {
            return PdfFontFactory.createFont(StandardFonts.TIMES_ROMAN);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public PdfFont getBoldFont() {
        try {
            return PdfFontFactory.createFont(StandardFonts.TIMES_BOLD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Paragraph buildHeader(PdfFont font) {
        String text = """
                ESTADO DA PARAÍBA
                PREFEITURA MUNICIPAL DE TACIMA/PB
                SECRETARIA MUNICIPAL DE SAÚDE
                COORDENAÇÃO DE VIGILÂNCIA SANITÁRIA
                
                """;

        return new Paragraph(text)
                .setFont(font)
                .setFontSize(FONT_SIZE_DEFAULT)
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginTop(-35f);
    }

    public Paragraph buildTitle(PdfFont boldFont) {
        return new Paragraph("LICENCIAMENTO SANITÁRIO\n\n")
                .setFont(boldFont)
                .setFontSize(FONT_SIZE_TITLE)
                .setTextAlignment(TextAlignment.CENTER);
    }

    public Paragraph buildBody(EmpresaResponseDto empresa, LicensaSanitariaResponseDto licensa, PdfFont font) {

        StringBuilder text = new StringBuilder();
        text.append("Parecer técnico nº ").append(licensa.numControle()).append("\n\n");
        text.append("A coordenação de vigilância sanitária Municipal de Tacima/PB, baseado na Resolução RDC nº 275/2002 da ANVISA, ");
        text.append("autoriza o licenciamento sanitário no estabelecimento comercial ").append(empresa.atividadeFirma()).append(". ");
        text.append("Nome fantasia: ").append(empresa.nomeFantasia()).append(", CNPJ: ").append(empresa.cnpj()).append(", ");
        text.append("Inscrição estadual: ").append(empresa.inscricaoEstadual()).append(", ");
        text.append("Proprietário responsável: ").append(empresa.responsavel().nome()).append(", ");
        text.append("portador do RG: ").append(empresa.responsavel().rg()).append(" e CPF: ").append(empresa.responsavel().cpf()).append(".");

        return new Paragraph(text.toString())
                .setFont(font)
                .setFontSize(FONT_SIZE_DEFAULT)
                .setTextAlignment(TextAlignment.JUSTIFIED);
    }

    private String formatDate(LocalDateTime dateEmissao) {
        DateTimeFormatter formatter =
                DateTimeFormatter.ofPattern("dd 'de' MMMM 'de' yyyy", new Locale("pt", "BR"));
        return dateEmissao.format(formatter);
    }

    public IBlockElement buildFooter(LocalDateTime dateEmissao, PdfFont font, PdfFont boldFont) {
        String dataFormatada = formatDate(dateEmissao);

        Paragraph date = new Paragraph("Tacima-PB, " + dataFormatada)
                .setFont(font)
                .setFontSize(FONT_SIZE_SMALL)
                .setTextAlignment(TextAlignment.RIGHT);

        Paragraph obs = new Paragraph()
                .add(new Text("OBS: ").setFont(boldFont))
                .add(new Text("Válido por um ano a partir da data de expedição.").setFont(font))
                .setFontSize(FONT_SIZE_SMALL)
                .setFontColor(ColorConstants.BLACK)
                .setMarginBottom(120);

        Table tabelaAssinatura = new Table(1);
        tabelaAssinatura.setHorizontalAlignment(HorizontalAlignment.CENTER);
        tabelaAssinatura.setWidth(250);

        Paragraph textoAssinatura = new Paragraph()
                .add("José Henrique Barbosa da Costa\n")
                .add("Coordenador de vigilância sanitária\n")
                .add("Mat.12224-1")
                .setFont(font)
                .setFontSize(FONT_SIZE_SMALL)
                .setTextAlignment(TextAlignment.CENTER);

        Cell celulaAssinatura = new Cell().add(textoAssinatura);
        celulaAssinatura.setBorder(Border.NO_BORDER);
        celulaAssinatura.setBorderTop(new SolidBorder(1f));
        tabelaAssinatura.addCell(celulaAssinatura);

        Div footer = new Div();
        footer.add(date);
        footer.add(obs);
        footer.add(tabelaAssinatura);

        return footer;
    }

    public Image loadImage(String path, float width, float height, HorizontalAlignment horizontalAlignment) {
        URL resource = getClass().getResource(path);

        if (resource == null) {
            System.err.println("Imagem não encontrada: " + path);
            return null;
        }

        Image image = new Image(ImageDataFactory.create(resource));
        image.setHorizontalAlignment(horizontalAlignment);
        image.setWidth(width);
        image.setHeight(height);

        return image;
    }
}
