package com.well.server.routing.auth

import com.well.server.utils.Dependencies
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.util.pipeline.*
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.File
import java.io.FileReader
import java.lang.Exception
import java.security.PrivateKey

suspend fun PipelineContext<*, ApplicationCall>.appleLogin(
    dependencies: Dependencies
) = dependencies.run {
    val token = call.receive<String>()
//    call.respond("ok")
}

@Throws(Exception::class)
private fun generatePrivateKey(): PrivateKey? {
    val file = File("CERTIFICATE_PATH")
    val pemParser = PEMParser(FileReader(file))
    val converter = JcaPEMKeyConverter()
    val `object`: PrivateKeyInfo = pemParser.readObject() as PrivateKeyInfo
    return converter.getPrivateKey(`object`)
}