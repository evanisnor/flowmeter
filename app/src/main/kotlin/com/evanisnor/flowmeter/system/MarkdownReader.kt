package com.evanisnor.flowmeter.system

import android.content.res.Resources
import androidx.annotation.RawRes
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import com.evanisnor.flowmeter.di.AppScope
import com.squareup.anvil.annotations.ContributesBinding
import okio.buffer
import okio.source
import javax.inject.Inject

interface MarkdownReader {
  fun read(
    @RawRes rawResId: Int,
  ): AnnotatedString
}

@ContributesBinding(AppScope::class, MarkdownReader::class)
class RealMarkdownReader
  @Inject
  constructor(
    private val resources: Resources,
  ) : MarkdownReader {
    override fun read(rawResId: Int): AnnotatedString {
      return buildAnnotatedString {
        resources.readLines(rawResId) { line ->
          when {
            line.matches("#\\s+.*".toRegex()) -> {
              heading(line, sp = 24)
            }
            line.matches("##\\s+.*".toRegex()) -> {
              heading(line, sp = 18)
            }
            else -> append(line)
          }
          append("\n")
        }
      }
    }

    private fun AnnotatedString.Builder.heading(
      text: String,
      sp: Int,
    ) {
      withStyle(
        ParagraphStyle(
          lineHeight = TextUnit(24f, TextUnitType.Sp),
        ),
      ) {
        withStyle(
          SpanStyle(
            fontWeight = FontWeight.Bold,
            fontSize = TextUnit(sp.toFloat(), TextUnitType.Sp),
          ),
        ) {
          append(text.trimStart('#', ' '))
        }
      }
    }

    private fun Resources.readLines(
      @RawRes rawResId: Int,
      block: (String) -> Unit,
    ) {
      resources.openRawResource(rawResId).source().use { source ->
        source.buffer().use { buffer ->
          while (true) {
            val line = buffer.readUtf8Line() ?: break
            block(line)
          }
        }
      }
    }
  }
