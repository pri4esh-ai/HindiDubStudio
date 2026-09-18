package com.hindidubstudio

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.graphics.Color
import android.graphics.Typeface
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private lateinit var selectedFileText: TextView
    private lateinit var translationText: EditText
    private lateinit var speakerContainer: LinearLayout
    private lateinit var voiceModeGroup: RadioGroup
    private lateinit var voiceSampleStatus: TextView
    private lateinit var previewStatus: TextView
    private lateinit var generateButton: Button

    private var selectedMediaUri: Uri? = null
    private var activeSpeaker = 1

    private val speakerSamples = mutableMapOf<Int, Uri?>(
        1 to null,
        2 to null
    )

    private val FILE_REQUEST = 1001
    private val VOICE_SAMPLE_REQUEST = 1002

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        buildInterface()
    }

    private fun buildInterface() {

        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(24))
            setBackgroundColor(Color.rgb(248, 248, 250))
        }

        val scrollView = ScrollView(this).apply {
            addView(root)
        }

        setContentView(scrollView)

        addTitle()
        addMediaSection()
        addSpeakerSection()
        addTranslationSection()
        addVoiceSection()
        addPreviewSection()
        addGenerateSection()
    }

    // ---------------------------------------------------------
    // TITLE
    // ---------------------------------------------------------

    private fun addTitle() {

        val title = TextView(this).apply {
            text = "HindiDubStudio"
            textSize = 28f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(25, 25, 30))
            setPadding(0, 0, 0, dp(6))
        }

        root.addView(title)

        val subtitle = TextView(this).apply {
            text = "Offline English → Hindi dubbing studio"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, dp(20))
        }

        root.addView(subtitle)
    }

    // ---------------------------------------------------------
    // MEDIA
    // ---------------------------------------------------------

    private fun addMediaSection() {

        addSectionTitle("MEDIA")

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val videoButton = createButton("🎬 Select Video")

        videoButton.setOnClickListener {
            openMediaPicker()
        }

        val audioButton = createButton("🎵 Select Audio")

        audioButton.setOnClickListener {
            openMediaPicker()
        }

        row.addView(
            videoButton,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(0, 0, dp(6), 0)
            }
        )

        row.addView(
            audioButton,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                setMargins(dp(6), 0, 0, 0)
            }
        )

        root.addView(row)

        selectedFileText = TextView(this).apply {
            text = "No file selected"
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(dp(4), dp(12), dp(4), dp(12))
        }

        root.addView(selectedFileText)
    }

    private fun openMediaPicker() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            putExtra(
                Intent.EXTRA_MIME_TYPES,
                arrayOf(
                    "video/mp4",
                    "audio/mpeg",
                    "audio/wav",
                    "audio/x-wav",
                    "audio/mp4"
                )
            )
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(intent, FILE_REQUEST)
    }

    // ---------------------------------------------------------
    // SPEAKERS
    // ---------------------------------------------------------

    private fun addSpeakerSection() {

        addDivider()
        addSectionTitle("SPEAKERS")

        speakerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        root.addView(speakerContainer)

        refreshSpeakers()

        val addSpeakerButton = createButton("+ Add Speaker")

        addSpeakerButton.setOnClickListener {

            val nextSpeaker = speakerSamples.keys.maxOrNull()?.plus(1) ?: 1

            speakerSamples[nextSpeaker] = null

            refreshSpeakers()
        }

        root.addView(
            addSpeakerButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        )
    }

    private fun refreshSpeakers() {

        speakerContainer.removeAllViews()

        speakerSamples.keys.sorted().forEach { speakerId ->

            val card = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                setPadding(dp(14), dp(12), dp(14), dp(12))
                setBackgroundColor(Color.WHITE)
            }

            val speakerTitle = TextView(this).apply {
                text = "Speaker $speakerId"
                textSize = 18f
                setTypeface(null, Typeface.BOLD)
                setTextColor(Color.rgb(30, 30, 35))
            }

            card.addView(speakerTitle)

            val sampleStatus = TextView(this).apply {
                text = if (speakerSamples[speakerId] == null) {
                    "No voice sample"
                } else {
                    "Voice sample added"
                }

                textSize = 13f
                setTextColor(Color.DKGRAY)
                setPadding(0, dp(4), 0, dp(8))
            }

            card.addView(sampleStatus)

            val buttonRow = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
            }

            val sampleButton = createButton(
                if (speakerSamples[speakerId] == null) {
                    "🎙 Add Voice Sample"
                } else {
                    "🎙 Change Voice Sample"
                }
            )

            sampleButton.setOnClickListener {

                activeSpeaker = speakerId

                openVoiceSamplePicker()
            }

            buttonRow.addView(
                sampleButton,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            )

            val previewButton = createButton("▶ Preview")

            previewButton.setOnClickListener {

                if (speakerSamples[speakerId] == null) {
                    showMessage(
                        "Add a voice sample for Speaker $speakerId first."
                    )
                } else {
                    showMessage(
                        "Voice sample preview will be connected to the voice engine."
                    )
                }
            }

            buttonRow.addView(
                previewButton,
                LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    marginStart = dp(8)
                }
            )

            card.addView(buttonRow)

            val params = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(10)
            }

            speakerContainer.addView(card, params)
        }
    }

    // ---------------------------------------------------------
    // TRANSLATION
    // ---------------------------------------------------------

    private fun addTranslationSection() {

        addDivider()
        addSectionTitle("TRANSLATION")

        val englishLabel = TextView(this).apply {
            text = "English"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
        }

        root.addView(englishLabel)

        val englishText = TextView(this).apply {
            text = "Hello, how are you?"
            textSize = 16f
            setPadding(0, dp(6), 0, dp(14))
        }

        root.addView(englishText)

        val hindiLabel = TextView(this).apply {
            text = "Hindi"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
        }

        root.addView(hindiLabel)

        translationText = EditText(this).apply {
            setText("नमस्ते, आप कैसे हैं?")
            textSize = 17f
            gravity = Gravity.TOP
            minLines = 3
            setPadding(dp(12), dp(10), dp(12), dp(10))
            hint = "Edit Hindi translation"
        }

        root.addView(
            translationText,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(6)
            }
        )

        val editButton = createButton("✏ Edit Translation")

        editButton.setOnClickListener {
            translationText.requestFocus()
        }

        root.addView(
            editButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(8)
            }
        )

        val replaceRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val replaceButton = createButton("Smart Replace")

        replaceButton.setOnClickListener {
            showReplaceDialog(false)
        }

        val replaceAllButton = createButton("Replace All")

        replaceAllButton.setOnClickListener {
            showReplaceDialog(true)
        }

        replaceRow.addView(
            replaceButton,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        replaceRow.addView(
            replaceAllButton,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = dp(8)
            }
        )

        root.addView(replaceRow)
    }

    private fun showReplaceDialog(replaceAll: Boolean) {

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }

        val findInput = EditText(this).apply {
            hint = "Find text"
        }

        val replaceInput = EditText(this).apply {
            hint = "Replace with"
        }

        layout.addView(findInput)
        layout.addView(replaceInput)

        val dialog = android.app.AlertDialog.Builder(this)
            .setTitle(
                if (replaceAll) "Replace All" else "Smart Replace"
            )
            .setView(layout)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Replace") { _, _ ->

                val find = findInput.text.toString()
                val replacement = replaceInput.text.toString()

                if (find.isEmpty()) return@setPositiveButton

                val current = translationText.text.toString()

                translationText.setText(
                    if (replaceAll) {
                        current.replace(find, replacement)
                    } else {
                        current.replaceFirst(find, replacement)
                    }
                )
            }
            .create()

        dialog.show()
    }

    // ---------------------------------------------------------
    // VOICE MODE
    // ---------------------------------------------------------

    private fun addVoiceSection() {

        addDivider()
        addSectionTitle("VOICE MODE")

        voiceModeGroup = RadioGroup(this).apply {
            orientation = RadioGroup.VERTICAL
        }

        val originalVoice = RadioButton(this).apply {
            id = View.generateViewId()
            text = "Original Voice"
            textSize = 16f
        }

        val selectVoice = RadioButton(this).apply {
            id = View.generateViewId()
            text = "Select Voice"
            textSize = 16f
        }

        voiceModeGroup.addView(originalVoice)
        voiceModeGroup.addView(selectVoice)

        originalVoice.isChecked = true

        root.addView(voiceModeGroup)

        voiceSampleStatus = TextView(this).apply {
            text = "Original speaker voice will be used."
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(8), 0, dp(8))
        }

        root.addView(voiceSampleStatus)

        voiceModeGroup.setOnCheckedChangeListener { _, checkedId ->

            if (checkedId == originalVoice.id) {

                voiceSampleStatus.text =
                    "Original speaker voice will be used."

            } else {

                voiceSampleStatus.text =
                    "Select a speaker and add a reference voice sample."
            }
        }

        val addSampleButton = createButton("🎙 Add Voice Sample")

        addSampleButton.setOnClickListener {

            openVoiceSamplePicker()
        }

        root.addView(addSampleButton)
    }

    private fun openVoiceSamplePicker() {

        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "audio/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }

        startActivityForResult(intent, VOICE_SAMPLE_REQUEST)
    }

    // ---------------------------------------------------------
    // PREVIEW
    // ---------------------------------------------------------

    private fun addPreviewSection() {

        addDivider()
        addSectionTitle("PREVIEW")

        val previewButton = createButton("▶ Preview Line")

        previewButton.setOnClickListener {

            val text = translationText.text.toString().trim()

            if (text.isEmpty()) {
                showMessage("Enter Hindi translation first.")
                return@setOnClickListener
            }

            previewStatus.text =
                "Preview requested. Voice engine will generate this line."

        }

        root.addView(previewButton)

        previewStatus = TextView(this).apply {
            text = "No preview generated."
            textSize = 14f
            setTextColor(Color.DKGRAY)
            setPadding(0, dp(8), 0, dp(8))
        }

        root.addView(previewStatus)
    }

    // ---------------------------------------------------------
    // GENERATE / EXPORT
    // ---------------------------------------------------------

    private fun addGenerateSection() {

        addDivider()

        generateButton = createButton("🚀 Generate Hindi Dub").apply {
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
        }

        generateButton.setOnClickListener {

            if (selectedMediaUri == null) {
                showMessage("Select an MP4, MP3 or WAV file first.")
                return@setOnClickListener
            }

            if (translationText.text.toString().trim().isEmpty()) {
                showMessage("Enter the Hindi translation first.")
                return@setOnClickListener
            }

            generateButton.isEnabled = false
            generateButton.text = "Generating..."

            Toast.makeText(
                this,
                "Dubbing engine will be connected in the next stage.",
                Toast.LENGTH_LONG
            ).show()

            generateButton.postDelayed({

                generateButton.isEnabled = true
                generateButton.text = "🚀 Generate Hindi Dub"

            }, 1500)
        }

        root.addView(
            generateButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dp(10)
            }
        )

        val exportTitle = TextView(this).apply {
            text = "Export"
            textSize = 14f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, dp(16), 0, dp(6))
        }

        root.addView(exportTitle)

        val exportRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val mp3Button = createButton("🎵 MP3")

        mp3Button.setOnClickListener {
            showMessage("MP3 export engine will be connected later.")
        }

        val mp4Button = createButton("🎬 MP4")

        mp4Button.setOnClickListener {
            showMessage("MP4 export engine will be connected later.")
        }

        exportRow.addView(
            mp3Button,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        )

        exportRow.addView(
            mp4Button,
            LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            ).apply {
                marginStart = dp(8)
            }
        )

        root.addView(exportRow)
    }

    // ---------------------------------------------------------
    // ACTIVITY RESULT
    // ---------------------------------------------------------

    @Deprecated("Using Activity Result API is not required for this initial dependency-free UI.")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode != RESULT_OK) return

        val uri = data?.data ?: return

        when (requestCode) {

            FILE_REQUEST -> {

                selectedMediaUri = uri

                val name = getFileName(uri)

                selectedFileText.text =
                    "Selected: ${name ?: "Media file"}"
            }

            VOICE_SAMPLE_REQUEST -> {

                speakerSamples[activeSpeaker] = uri

                voiceSampleStatus.text =
                    "Reference voice sample added for Speaker $activeSpeaker."

                refreshSpeakers()
            }
        }
    }

    // ---------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------

    private fun getFileName(uri: Uri): String? {

        var result: String? = null

        contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )?.use { cursor ->

            if (cursor.moveToFirst()) {

                val index =
                    cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)

                if (index >= 0) {
                    result = cursor.getString(index)
                }
            }
        }

        return result
    }

    private fun addSectionTitle(text: String) {

        val title = TextView(this).apply {
            this.text = text
            textSize = 13f
            setTypeface(null, Typeface.BOLD)
            setTextColor(Color.rgb(90, 90, 100))
            setPadding(0, dp(4), 0, dp(8))
        }

        root.addView(title)
    }

    private fun addDivider() {

        val divider = View(this).apply {
            setBackgroundColor(Color.LTGRAY)
        }

        root.addView(
            divider,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                dp(1)
            ).apply {
                topMargin = dp(14)
                bottomMargin = dp(14)
            }
        )
    }

    private fun createButton(text: String): Button {

        return Button(this).apply {
            this.text = text
            textSize = 14f
            isAllCaps = false
            minHeight = dp(48)
        }
    }

    private fun showMessage(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun dp(value: Int): Int {

        return (value * resources.displayMetrics.density)
            .toInt()
    }
}q
