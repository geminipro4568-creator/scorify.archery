package com.example.data

import android.content.Context
import android.os.Environment
import java.io.File
import org.json.JSONArray
import org.json.JSONObject

object DataExporter {

    /**
     * Exports session data as a SpreadsheetML (XML) string compatible with Excel.
     * If [differentSheets] is true, creates a separate tab for each archer.
     * Otherwise, creates a single unified sheet.
     */
    fun exportToExcelXml(
        session: ArcherySession,
        archers: List<Archer>,
        shots: List<ArrowShot>,
        differentSheets: Boolean
    ): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\"?>\n")
        sb.append("<?mso-application progid=\"Excel.Sheet\"?>\n")
        sb.append("<Workbook xmlns=\"urn:schemas-microsoft-microsoft:office:spreadsheet\"\n")
        sb.append(" xmlns:o=\"urn:schemas-microsoft-com:office:office\"\n")
        sb.append(" xmlns:x=\"urn:schemas-microsoft-com:office:excel\"\n")
        sb.append(" xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\"\n")
        sb.append(" xmlns:html=\"http://www.w3.org/TR/REC-html40\">\n")

        // Styling
        sb.append(" <Styles>\n")
        sb.append("  <Style ss:ID=\"Default\" ss:Name=\"Normal\">\n")
        sb.append("   <Alignment ss:Vertical=\"Bottom\"/>\n")
        sb.append("   <Borders/>\n")
        sb.append("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\" ss:Color=\"#000000\"/>\n")
        sb.append("   <Interior/>\n")
        sb.append("   <NumberFormat/>\n")
        sb.append("   <Protection/>\n")
        sb.append("  </Style>\n")
        sb.append("  <Style ss:ID=\"Header\">\n")
        sb.append("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"12\" ss:Bold=\"1\" ss:Color=\"#FFFFFF\"/>\n")
        sb.append("   <Interior ss:Color=\"#121212\" ss:Pattern=\"Solid\"/>\n")
        sb.append("   <Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>\n")
        sb.append("  </Style>\n")
        sb.append("  <Style ss:ID=\"Meta\">\n")
        sb.append("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"11\" ss:Italic=\"1\" ss:Color=\"#555555\"/>\n")
        sb.append("  </Style>\n")
        sb.append("  <Style ss:ID=\"Title\">\n")
        sb.append("   <Font ss:FontName=\"Calibri\" x:Family=\"Swiss\" ss:Size=\"16\" ss:Bold=\"1\" ss:Color=\"#FFD700\"/>\n")
        sb.append("   <Interior ss:Color=\"#121212\" ss:Pattern=\"Solid\"/>\n")
        sb.append("  </Style>\n")
        sb.append(" </Styles>\n")

        val archerMap = archers.associateBy { it.id }

        if (differentSheets) {
            // Sheet per Archer
            for (archer in archers) {
                val archerShots = shots.filter { it.archerId == archer.id }
                appendArcherSheet(sb, session, archer, archerShots)
            }
        } else {
            // Single Unified Sheet
            sb.append(" <Worksheet ss:Name=\"Scorify Unified\">\n")
            sb.append("  <Table>\n")
            sb.append("   <Column ss:Width=\"120\"/>\n")
            sb.append("   <Column ss:Width=\"80\"/>\n")
            sb.append("   <Column ss:Width=\"80\"/>\n")
            sb.append("   <Column ss:Width=\"80\"/>\n")
            sb.append("   <Column ss:Width=\"100\"/>\n")
            sb.append("   <Column ss:Width=\"100\"/>\n")
            sb.append("   <Column ss:Width=\"120\"/>\n")

            // Title Row
            sb.append("   <Row ss:Height=\"30\">\n")
            sb.append("    <Cell ss:StyleID=\"Title\"><Data ss:Type=\"String\">Scorify Archery Session: ${session.name}</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
            sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
            sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
            sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
            sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
            sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
            sb.append("   </Row>\n")

            // Meta Rows
            sb.append("   <Row>\n")
            sb.append("    <Cell ss:StyleID=\"Meta\"><Data ss:Type=\"String\">Preset: ${session.presetType}</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"Meta\"><Data ss:Type=\"String\">Distance: ${session.distanceName} ${session.distanceUnit}</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"Meta\"><Data ss:Type=\"String\">Target Size: ${session.targetSizeCm} cm</Data></Cell>\n")
            sb.append("   </Row>\n")
            sb.append("   <Row><Cell/></Row>\n") // empty row

            // Headers
            sb.append("   <Row ss:Height=\"20\">\n")
            sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Archer Name</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">End No.</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Arrow No.</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Score Value</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Numeric Value</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Plot X</Data></Cell>\n")
            sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Plot Y</Data></Cell>\n")
            sb.append("   </Row>\n")

            // Data Rows
            for (shot in shots) {
                val archerName = archerMap[shot.archerId]?.name ?: "Unknown"
                sb.append("   <Row>\n")
                sb.append("    <Cell><Data ss:Type=\"String\">$archerName</Data></Cell>\n")
                sb.append("    <Cell><Data ss:Type=\"Number\">${shot.endNumber}</Data></Cell>\n")
                sb.append("    <Cell><Data ss:Type=\"Number\">${shot.shotNumber}</Data></Cell>\n")
                sb.append("    <Cell><Data ss:Type=\"String\">${shot.valueString}</Data></Cell>\n")
                sb.append("    <Cell><Data ss:Type=\"Number\">${shot.valueNum}</Data></Cell>\n")
                sb.append("    <Cell><Data ss:Type=\"String\">${shot.posX ?: ""}</Data></Cell>\n")
                sb.append("    <Cell><Data ss:Type=\"String\">${shot.posY ?: ""}</Data></Cell>\n")
                sb.append("   </Row>\n")
            }

            sb.append("  </Table>\n")
            sb.append(" </Worksheet>\n")
        }

        sb.append("</Workbook>\n")
        return sb.toString()
    }

    private fun appendArcherSheet(
        sb: StringBuilder,
        session: ArcherySession,
        archer: Archer,
        shots: List<ArrowShot>
    ) {
        val sanitizedName = archer.name.replace("[\\*\\?:/\\\\'\\[\\]]".toRegex(), "")
        val sheetName = if (sanitizedName.length > 25) sanitizedName.substring(0, 25) else sanitizedName

        sb.append(" <Worksheet ss:Name=\"$sheetName\">\n")
        sb.append("  <Table>\n")
        sb.append("   <Column ss:Width=\"80\"/>\n")
        sb.append("   <Column ss:Width=\"80\"/>\n")
        sb.append("   <Column ss:Width=\"80\"/>\n")
        sb.append("   <Column ss:Width=\"100\"/>\n")
        sb.append("   <Column ss:Width=\"100\"/>\n")
        sb.append("   <Column ss:Width=\"120\"/>\n")

        // Title
        sb.append("   <Row ss:Height=\"30\">\n")
        sb.append("    <Cell ss:StyleID=\"Title\"><Data ss:Type=\"String\">Scorify Record - Archer: ${archer.name}</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
        sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
        sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
        sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
        sb.append("    <Cell ss:StyleID=\"Title\"/>\n")
        sb.append("   </Row>\n")

        // Session Meta
        sb.append("   <Row>\n")
        sb.append("    <Cell ss:StyleID=\"Meta\"><Data ss:Type=\"String\">Round: ${session.name}</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Meta\"><Data ss:Type=\"String\">Distance: ${session.distanceName} ${session.distanceUnit}</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Meta\"><Data ss:Type=\"String\">Target Size: ${session.targetSizeCm} cm</Data></Cell>\n")
        sb.append("   </Row>\n")
        sb.append("   <Row><Cell/></Row>\n") // empty row

        // Headers
        sb.append("   <Row ss:Height=\"20\">\n")
        sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">End No.</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Arrow No.</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Score Value</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Numeric Value</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Plot X</Data></Cell>\n")
        sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Plot Y</Data></Cell>\n")
        sb.append("   </Row>\n")

        // Data Rows
        for (shot in shots) {
            sb.append("   <Row>\n")
            sb.append("    <Cell><Data ss:Type=\"Number\">${shot.endNumber}</Data></Cell>\n")
            sb.append("    <Cell><Data ss:Type=\"Number\">${shot.shotNumber}</Data></Cell>\n")
            sb.append("    <Cell><Data ss:Type=\"String\">${shot.valueString}</Data></Cell>\n")
            sb.append("    <Cell><Data ss:Type=\"Number\">${shot.valueNum}</Data></Cell>\n")
            sb.append("    <Cell><Data ss:Type=\"String\">${shot.posX ?: ""}</Data></Cell>\n")
            sb.append("    <Cell><Data ss:Type=\"String\">${shot.posY ?: ""}</Data></Cell>\n")
            sb.append("   </Row>\n")
        }

        // Summary Calculations inside sheet
        sb.append("   <Row><Cell/></Row>\n")
        val totalArrows = shots.size
        val totalScore = shots.sumOf { it.valueNum }
        val avgArrow = if (totalArrows > 0) totalScore.toFloat() / totalArrows else 0f
        sb.append("   <Row>\n")
        sb.append("    <Cell ss:StyleID=\"Header\"><Data ss:Type=\"String\">Summary</Data></Cell>\n")
        sb.append("    <Cell><Data ss:Type=\"String\">Total Arrows: $totalArrows</Data></Cell>\n")
        sb.append("    <Cell><Data ss:Type=\"String\">Total Score: $totalScore</Data></Cell>\n")
        sb.append("    <Cell><Data ss:Type=\"String\">Average: ${String.format("%.2f", avgArrow)}</Data></Cell>\n")
        sb.append("   </Row>\n")

        sb.append("  </Table>\n")
        sb.append(" </Worksheet>\n")
    }

    /**
     * Exports entire database tables as a JSON string backup.
     */
    fun exportDb(
        archers: List<Archer>,
        sessions: List<ArcherySession>,
        ends: List<EndSession>,
        shots: List<ArrowShot>
    ): String {
        val root = JSONObject()

        val archersArray = JSONArray()
        for (a in archers) {
            val obj = JSONObject()
            obj.put("id", a.id)
            obj.put("name", a.name)
            obj.put("colorHex", a.colorHex)
            archersArray.put(obj)
        }
        root.put("archers", archersArray)

        val sessionsArray = JSONArray()
        for (s in sessions) {
            val obj = JSONObject()
            obj.put("id", s.id)
            obj.put("name", s.name)
            obj.put("presetType", s.presetType)
            obj.put("distanceName", s.distanceName)
            obj.put("distanceUnit", s.distanceUnit)
            obj.put("targetSizeCm", s.targetSizeCm)
            obj.put("totalEnds", s.totalEnds)
            obj.put("shotsPerEnd", s.shotsPerEnd)
            obj.put("targetFaceType", s.targetFaceType)
            obj.put("isInfinite", s.isInfinite)
            obj.put("isCompleted", s.isCompleted)
            obj.put("archerIdsString", s.archerIdsString)
            obj.put("timestamp", s.timestamp)
            sessionsArray.put(obj)
        }
        root.put("sessions", sessionsArray)

        val endsArray = JSONArray()
        for (e in ends) {
            val obj = JSONObject()
            obj.put("id", e.id)
            obj.put("sessionId", e.sessionId)
            obj.put("endNumber", e.endNumber)
            obj.put("notes", e.notes ?: JSONObject.NULL)
            endsArray.put(obj)
        }
        root.put("ends", endsArray)

        val shotsArray = JSONArray()
        for (sh in shots) {
            val obj = JSONObject()
            obj.put("id", sh.id)
            obj.put("sessionId", sh.sessionId)
            obj.put("endNumber", sh.endNumber)
            obj.put("shotNumber", sh.shotNumber)
            obj.put("archerId", sh.archerId)
            obj.put("valueString", sh.valueString)
            obj.put("valueNum", sh.valueNum)
            obj.put("posX", if (sh.posX != null) sh.posX.toDouble() else JSONObject.NULL)
            obj.put("posY", if (sh.posY != null) sh.posY.toDouble() else JSONObject.NULL)
            obj.put("timestamp", sh.timestamp)
            shotsArray.put(obj)
        }
        root.put("shots", shotsArray)

        return root.toString(2)
    }

    /**
     * Parses full JSON string backup and restores it into database.
     * Returns true if import is successful.
     */
    suspend fun importDb(
        backupJson: String,
        repository: ArcheryRepository
    ): Boolean {
        return try {
            val root = JSONObject(backupJson)

            // Parse archers
            val archersArray = root.optJSONArray("archers")
            if (archersArray != null) {
                for (i in 0 until archersArray.length()) {
                    val obj = archersArray.getJSONObject(i)
                    val archer = Archer(
                        id = obj.optInt("id", 0),
                        name = obj.getString("name"),
                        colorHex = obj.getString("colorHex")
                    )
                    repository.insertArcher(archer)
                }
            }

            // Parse sessions
            val sessionsArray = root.optJSONArray("sessions")
            if (sessionsArray != null) {
                for (i in 0 until sessionsArray.length()) {
                    val obj = sessionsArray.getJSONObject(i)
                    val session = ArcherySession(
                        id = obj.optInt("id", 0),
                        name = obj.getString("name"),
                        presetType = obj.getString("presetType"),
                        distanceName = obj.getString("distanceName"),
                        distanceUnit = obj.getString("distanceUnit"),
                        targetSizeCm = obj.optDouble("targetSizeCm", 122.0),
                        totalEnds = obj.getInt("totalEnds"),
                        shotsPerEnd = obj.getInt("shotsPerEnd"),
                        targetFaceType = obj.getString("targetFaceType"),
                        isInfinite = obj.optBoolean("isInfinite", false),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        archerIdsString = obj.getString("archerIdsString"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                    repository.insertSession(session)
                }
            }

            // Parse ends
            val endsArray = root.optJSONArray("ends")
            if (endsArray != null) {
                for (i in 0 until endsArray.length()) {
                    val obj = endsArray.getJSONObject(i)
                    val endSession = EndSession(
                        id = obj.optInt("id", 0),
                        sessionId = obj.getInt("sessionId"),
                        endNumber = obj.getInt("endNumber"),
                        notes = if (obj.isNull("notes")) null else obj.getString("notes")
                    )
                    repository.insertEnd(endSession)
                }
            }

            // Parse shots
            val shotsArray = root.optJSONArray("shots")
            if (shotsArray != null) {
                for (i in 0 until shotsArray.length()) {
                    val obj = shotsArray.getJSONObject(i)
                    val shot = ArrowShot(
                        id = obj.optInt("id", 0),
                        sessionId = obj.getInt("sessionId"),
                        endNumber = obj.getInt("endNumber"),
                        shotNumber = obj.getInt("shotNumber"),
                        archerId = obj.getInt("archerId"),
                        valueString = obj.getString("valueString"),
                        valueNum = obj.getInt("valueNum"),
                        posX = if (obj.isNull("posX")) null else obj.getDouble("posX").toFloat(),
                        posY = if (obj.isNull("posY")) null else obj.getDouble("posY").toFloat(),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis())
                    )
                    repository.insertShot(shot)
                }
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}
