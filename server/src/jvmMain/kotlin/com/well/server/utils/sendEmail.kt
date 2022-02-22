package com.well.server.utils

import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClientBuilder
import com.amazonaws.services.simpleemail.model.Body
import com.amazonaws.services.simpleemail.model.Content
import com.amazonaws.services.simpleemail.model.Destination
import com.amazonaws.services.simpleemail.model.Message
import com.amazonaws.services.simpleemail.model.SendEmailRequest

fun sendEmail(
    source: String = "worldendolivelink@gmail.com",
    destination: String,
    subject: String,
    body: String,
) {
    AmazonSimpleEmailServiceClientBuilder
        .standard()
        .build()
        .sendEmail(
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
}