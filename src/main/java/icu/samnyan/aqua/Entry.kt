package icu.samnyan.aqua

import org.springframework.boot.SpringApplication
import org.springframework.boot.ansi.AnsiOutput
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.scheduling.annotation.EnableScheduling
import java.io.File
import org.springframework.core.Ordered

@SpringBootApplication
@EnableScheduling
class Entry

fun main(args: Array<String>) {
    AnsiOutput.setEnabled(AnsiOutput.Enabled.ALWAYS)

    // If data/ is not found, create it
    File("data").mkdirs()

    // Run the application
    val ctx = SpringApplication.run(Entry::class.java, *args)
}

