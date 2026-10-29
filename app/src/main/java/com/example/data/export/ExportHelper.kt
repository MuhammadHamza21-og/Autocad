package com.example.data.export

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.data.database.ProjectJsonSerializer
import com.example.data.model.CadFloor
import com.example.data.model.CadProject
import java.io.File
import java.io.FileOutputStream

object ExportHelper {

    fun exportDxfFile(context: Context, project: CadProject, floor: CadFloor): File {
        val dxfContent = DxfExporter.exportFloorPlan(project, floor)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "${cleanFileName(project.name)}_${cleanFileName(floor.name)}.dxf"
        val file = File(exportDir, fileName)
        file.writeText(dxfContent)
        return file
    }

    fun exportSvgFile(context: Context, project: CadProject, floor: CadFloor): File {
        val svgContent = SvgExporter.exportFloorPlan(project, floor)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "${cleanFileName(project.name)}_${cleanFileName(floor.name)}.svg"
        val file = File(exportDir, fileName)
        file.writeText(svgContent)
        return file
    }

    fun exportJsonProject(context: Context, project: CadProject): File {
        val jsonContent = ProjectJsonSerializer.toJson(project)
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val fileName = "${cleanFileName(project.name)}.asdcad"
        val file = File(exportDir, fileName)
        file.writeText(jsonContent)
        return file
    }

    fun shareFile(context: Context, file: File, mimeType: String, title: String = "Share CAD Drawing") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, title)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        val chooser = Intent.createChooser(intent, title).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(chooser)
    }

    private fun cleanFileName(name: String): String {
        return name.replace("[^a-zA-Z0-9.-]".toRegex(), "_")
    }
}
