package com.well.server.routing.auth

import com.well.server.utils.Dependencies
import com.well.server.utils.sendEmail
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sns.model.MessageAttributeValue
import com.amazonaws.services.sns.model.PublishRequest
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.sendEmail(
    dependencies: Dependencies,
) {
    sendEmail("philip.dukhov@gmail.com", call.receive(), "Hi", "code")
    call.respond(HttpStatusCode.OK)
}

suspend fun PipelineContext<*, ApplicationCall>.sendSms(
    dependencies: Dependencies,
) {
    sendSms("Enter code", call.receive())
    call.respond(HttpStatusCode.OK)
}

private fun sendSms(
    message: String,
    phoneNumber: String,
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

