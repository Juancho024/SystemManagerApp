package com.jrdev.systemmanager.utilities;

import android.util.Base64;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao;
import com.jrdev.systemmanager.models.InformeTableView;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class GeneradorPDF {

    // Colores
    private static final BaseColor COLOR_AZUL_OSCURO = new BaseColor(44, 62, 80);
    private static final BaseColor COLOR_ZEBRA = new BaseColor(245, 245, 245);
    private static final BaseColor COLOR_VERDE = new BaseColor(39, 174, 96);
    private static final BaseColor COLOR_ROJO = new BaseColor(192, 57, 43);

    // Fuentes
    private static final Font FONT_TITULO =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, COLOR_AZUL_OSCURO);
    private static final Font FONT_HEADER_SMALL =
            FontFactory.getFont(FontFactory.HELVETICA, 9, BaseColor.WHITE);
    private static final Font FONT_TABLA_HEADER =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.5f, BaseColor.WHITE);
    private static final Font FONT_TABLA_NORMAL =
            FontFactory.getFont(FontFactory.HELVETICA, 8.2f, BaseColor.BLACK);
    private static final Font FONT_ALDIA =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.2f, COLOR_VERDE);
    private static final Font FONT_PENDIENTE =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 8.2f, COLOR_ROJO);
    private static final Font FONT_NOTA =
            FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, BaseColor.BLACK);

        public void generarReporte(OutputStream outputStream,
                                                           RegistroFinancieroDao registro,
                                                           List<InformeTableView> listaDatos) throws Exception {

                Document document = new Document(PageSize.LETTER, 25, 25, 30, 35);
                PdfWriter writer = PdfWriter.getInstance(document, outputStream);
                writer.setPageEvent(new PieDePaginaEvento());
                document.open();

        // Título
        Paragraph titulo = new Paragraph(
                "INFORME FINANCIERO - " + registro.mesCuota.toUpperCase(),
                FONT_TITULO
        );
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(8);
        document.add(titulo);

        // Resumen totales
        PdfPTable tablaTotales = new PdfPTable(4);
        tablaTotales.setWidthPercentage(100);
        tablaTotales.setSpacingAfter(10);

        float totalPendiente = 0;
        float totalRecibido = 0;

        for (InformeTableView i : listaDatos) {
            if (i.getBalance() < 0) totalPendiente += i.getMontoPagar();
            if (i.getBalance() >= 0) totalRecibido += i.getMontoPagar();
        }
        if(totalPendiente > 0) totalPendiente *= (-1);

        agregarCeldaTotal(tablaTotales, "TOTAL RECIBIDO", totalRecibido, false);
        agregarCeldaTotal(tablaTotales, "CUOTA", registro.cuotaMensual != null ? registro.cuotaMensual : 0f, false);
        agregarCeldaTotal(tablaTotales, "MONTO A PAGAR", registro.montoPagar != null ? registro.montoPagar : 0f, false);
        agregarCeldaTotal(tablaTotales, "PENDIENTE", totalPendiente, true);

        document.add(tablaTotales);

        // Tabla principal
        PdfPTable tabla = new PdfPTable(
                new float[]{0.8f, 1.6f, 1.6f, 2.0f, 1.8f, 3.0f, 2.0f, 2.0f}
        );
        tabla.setWidthPercentage(100);
        tabla.setHeaderRows(1);

        String[] headers = {
                "Apto", "Propietario", "Estado", "Recibido a la Fecha",
                "Cuota", "Descripción", "Monto A Pagar", "Balance"
        };

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, FONT_TABLA_HEADER));
            cell.setBackgroundColor(COLOR_AZUL_OSCURO);
            cell.setPadding(4);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            tabla.addCell(cell);
        }

        boolean par = false;
        for (InformeTableView d : listaDatos) {
            BaseColor fondo = par ? COLOR_ZEBRA : BaseColor.WHITE;

            agregarCelda(tabla, d.getApto(), fondo, Element.ALIGN_CENTER);
            agregarCelda(tabla, d.getPropietario(), fondo, Element.ALIGN_LEFT);

            PdfPCell estado = new PdfPCell();
            estado.setBackgroundColor(fondo);
            estado.setPadding(4);
            estado.setHorizontalAlignment(Element.ALIGN_CENTER);
            estado.setPhrase(
                    d.getEstado().equalsIgnoreCase("Verde")
                            ? new Phrase("AL DÍA", FONT_ALDIA)
                            : new Phrase("PENDIENTE", FONT_PENDIENTE)
            );
            tabla.addCell(estado);

            agregarMoneda(tabla, d.getTotalRecibido(), fondo, BaseColor.BLACK);
            agregarMoneda(tabla, d.getCuotaMensual(), fondo, BaseColor.BLACK);
            agregarCelda(tabla, d.getDescripcion(), fondo, Element.ALIGN_LEFT);
            agregarMoneda(tabla, d.getMontoPagar(), fondo, BaseColor.BLACK);

            BaseColor colorBalance = d.getBalance() < 0 ? COLOR_ROJO : BaseColor.BLACK;
            agregarMoneda(tabla, d.getBalance(), fondo, colorBalance);

            par = !par;
        }

        document.add(tabla);

        Paragraph titulonota = new Paragraph("NOTA: ", FONT_NOTA);
        titulonota.setAlignment(Element.ALIGN_LEFT);
        titulonota.setSpacingBefore(16);
        titulonota.setSpacingAfter(0);
        document.add(titulonota);

        Paragraph nota = new Paragraph(
                "Los balances negativos indican montos pendientes por pagar.\n" +
                        "- Recordar que dentro de monto a pagar incluye el pago de la luz, agua y mantenimiento.\n" +
                        "- La mensualidad del agua son DOP$ 1082.00 / 8 = DOP$ 135.25 por apartamento.\n" +
                        "- La mensualidad del mantenimiento son DOP$ 4918.00 / 8 = DOP$ 614.75 por apartamento.\n" +
                        "- La mensualidad de la luz son DOP$ 638.00 / 8 = DOP$ 80.00 por apartamento.\n" +
                        "- Cualquier duda contactar a administración.",
                FONT_TABLA_NORMAL
        );
        nota.setAlignment(Element.ALIGN_LEFT);
        titulonota.setSpacingAfter(0);
        document.add(nota);

        // Imágenes anexas (img1..4 en Base64)
        List<byte[]> imagenes = obtenerImagenes(registro);
        int contador = 1;
        for (byte[] imgBytes : imagenes) {
            if (imgBytes == null) continue;
            document.newPage();
            Paragraph tituloImg = new Paragraph("ANEXO DE PAGO #" + contador,
                    FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, COLOR_AZUL_OSCURO));
            tituloImg.setAlignment(Element.ALIGN_CENTER);
            tituloImg.setSpacingAfter(10);
            document.add(tituloImg);
            try {
                Image img = Image.getInstance(imgBytes);
                float anchoPagina = document.getPageSize().getWidth() - 80;
                float altoPagina = document.getPageSize().getHeight() - 100;
                img.scaleToFit(anchoPagina, altoPagina);
                img.setAlignment(Element.ALIGN_CENTER);
                img.setBorder(Image.BOX);
                img.setBorderWidth(1);
                img.setBorderColor(BaseColor.GRAY);
                document.add(img);
            } catch (Exception ignored) {}
            contador++;
        }

        document.close();
    }

    private void agregarCeldaTotal(PdfPTable table, String titulo, float valor, boolean rojo) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(COLOR_AZUL_OSCURO);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setBorderColor(BaseColor.WHITE);

        Paragraph t = new Paragraph(titulo, FONT_HEADER_SMALL);
        t.setAlignment(Element.ALIGN_CENTER);

        Font f = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD,
                rojo && valor < 0 ? new BaseColor(255, 100, 100) : BaseColor.WHITE);

        Paragraph v = new Paragraph(String.format("DOP$ %.2f", valor), f);
        v.setAlignment(Element.ALIGN_CENTER);

        cell.addElement(t);
        cell.addElement(v);
        table.addCell(cell);
    }

    private void agregarCelda(PdfPTable t, String txt, BaseColor fondo, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt != null ? txt : "", FONT_TABLA_NORMAL));
        c.setBackgroundColor(fondo);
        c.setPadding(4);
        c.setHorizontalAlignment(align);
        t.addCell(c);
    }

    private void agregarMoneda(PdfPTable t, float v, BaseColor fondo, BaseColor color) {
        Font f = new Font(Font.FontFamily.HELVETICA, 8.8f, Font.NORMAL, color);
        PdfPCell c = new PdfPCell(new Phrase(String.format("DOP$ %.2f", v), f));
        c.setBackgroundColor(fondo);
        c.setPadding(4);
        c.setNoWrap(true);
        c.setHorizontalAlignment(Element.ALIGN_RIGHT);
        t.addCell(c);
    }

    private List<byte[]> obtenerImagenes(RegistroFinancieroDao registro) {
        List<byte[]> imgs = new ArrayList<>();
        if (registro == null) return imgs;
        decode(imgs, registro.img1);
        decode(imgs, registro.img2);
        decode(imgs, registro.img3);
        decode(imgs, registro.img4);
        return imgs;
    }

    private void decode(List<byte[]> list, String base64) {
        if (base64 == null || base64.trim().isEmpty()) return;
        try {
            list.add(Base64.decode(base64, Base64.DEFAULT));
        } catch (Exception ignored) {}
    }

    // Pie de pagina
    static class PieDePaginaEvento extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date());
            String texto = "Generado el: " + fecha + " | Residencial Santos I";
            String pagina = "Página " + writer.getPageNumber();
            Font f = FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY);

            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase(texto, f),
                    document.leftMargin(),
                    20, 0);

            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(pagina, f),
                    document.getPageSize().getWidth() - document.rightMargin(),
                    20, 0);
        }
    }
}
