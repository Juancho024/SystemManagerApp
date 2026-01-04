package com.jrdev.systemmanager.utilities;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;

import com.jrdev.systemmanager.DataBaseConnection.dao.RegistroFinancieroDao;
import com.jrdev.systemmanager.models.InformeTableView;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class GeneradorIMG {

    // Configuración de dimensiones
    private static final int ANCHO_IMAGEN = 1200; // Ancho fijo de alta calidad
    private static final int PADDING = 40;
    private static final int ALTO_FILA = 50;
    private static final int ALTO_HEADER = 60;

    // Colores
    private static final int COLOR_AZUL_OSCURO = Color.rgb(44, 62, 80);
    private static final int COLOR_ZEBRA = Color.rgb(245, 245, 245);
    private static final int COLOR_VERDE = Color.rgb(39, 174, 96);
    private static final int COLOR_ROJO = Color.rgb(192, 57, 43);
    private static final int COLOR_BLANCO = Color.WHITE;
    private static final int COLOR_NEGRO = Color.BLACK;

    // Pesos de las columnas
    private static final float[] COL_WEIGHTS = {0.8f, 1.6f, 1.6f, 2.0f, 1.8f, 3.0f, 2.0f, 2.0f};

    public void generarImagen(OutputStream outputStream, RegistroFinancieroDao registro, List<InformeTableView> listaDatos) {
        try {
            // 1. CALCULAR ALTO ESTIMADO (Aumentamos el buffer porque ahora las filas crecen)
            // Multiplicamos por 3 el alto de fila en el estimado para tener espacio de sobra
            int altoEstimado = 400 + 150 + ALTO_HEADER + (listaDatos.size() * ALTO_FILA * 3) + 600;

            // 2. CREAR BITMAP Y CANVAS
            Bitmap bitmap = Bitmap.createBitmap(ANCHO_IMAGEN, altoEstimado, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.drawColor(COLOR_BLANCO);

            Paint paintTexto = new Paint();
            paintTexto.setAntiAlias(true);
            Paint paintFondo = new Paint();

            // Paint especial para texto multilínea
            TextPaint textPaintDesc = new TextPaint();
            textPaintDesc.setAntiAlias(true);
            textPaintDesc.setTextSize(19);
            textPaintDesc.setColor(COLOR_NEGRO);

            int currentY = PADDING + 40;

            // 3. DIBUJAR TÍTULO
            paintTexto.setColor(COLOR_AZUL_OSCURO);
            paintTexto.setTextSize(36);
            paintTexto.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paintTexto.setTextAlign(Paint.Align.CENTER);

            String titulo = "INFORME FINANCIERO - " + (registro.mesCuota != null ? registro.mesCuota.toUpperCase() : "");
            canvas.drawText(titulo, ANCHO_IMAGEN / 2f, currentY, paintTexto);

            currentY += 60;

            // 4. DIBUJAR RESUMEN TOTALES
            float totalPendiente = 0;
            float totalRecibido = 0;
            for (InformeTableView i : listaDatos) {
                if (i.getBalance() < 0) totalPendiente += i.getMontoPagar();
                if (i.getBalance() >= 0) totalRecibido += i.getMontoPagar();
            }
            if(totalPendiente > 0) totalPendiente *= (-1);

            dibujarTablaTotales(canvas, currentY, totalRecibido, registro.cuotaMensual, registro.montoPagar, totalPendiente);
            currentY += 140;

            // 5. DIBUJAR CABECERA
            String[] headers = {"Apto", "Propietario", "Estado", "Recibido a la Fecha", "Cuota", "Descripción", "Monto A Pagar", "Balance"};
            float[] anchosColumnas = calcularAnchosColumnas();
            float currentX = PADDING;

            paintFondo.setColor(COLOR_AZUL_OSCURO);
            canvas.drawRect(PADDING, currentY, ANCHO_IMAGEN - PADDING, currentY + ALTO_HEADER, paintFondo);

            paintTexto.setColor(COLOR_BLANCO);
            paintTexto.setTextSize(20);
            paintTexto.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
            paintTexto.setTextAlign(Paint.Align.CENTER);

            for (int i = 0; i < headers.length; i++) {
                float centroCelda = currentX + (anchosColumnas[i] / 2);
                canvas.drawText(headers[i], centroCelda, currentY + (ALTO_HEADER / 2) + 8, paintTexto);
                currentX += anchosColumnas[i];
            }
            currentY += ALTO_HEADER;

            // 6. DIBUJAR FILAS DE DATOS (Lógica Modificada para Multilínea)
            paintTexto.setTextSize(19);
            boolean par = false;

            for (InformeTableView d : listaDatos) {

                // --- CALCULO DE ALTURA DINÁMICA ---
                // Preparamos el texto de descripción completo
                String descCompleta = d.getDescripcion() != null ? d.getDescripcion() : "";

                // Calculamos ancho disponible para descripción (índice 5 en headers)
                int anchoDesc = (int) anchosColumnas[5] - 20; // -20 padding lateral

                // Creamos un layout para medir cuánto ocupará el texto
                StaticLayout layoutDesc = new StaticLayout(descCompleta, textPaintDesc, anchoDesc,
                        Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

                // La altura de la fila será el MAXIMO entre el alto estándar (50) y el alto del texto + padding
                int altoTexto = layoutDesc.getHeight();
                int altoFilaActual = Math.max(ALTO_FILA, altoTexto + 20); // +20 para que no pegue en los bordes

                // --- DIBUJAR FONDO ---
                if (par) {
                    paintFondo.setColor(COLOR_ZEBRA);
                    canvas.drawRect(PADDING, currentY, ANCHO_IMAGEN - PADDING, currentY + altoFilaActual, paintFondo);
                }
                par = !par;

                currentX = PADDING;

                // Calculamos el centro vertical para los textos normales de una sola línea
                float yTextoCentro = currentY + (altoFilaActual / 2) + 6;

                // Col 1: Apto
                dibujarTextoCelda(canvas, d.getApto(), currentX, anchosColumnas[0], yTextoCentro, Paint.Align.CENTER, COLOR_NEGRO, false);
                currentX += anchosColumnas[0];

                // Col 2: Propietario
                dibujarTextoCelda(canvas, d.getPropietario(), currentX, anchosColumnas[1], yTextoCentro, Paint.Align.LEFT, COLOR_NEGRO, false);
                currentX += anchosColumnas[1];

                // Col 3: Estado
                boolean alDia = d.getEstado().equalsIgnoreCase("Verde");
                dibujarTextoCelda(canvas, alDia ? "AL DÍA" : "PENDIENTE", currentX, anchosColumnas[2], yTextoCentro, Paint.Align.CENTER, alDia ? COLOR_VERDE : COLOR_ROJO, true);
                currentX += anchosColumnas[2];

                // Col 4: Recibido
                dibujarTextoCelda(canvas, fmtMoney(d.getTotalRecibido()), currentX, anchosColumnas[3], yTextoCentro, Paint.Align.RIGHT, COLOR_NEGRO, false);
                currentX += anchosColumnas[3];

                // Col 5: Cuota
                dibujarTextoCelda(canvas, fmtMoney(d.getCuotaMensual()), currentX, anchosColumnas[4], yTextoCentro, Paint.Align.RIGHT, COLOR_NEGRO, false);
                currentX += anchosColumnas[4];

                // --- Col 6: DESCRIPCIÓN MULTILÍNEA ---
                // Guardamos estado del canvas
                canvas.save();
                // Nos movemos a la posición X correcta y bajamos un poco (padding top 10)
                canvas.translate(currentX + 10, currentY + 10);
                // Dibujamos el layout calculado antes
                layoutDesc.draw(canvas);
                // Restauramos canvas
                canvas.restore();

                currentX += anchosColumnas[5]; // Avanzamos X

                // Col 7: A Pagar
                dibujarTextoCelda(canvas, fmtMoney(d.getMontoPagar()), currentX, anchosColumnas[6], yTextoCentro, Paint.Align.RIGHT, COLOR_NEGRO, false);
                currentX += anchosColumnas[6];

                // Col 8: Balance
                int colorBalance = d.getBalance() < 0 ? COLOR_ROJO : COLOR_NEGRO;
                dibujarTextoCelda(canvas, fmtMoney(d.getBalance()), currentX, anchosColumnas[7], yTextoCentro, Paint.Align.RIGHT, colorBalance, false);

                // AVANZAMOS Y CON LA ALTURA DINÁMICA
                currentY += altoFilaActual;
            }

            // 7. NOTAS
            currentY += 40;
            TextPaint tp = new TextPaint();
            tp.setAntiAlias(true);
            tp.setTextSize(22);
            tp.setColor(COLOR_NEGRO);
            tp.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));

            canvas.drawText("NOTA:", PADDING, currentY, tp);
            currentY += 10;
            // ... (Resto del código de notas igual) ...

            tp.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL));
            String notas = "Los balances negativos indican montos pendientes por pagar.\n" +
                    "- Recordar que dentro de monto a pagar incluye el pago de la luz, agua y mantenimiento.\n" +
                    "- La mensualidad del agua son DOP$ 1082.00 / 8 = DOP$ 135.25 por apartamento.\n" +
                    "- La mensualidad del mantenimiento son DOP$ 4918.00 / 8 = DOP$ 614.75 por apartamento.\n" +
                    "- La mensualidad de la luz son DOP$ 638.00 / 8 = DOP$ 80.00 por apartamento.\n" +
                    "- Cualquier duda contactar a administración.";

            StaticLayout layoutNotas = new StaticLayout(notas, tp, ANCHO_IMAGEN - (PADDING * 2), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, false);

            canvas.save();
            canvas.translate(PADDING, currentY);
            layoutNotas.draw(canvas);
            canvas.restore();

            currentY += layoutNotas.getHeight() + 40;

            // 8. PIE DE PÁGINA
            String fecha = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(new Date());
            paintTexto.setTextSize(18);
            paintTexto.setColor(Color.GRAY);
            paintTexto.setTextAlign(Paint.Align.LEFT);
            canvas.drawText("Generado el: " + fecha + " | Residencial Santos I", PADDING, currentY, paintTexto);

            // 9. GUARDAR
            Bitmap bitmapFinal = Bitmap.createBitmap(bitmap, 0, 0, ANCHO_IMAGEN, currentY + 50);
            bitmapFinal.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Sobrecarga para mantener compatibilidad si pasas un String (Ruta de archivo)
     */
    public void generarImagen(String rutaDestino, RegistroFinancieroDao registro, List<InformeTableView> listaDatos) {
        try (FileOutputStream fos = new FileOutputStream(rutaDestino)) {
            generarImagen(fos, registro, listaDatos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- Helpers ---

    private void dibujarTablaTotales(Canvas canvas, int y, float recibido, Float cuota, Float aPagar, float pendiente) {
        int anchoTotal = ANCHO_IMAGEN - (PADDING * 2);
        int anchoCelda = anchoTotal / 4;
        int x = PADDING;
        int altoCaja = 100;

        String[] titulos = {"TOTAL RECIBIDO", "CUOTA", "MONTO A PAGAR", "PENDIENTE"};
        float[] valores = {recibido, (cuota != null ? cuota : 0), (aPagar != null ? aPagar : 0), pendiente};

        Paint bgPaint = new Paint();
        bgPaint.setColor(COLOR_AZUL_OSCURO);

        Paint txtPaint = new Paint();
        txtPaint.setAntiAlias(true);
        txtPaint.setTextAlign(Paint.Align.CENTER);

        for (int i = 0; i < 4; i++) {
            Rect r = new Rect(x + 5, y, x + anchoCelda - 5, y + altoCaja);
            canvas.drawRect(r, bgPaint);

            txtPaint.setColor(Color.WHITE);
            txtPaint.setTextSize(18);
            txtPaint.setTypeface(Typeface.DEFAULT);
            canvas.drawText(titulos[i], r.centerX(), y + 35, txtPaint);

            txtPaint.setTextSize(26);
            txtPaint.setTypeface(Typeface.DEFAULT_BOLD);
            if (i == 3 && valores[i] < 0) txtPaint.setColor(Color.rgb(255, 100, 100));
            canvas.drawText(fmtMoney(valores[i]), r.centerX(), y + 75, txtPaint);

            x += anchoCelda;
        }
    }

    private void dibujarTextoCelda(Canvas c, String txt, float x, float ancho, float y, Paint.Align align, int color, boolean bold) {
        Paint p = new Paint();
        p.setAntiAlias(true);
        p.setColor(color);
        p.setTextSize(20);
        p.setTypeface(bold ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
        p.setTextAlign(align);

        float drawX = x;
        if (align == Paint.Align.CENTER) drawX = x + (ancho / 2);
        else if (align == Paint.Align.RIGHT) drawX = x + ancho - 10;
        else drawX = x + 10;

        c.drawText(txt != null ? txt : "", drawX, y, p);
    }

    private float[] calcularAnchosColumnas() {
        float totalWeight = 0;
        for (float w : COL_WEIGHTS) totalWeight += w;

        float anchoDisponible = ANCHO_IMAGEN - (PADDING * 2);
        float[] anchos = new float[COL_WEIGHTS.length];

        for (int i = 0; i < COL_WEIGHTS.length; i++) {
            anchos[i] = (COL_WEIGHTS[i] / totalWeight) * anchoDisponible;
        }
        return anchos;
    }

    private String fmtMoney(float val) {
        return String.format(Locale.getDefault(), "DOP$ %.2f", val);
    }
}