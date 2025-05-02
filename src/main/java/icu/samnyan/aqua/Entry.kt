package icu.samnyan.aqua

import icu.samnyan.aqua.sega.aimedb.AimeDbServer
import icu.samnyan.aqua.spring.AutoChecker
import org.springframework.boot.SpringApplication
import org.springframework.boot.ansi.AnsiOutput
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File
import io.sentry.spring.jakarta.EnableSentry
import org.springframework.core.Ordered

@EnableSentry(
    dsn = "https://cd71ed562e477e3ce3d8d418b424e0d8@sentry.c5y.moe/12",
    exceptionResolverOrder = Ordered.LOWEST_PRECEDENCE
)

@SpringBootApplication
@EnableScheduling
class Entry

fun main(args: Array<String>) {
    AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS)

    // If data/ is not found, create it
    File("data").mkdirs()

    // Run the application
    val ctx = SpringApplication.run(Entry::class.java, *args)

    // Start the AimeDbServer
    val aimeDbServer = ctx.getBean(AimeDbServer::class.java)
    aimeDbServer.start()

    // Start the AutoChecker
    val checker = ctx.getBean(AutoChecker::class.java)
    checker.check()
}

