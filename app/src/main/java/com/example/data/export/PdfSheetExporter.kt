package com.example.data.export

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import com.example.data.model.*
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.*

enum class PaperSize(val label: String, val widthPt: Int, val heightPt: Int) {
    A4("A4 (210 × 297 mm)", 595, 842),
    A3("A3 (297 × 420 mm)", 842, 1191),
    A1("A1 (594 × 841 mm)", 1684, 2384)
}

enum class Orientation(val label: String) {
    LANDSCAPE("Landscape"),
    PORTRAIT("Portrait")
}

object PdfSheetExporter {

    fun generatePdfSheet(
        context: Context,
        project: CadProject,
        floor: CadFloor,
        paperSize: PaperSize = PaperSize.A4,
        orientation: Orientation = Orientation.LANDSCAPE
    ): File {
        val pdfDocument = PdfDocument()

        val pageWidth = if (orientation == Orientation.LANDSCAPE) paperSize.heightPt else paperSize.widthPt
        val pageHeight = if (orientation == Orientation.LANDSCAPE) paperSize.widthPt else paperSize.heightPt

        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        val page = pdfDocument.startPage(pageInfo)
        val canvas = page.canvas

        // Drawing Paints
        val borderPaint = Paint().apply {
            color = Color.rgb(15, 23, 42) // Dark navy slate
            strokeWidth = 2.0f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val thinBorderPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            strokeWidth = 0.8f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val wallPaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val intWallPaint = Paint().apply {
            color = Color.rgb(71, 85, 105)
            strokeWidth = 1.5f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        val roomFillPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val textTitlePaint = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 10f
            isFakeBoldText = true
            isAntiAlias = true
        }

        val textBodyPaint = Paint().apply {
            color = Color.rgb(51, 65, 85)
            textSize = 7.5f
            isAntiAlias = true
        }

        val textSmallPaint = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 6.5f
            isAntiAlias = true
        }

        val plotLinePaint = Paint().apply {
            color = Color.rgb(220, 38, 38)
            strokeWidth = 1.0f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        // 1. Draw Architectural Border
        val margin = 20f
        canvas.drawRect(margin, margin, pageWidth - margin, pageHeight - margin, borderPaint)
        canvas.drawRect(margin + 4f, margin + 4f, pageWidth - margin - 4f, pageHeight - margin - 4f, thinBorderPaint)

        // 2. Draw Title Block (Bottom Bar)
        val titleBlockH = 75f
        val tbTop = pageHeight - margin - 4f - titleBlockH
        val tbLeft = margin + 4f
        val tbRight = pageWidth - margin - 4f

        canvas.drawRect(tbLeft, tbTop, tbRight, pageHeight - margin - 4f, thinBorderPaint)
        // Divide title block into compartments
        val col1W = 160f
        val col2W = 160f
        val col3W = tbRight - tbLeft - col1W - col2W

        canvas.drawLine(tbLeft + col1W, tbTop, tbLeft + col1W, pageHeight - margin - 4f, thinBorderPaint)
        canvas.drawLine(tbLeft + col1W + col2W, tbTop, tbLeft + col1W + col2W, pageHeight - margin - 4f, thinBorderPaint)

        // Compartment 1: Project Info
        canvas.drawText("AI SMART DRAFTSMAN CAD SUITE", tbLeft + 8f, tbTop + 14f, textSmallPaint)
        canvas.drawText(project.name.take(28), tbLeft + 8f, tbTop + 30f, textTitlePaint)
        canvas.drawText("Client: ${project.clientName}", tbLeft + 8f, tbTop + 45f, textBodyPaint)
        canvas.drawText("Location: ${project.siteLocation}", tbLeft + 8f, tbTop + 60f, textSmallPaint)

        // Compartment 2: Drawing Info
        canvas.drawText("DRAWING DETAILS", tbLeft + col1W + 8f, tbTop + 14f, textSmallPaint)
        canvas.drawText("${floor.name.uppercase(Locale.US)} - ${project.drawingTitle}", tbLeft + col1W + 8f, tbTop + 30f, textBodyPaint)
        val areaReport = UnitConverter.calculateAreaReport(project.plot.totalAreaSqFt, project.marlaConvention)
        canvas.drawText("Plot Area: ${areaReport.formattedSummary}", tbLeft + col1W + 8f, tbTop + 45f, textBodyPaint)
        canvas.drawText("Covered: ${floor.coveredAreaSqFt.toInt()} sq ft (${project.groundCoveragePercentage.toInt()}% Coverage)", tbLeft + col1W + 8f, tbTop + 60f, textSmallPaint)

        // Compartment 3: Drawing No, Date, Scale, Rev, North
        val col3Left = tbLeft + col1W + col2W
        val dateStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(project.dateModified))
        canvas.drawText("DWG NO: ${project.drawingNumber}", col3Left + 8f, tbTop + 16f, textTitlePaint)
        canvas.drawText("REV: ${project.revision}   DATE: $dateStr", col3Left + 8f, tbTop + 32f, textBodyPaint)
        canvas.drawText("SCALE: 1/8\" = 1'-0\"   UNITS: ${project.unitSystem.label}", col3Left + 8f, tbTop + 48f, textSmallPaint)
        canvas.drawText("Standard: ${project.marlaConvention.label}", col3Left + 8f, tbTop + 62f, textSmallPaint)

        // North Compass Arrow in Compartment 3
        val compassX = tbRight - 28f
        val compassY = tbTop + 36f
        canvas.drawLine(compassX, compassY - 18f, compassX, compassY + 18f, borderPaint)
        val arrowPath = Path().apply {
            moveTo(compassX, compassY - 22f)
            lineTo(compassX - 6f, compassY - 10f)
            lineTo(compassX + 6f, compassY - 10f)
            close()
        }
        val fillPaint = Paint().apply { color = Color.rgb(15, 23, 42); style = Paint.Style.FILL }
        canvas.drawPath(arrowPath, fillPaint)
        canvas.drawText("N", compassX - 3.5f, compassY - 25f, textTitlePaint)

        // 3. Render CAD Floor Plan on printable region
        val planRegionLeft = margin + 12f
        val planRegionTop = margin + 12f
        val planRegionRight = pageWidth - margin - 12f
        val planRegionBottom = tbTop - 10f

        val planW = planRegionRight - planRegionLeft
        val planH = planRegionBottom - planRegionTop

        val plotBounds = project.plot.polygon.bounds()
        val scaleX = planW / (plotBounds.actualWidth + 8f)
        val scaleY = planH / (plotBounds.actualHeight + 8f)
        val scale = min(scaleX, scaleY).coerceAtMost(10f)

        val offsetX = planRegionLeft + (planW - plotBounds.actualWidth * scale) / 2f - plotBounds.left * scale
        val offsetY = planRegionTop + (planH - plotBounds.actualHeight * scale) / 2f - plotBounds.top * scale

        // Draw Plot Boundary
        val plotVerts = project.plot.computePlotVertices()
        if (plotVerts.size >= 3) {
            val pPath = Path()
            pPath.moveTo(offsetX + plotVerts[0].x * scale, offsetY + plotVerts[0].y * scale)
            for (i in 1 until plotVerts.size) {
                pPath.lineTo(offsetX + plotVerts[i].x * scale, offsetY + plotVerts[i].y * scale)
            }
            pPath.close()
            canvas.drawPath(pPath, plotLinePaint)
        }

        // Draw Rooms
        for (r in floor.rooms) {
            val rx = offsetX + r.rect.left * scale
            val ry = offsetY + r.rect.top * scale
            val rw = r.rect.actualWidth * scale
            val rh = r.rect.actualHeight * scale

            canvas.drawRect(rx, ry, rx + rw, ry + rh, roomFillPaint)
            canvas.drawRect(rx, ry, rx + rw, ry + rh, thinBorderPaint)

            // Text
            val textPaint = Paint().apply {
                color = Color.rgb(30, 41, 59)
                textSize = (7f * (scale / 4f).coerceIn(0.8f, 1.4f)).coerceIn(5f, 12f)
                isFakeBoldText = true
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(r.name, rx + rw / 2f, ry + rh / 2f - 4f, textPaint)

            val dimPaint = Paint().apply {
                color = Color.rgb(100, 116, 139)
                textSize = textPaint.textSize * 0.8f
                textAlign = Paint.Align.CENTER
                isAntiAlias = true
            }
            canvas.drawText(
                "${r.rect.actualWidth.toInt()}'-0\" × ${r.rect.actualHeight.toInt()}'-0\"",
                rx + rw / 2f, ry + rh / 2f + 6f, dimPaint
            )
        }

        // Draw Walls
        for (w in floor.walls) {
            val p = if (w.isExterior) wallPaint else intWallPaint
            canvas.drawLine(
                offsetX + w.start.x * scale,
                offsetY + w.start.y * scale,
                offsetX + w.end.x * scale,
                offsetY + w.end.y * scale,
                p
            )
        }

        // Draw Staircase
        for (s in floor.staircases) {
            val sx = offsetX + s.rect.left * scale
            val sy = offsetY + s.rect.top * scale
            val sw = s.rect.actualWidth * scale
            val sh = s.rect.actualHeight * scale
            canvas.drawRect(sx, sy, sx + sw, sy + sh, thinBorderPaint)

            val stepH = sh / s.stepsCount
            for (step in 1 until s.stepsCount) {
                val y = sy + step * stepH
                canvas.drawLine(sx, y, sx + sw, y, thinBorderPaint)
            }
        }

        pdfDocument.finishPage(page)

        // Save to cache directory
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "${project.name.replace("\\s+".toRegex(), "_")}_${floor.name.replace("\\s+".toRegex(), "_")}.pdf"
        val file = File(exportDir, fileName)
        FileOutputStream(file).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        return file
    }
}
