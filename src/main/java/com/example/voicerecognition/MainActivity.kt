package com.example.voicerecognition
//import các thành phần trong các gói cần thiết
import android.Manifest//Sử dụng các lớp trong android.Manifest
import android.content.pm.PackageManager//truy cập các gói được cài đặt trên thiết bị, quản lý quyền của ứng dụng
import android.os.Bundle//đóng gói và truyền các thành phần ứng dụng
import android.speech.RecognitionListener//Giao diện nhận dạng: thông báo bắt đầu, kết thúc, bị lỗi
import android.speech.RecognizerIntent// gửi yêu cầu nhận dạng giọng nói
import android.speech.SpeechRecognizer//nhận dạng giọng nói người dùng:bắt đầu, dừng, phân tích dữ liệu
import android.widget.Button// thêm tiện ích từ gói android.widget
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity//Tương thích ngược: Sử dụng một số tính năng mới trên Android cũ
import androidx.core.app.ActivityCompat// Làm việc với các hoạt động trong ứng dụng Android
import androidx.core.content.ContextCompat// Truy cập tài nguyên hệ thống, tương tác với các thành phần khác của ứng dụng
import android.content.Intent//Thực hiện hoạt động trong hệ thống: ví dụ kích hoạt chức năng gửi email
import android.widget.EditText



class MainActivity : AppCompatActivity() {

    private lateinit var speechRecognizer: SpeechRecognizer// late-initialized
    private lateinit var startButton: Button
    private lateinit var resultEditText: EditText // Sửa TextView thành EditText
//Phương thức onCreate() được gọi khi hoạt động được tạo ra.
    // Trong phương thức này, giao diện người dùng được thiết lập và các trình lắng nghe sự kiện được khởi tạo
    override fun onCreate(savedInstanceState: Bundle?) {//override: ghi đè lên lớp cha
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
//Gán các view từ layout tương ứng với id
        startButton = findViewById(R.id.startButton)
        //resultTextView = findViewById(R.id.resultTextView)
        resultEditText = findViewById(R.id.resultEditText)
//Kiểm tra quyền
        startButton.setOnClickListener {
            checkPermission()
        }
//Tạo một đối tượng SpeechRecognizer và thiết lập một RecognitionListener để xử lý các sự kiện nhận dạng giọng nói.
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this)
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            //Các phương thức của RecognitionListener
            override fun onReadyForSpeech(params: Bundle?) {}
            override fun onBeginningOfSpeech() {}
            override fun onRmsChanged(rmsdB: Float) {}
            override fun onBufferReceived(buffer: ByteArray?) {}
            override fun onEndOfSpeech() {}
            override fun onError(error: Int) {}
            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                if (!matches.isNullOrEmpty()) {
                    //resultTextView.text = matches[0]
                    resultEditText.setText(matches[0])
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {}
            override fun onEvent(eventType: Int, params: Bundle?) {}
        })
    }
// Kiểm tra quyền, nếu chưa thì yêu cầu người dùng cấp quyền
    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                RECORD_AUDIO_PERMISSION_CODE
            )
        } else {
            startSpeechRecognition()
        }
    }
// Nếu được cấp quyền, bắt đầu quá trình nhận dạng
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == RECORD_AUDIO_PERMISSION_CODE && grantResults.isNotEmpty()
            && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startSpeechRecognition()
        }
    }
// Khởi tạo intent nhận dạng giọng nói bằng tiếng việt
    private fun startSpeechRecognition() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "vi-VN")
        //intent.putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true) // Thiết lập tính năng nhận dạng ngoại tuyến
        speechRecognizer.startListening(intent)
    }
// Xác định mã yêu cầu cấp quyền record
    companion object {
        const val RECORD_AUDIO_PERMISSION_CODE = 1
    }
}
