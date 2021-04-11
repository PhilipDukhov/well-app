package com.well.server.routing.auth

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sns.model.PublishRequest
import com.well.server.utils.Dependencies
import io.ktor.application.*
import io.ktor.response.*
import io.ktor.util.pipeline.*

suspend fun PipelineContext<*, ApplicationCall>.sendEmail(
    dependencies: Dependencies
) {
    sendEmail("philip.dukhov@gmail.com", "philip.dukhov@gmail.com", "Hi", "code")
    call.respond("ok")
}

private fun sms() {
    val client = AmazonSNSClientBuilder.defaultClient()
    client.publish(
        PublishRequest()
            .withMessage("")
            .withPhoneNumber("")
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