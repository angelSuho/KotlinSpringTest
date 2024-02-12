package com.learn.kopring.websocket.controller

import com.google.zxing.BarcodeFormat
import com.google.zxing.client.j2se.MatrixToImageWriter
import com.google.zxing.qrcode.QRCodeWriter
import com.learn.kopring.websocket.PresentationPracticeService
import com.learn.kopring.websocket.dto.InsertActionRequest
import com.learn.kopring.websocket.dto.UpdateActionRequest
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import java.io.ByteArrayOutputStream
import java.util.UUID

//@RestController
@Controller
class PresentationStatusController(
    private val presentationPracticeService: PresentationPracticeService,
    private val messagingTemplate: SimpMessagingTemplate,
) {

    @GetMapping("/generate-qrcode")
    fun generateQRCode(): ByteArray {
        val qrCodeContent = "http://localhost:8080/action/${UUID.randomUUID()}"
        val width = 250
        val height = 250

        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(qrCodeContent, BarcodeFormat.QR_CODE, width, height)

        val outputStream = ByteArrayOutputStream()
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", outputStream)
        val qrcodeByteArray = outputStream.toByteArray()
        return qrcodeByteArray
    }

    @MessageMapping("/{qrCode}/insert")
    fun insertPresentationStatus(@PathVariable qrCode: String,
                                 request: InsertActionRequest) {
        presentationPracticeService.insertPresentation(qrCode, request.notificationStatus.toBoolean())
        messagingTemplate.convertAndSend("/practice/$qrCode", request)
    }

    @MessageMapping("/{qrCode}/update")
    fun updatePresentationStatus(@PathVariable qrCode: String,
                                request: UpdateActionRequest) {
        presentationPracticeService.updatePresentationField(qrCode, request)
        messagingTemplate.convertAndSend("/practice/$qrCode", request)
    }

    @MessageMapping("/{qrCode}/delete")
    fun deletePresentationStatus(@PathVariable qrCode: String) {
        presentationPracticeService.deletePresentation(qrCode)
    }

    @MessageMapping("/{qrCode}/{field}")
    fun getPresentationStatus(@PathVariable qrCode: String,
                              @PathVariable field: String) {
        val presentationStatus = presentationPracticeService.getPresentationStatus(qrCode, field)
        messagingTemplate.convertAndSend("/practice/$qrCode", presentationStatus!!)
    }
}