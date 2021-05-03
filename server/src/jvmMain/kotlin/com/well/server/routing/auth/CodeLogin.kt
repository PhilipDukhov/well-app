package com.well.server.routing.auth

import com.amazonaws.regions.Regions
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import com.well.server.utils.Dependencies
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.sendEmail(
    dependencies: Dependencies
) {
    sendEmail("philip.dukhov@gmail.com", call.receive(), "Hi", "code")
    call.respond("ok")
}

suspend fun PipelineContext<*, ApplicationCall>.sendSms(
    dependencies: Dependencies
) {
    sendSms("Enter code", call.receive())
    call.respond("ok")
}

private fun sendSms(
    message: String,
    phoneNumber: String
) {
    val client = AmazonSNSClientBuilder.standard()
        .build()
    val smsType = MessageAttributeValue()
    smsType.dataType = "String"
    smsType.stringValue = "Transactional"
    client.publish(
        PublishRequest()
            .withMessage(message)
            .withPhoneNumber(phoneNumber)
            .withMessageAttributes(
                mapOf(
                    Pair("DefaultSMSType", smsType)
                )
            )
    )
}

private fun sendEmail(
    source: String,
    destination: String,
    subject: String,
    body: String
) {
    println("sendEmail")
    val client = AmazonSimpleEmailServiceClientBuilder.standard().build()
    println("build")
    val res = client.sendEmail(
        SendEmailRequest()
            .withSource(source)
            .withDestination(
                Destination()
                    .withToAddresses(destination)
            )
            .withMessage(
                Message()
                    .withSubject(Content(subject))
                    .withBody(
                        Body()
                            .withHtml(Content(body))
                    )
            )
    )
    println("sendEmail ${res.messageId}")
}