package com.itravelguide.data

import java.text.Normalizer
import kotlin.random.Random

/**
 * A lightweight, offline question generator that simulates AI-generated
 * questions by creating fill-in-the-blank prompts from long-form text.
 */
object QuestionGenerator {
    private val sentenceDelimiters = Regex("[.!?]\s+")
    private val wordRegex = Regex("[\u00C0-\u1EF9\w']+")

    fun generateQuestions(
        text: String,
        isVietnamese: Boolean,
        maxQuestions: Int = 5,
    ): List<String> {
        if (text.isBlank()) return emptyList()

        val sentences = text
            .replace('\n', ' ')
            .split(sentenceDelimiters)
            .map { it.trim() }
            .filter { it.split(' ').size >= 6 }
            .ifEmpty { listOf(text.trim()) }

        val seed = Normalizer.normalize(text.take(64), Normalizer.Form.NFD)
            .filter { it.isLetter() }
            .sumOf { it.code }
        val random = Random(seed)

        val generated = mutableListOf<String>()
        val usedSentences = mutableSetOf<Int>()

        while (generated.size < maxQuestions && usedSentences.size < sentences.size) {
            val index = random.nextInt(sentences.size)
            if (!usedSentences.add(index)) continue
            val sentence = sentences[index]
            val question = buildQuestion(sentence, isVietnamese)
            if (question != null) {
                generated += question
            }
        }

        return generated
    }

    private fun buildQuestion(sentence: String, isVietnamese: Boolean): String? {
        val words = wordRegex.findAll(sentence)
            .map { it.value }
            .toList()
            .filter { it.length > 3 }
            .ifEmpty { return null }

        val target = words.maxByOrNull { it.length } ?: return null
        val blankSentence = sentence.replaceFirst(target, "_____")

        return if (isVietnamese) {
            "Điền vào chỗ trống: $blankSentence"
        } else {
            "Fill in the blank: $blankSentence"
        }
    }
}
