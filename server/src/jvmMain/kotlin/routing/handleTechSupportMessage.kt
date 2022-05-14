package com.well.server.routing

import com.well.modules.utils.kotlinUtils.letNamed
import com.well.server.utils.Services
import com.well.server.utils.authUid
import com.amazonaws.services.simpleemail.model.RawMessage
import com.amazonaws.services.simpleemail.model.SendRawEmailRequest
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.util.*
import javax.activation.DataHandler
import javax.mail.Message
import javax.mail.Session
import javax.mail.internet.MimeBodyPart
import javax.mail.internet.MimeMessage
import javax.mail.internet.MimeMultipart
import javax.mail.util.ByteArrayDataSource

suspend fun PipelineContext<*, ApplicationCall>.handleTechSupportMessage(
    services: Services,
) {
    val uid = call.authUid
    var message: String? = null
    var fileBytesAndName: Pair<ByteArray, String?>? = null
    call.receiveMultipart()
        .forEachPart { part ->
            println("part $part")
            when (part) {
                is PartData.FormItem -> {
                    message = part.value
                }
                is PartData.FileItem -> {
                    fileBytesAndName = part.streamProvider().readBytes() to part.originalFileName
                    part.dispose()
                }
                else -> {
                    throw IllegalStateException("unexpected part $part")
                }
            }
        }
    val session = Session.getInstance(Properties(System.getProperties()))
    val mimeMessage = MimeMessage(session)

    mimeMessage.setSubject("Tech support from $uid", "UTF-8")
    mimeMessage.setFrom("worldendolivelink@gmail.com")
    mimeMessage.setRecipients(Message.RecipientType.TO, "philip.dukhov@gmail.com")
    val msgBody = MimeMultipart("alternative")
    val wrap = MimeBodyPart()
    val htmlPart = MimeBodyPart()
    htmlPart.setContent(message, "text/html; charset=UTF-8")
    msgBody.addBodyPart(htmlPart)
    wrap.setContent(msgBody)
    val msg = MimeMultipart("mixed")
    msg.addBodyPart(wrap)

    fileBytesAndName?.letNamed { fileBytes, filename ->
        val messageBodyPart = MimeBodyPart()
        messageBodyPart.dataHandler = DataHandler(ByteArrayDataSource(fileBytes, "application/zip"))
        messageBodyPart.fileName = filename ?: "logs.zip"
        msg.addBodyPart(messageBodyPart)
    }
    mimeMessage.setContent(msg)
    val outputStream = ByteArrayOutputStream()
    withContext(Dispatchers.IO) {
        mimeMessage.writeTo(outputStream)
    }
    val rawMessage = RawMessage(ByteBuffer.wrap(outputStream.toByteArray()))
    val rawEmailRequest = SendRawEmailRequest(rawMessage)
    services.awsManager.simpleEmailService.sendRawEmail(rawEmailRequest)
    call.respond(HttpStatusCode.OK)
}