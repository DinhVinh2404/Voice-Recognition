package com.example.voicerecognition

//import các thành phần trong các gói cần thiết
import android.Manifest//Sử dụng các lớp trong android.Manifest
import android.content.pm.PackageManager//truy cập các gói được cài đặt trên thiết bị, quản lý quyền của ứng dụng
import android.os.Bundle//đóng gói và truyền các thành phần ứng dụng
import android.speech.RecognitionListener//Giao diện nhận dạng: thông báo bắt đầu, kết thúc, bị lỗi
import android.speech.RecognizerIntent// gửi yêu cầu nhận dạng giọng nói
import android.speech.SpeechRecognizer//nhận dạng giọng nói người dùng:bắt đầu, dừng, phân tích dữ liệu
import android.widget.Button// thêm tiện ích từ gói android.widget
//import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity//Tương thích ngược: Sử dụng một số tính năng mới trên Android cũ
import androidx.core.app.ActivityCompat// Làm việc với các hoạt động trong ứng dụng Android
import androidx.core.content.ContextCompat// Truy cập tài nguyên hệ thống, tương tác với các thành phần khác của ứng dụng
import android.content.Intent//Thực hiện hoạt động trong hệ thống: ví dụ kích hoạt chức năng gửi email
import android.widget.EditText
import android.widget.Toast//Hiển thị thông báo ngắn
import android.provider.ContactsContract


class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer
    private lateinit var startButton: Button
    private lateinit var resultEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {//override: ghi đè lên lớp cha
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        startButton = findViewById(R.id.startButton)
        resultEditText = findViewById(R.id.resultEditText)

        startButton.setOnClickListener {
            checkPermission()
        }
    //+++++++++++++++++++ HÀM HỆ THỐNG +++++++++++++++++++++++
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    resultEditText.setText(matches[0])
                    handleVoiceCommand(matches[0].lowercase())
                }
            }
            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }

    private fun checkPermission() {
        val permissionsToCheck = listOf(
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,
            Manifest.permission.CALL_PHONE
        )

        val permissionsNeeded = permissionsToCheck.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                PERMISSION_REQUEST_CODE
            )
        } else {
            startSpeechRecognition()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST_CODE) {
            // Xử lý kết quả yêu cầu quyền ở đây
            startSpeechRecognition()
        }
    }


    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
        speechRecognizer.startListening(intent)
    }


    //+++++++++++++++++++ HÀM CHỨC NĂNG +++++++++++++++++++++++

    private fun handleVoiceCommand(command: String) {
        when {
            command.contains("mở youtube") ||
                    command.contains("mở ứng dụng youtube") ||
                    command.contains("mở phần mềm youtube") -> openYouTube()
            command.contains("gọi") -> {
                val contactName = extractContactName(command)

                if (contactName != null) {
                    val phoneNumber = findPhoneNumber(contactName)
                    if (phoneNumber != null) {
                        callContact(phoneNumber)
                    }
                } else {
                    showToast("Không tìm thấy tên trong danh bạ")
                }
            }
            else -> {
                // Handle other commands here
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun openYouTube() {
        val packageManager = packageManager
        val intent = packageManager.getLaunchIntentForPackage("com.google.android.youtube")
        if (intent != null) {
            intent.addCategory(Intent.CATEGORY_LAUNCHER)
            startActivity(intent)
        } else {
            showToast("Không thấy ứng dụng YouTube")
        }
    }

    private fun extractContactName(command: String): String? {
        val callPrefix = "gọi"

        return if (command.startsWith(callPrefix)) {
             command.substring(callPrefix.length).trim()
        } else {
            null
        }
    }

    private fun findPhoneNumber(contactName: String): String? {
        var phoneNumber: String? = null
        val contentResolver = applicationContext.contentResolver
        val cursor = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " = ?",
            arrayOf(contactName),
            null
        )
        cursor?.use { // Ensure the cursor is closed after use
            if (it.moveToFirst()) {
                val phoneNumberColumnIndex = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                if (phoneNumberColumnIndex != -1) {
                    phoneNumber = it.getString(phoneNumberColumnIndex)
                }
            }
        }
        showToast(phoneNumber + "")
        return phoneNumber
    }

    private fun callContact(phoneNumber: String) {
        val callIntent = Intent(Intent.ACTION_CALL).apply {
            data = android.net.Uri.parse("tel:$phoneNumber")
        }
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.CALL_PHONE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startActivity(callIntent)
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CALL_PHONE),
                CALL_PHONE_PERMISSION_CODE
            )
        }
    }

    companion object {
//        const val RECORD_AUDIO_PERMISSION_CODE = 1
        const val CALL_PHONE_PERMISSION_CODE = 2
        private const val PERMISSION_REQUEST_CODE = 1001
    }

}