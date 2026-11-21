package com.ifoodroitracker.service

import android.accessibilityservice.AccessibilityService
import android.util.Log
import android.view.accessibility.AccessibilityNodeInfo
import com.ifoodroitracker.data.entity.ParsedOffer
import java.util.regex.Pattern

/**
 * Parser para extrair dados da tela de oferta do iFood Entregador.
 *
 * A tela típica tem estrutura:
 * - Container principal com textos: "Nova corrida", "Valor", "Distância", "Tempo", "Origem", "Destino"
 * - IDs podem variar, então usamos busca por texto + heurísticas
 */
object IfoodScreenParser {
    private const val TAG = "IfoodScreenParser"

    // Padrões de regex para valores monetários brasileiros
    private val CURRENCY_PATTERN = Pattern.compile(
        "(?:R\\$|RS|R\\s?\\$)\\s*(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)"
    )
    private val CURRENCY_SIMPLE_PATTERN = Pattern.compile(
        "(\\d{1,3}(?:[.,]\\d{3})*(?:[.,]\\d{2})?)\\s*(?:reais|R\\$)?"
    )

    // Padrão para distância (km)
    private val DISTANCE_PATTERN = Pattern.compile(
        "(\\d+(?:[.,]\\d+)?)\\s*km",
        Pattern.CASE_INSENSITIVE
    )

    // Padrão para tempo (minutos)
    private val TIME_PATTERN = Pattern.compile(
        "(\\d+)\\s*min",
        Pattern.CASE_INSENSITIVE
    )

    // Palavras-chave que indicam tela de oferta
    private val OFFER_KEYWORDS = setOf(
        "nova corrida",
        "nova entrega",
        "corrida disponível",
        "oferta de corrida",
        "aceitar corrida",
        "valor da corrida",
        "ganhe por esta corrida"
    )

    // Textos que indicam elementos de valor
    private val VALUE_LABELS = setOf(
        "valor",
        "ganho",
        "receber",
        "pagamento",
        "total"
    )

    // Textos para distância
    private val DISTANCE_LABELS = setOf(
        "distância",
        "distancia",
        "km",
        "quilômetros",
        "quilometros"
    )

    // Textos para tempo
    private val TIME_LABELS = setOf(
        "tempo",
        "duração",
        "duracao",
        "min",
        "minutos",
        "estimado"
    )

    // Textos para origem/destino
    private val ORIGIN_LABELS = setOf(
        "origem",
        "retirada",
        "coleta",
        "restaurante",
        "de"
    )
    private val DESTINATION_LABELS = setOf(
        "destino",
        "entrega",
        "endereço",
        "endereco",
        "para"
    )

    /**
     * Tenta extrair uma oferta da árvore de acessibilidade.
     * Retorna null se não for tela de oferta ou dados insuficientes.
     */
    fun parseOffer(service: AccessibilityService, root: AccessibilityNodeInfo?): ParsedOffer? {
        root?.let { node ->
            val texts = collectAllTexts(node)
            Log.d(TAG, "Textos coletados: $texts")

            // Verifica se é tela de oferta
            if (!isOfferScreen(texts)) {
                Log.d(TAG, "Não é tela de oferta")
                return null
            }

            // Extrai campos
            val value = extractValue(texts, node)
            val distanceKm = extractDistance(texts, node)
            val estimatedMinutes = extractTime(texts, node)
            val origin = extractOrigin(texts, node)
            val destination = extractDestination(texts, node)

            Log.d(TAG, "Extraído: value=$value, distance=$distanceKm, time=$estimatedMinutes, origin=$origin, dest=$destination")

            // Validação mínima
            if (value <= 0 || distanceKm <= 0 || estimatedMinutes <= 0) {
                Log.w(TAG, "Dados insuficientes para criar oferta")
                return null
            }

            return ParsedOffer(
                value = value,
                distanceKm = distanceKm,
                estimatedMinutes = estimatedMinutes,
                origin = origin,
                destination = destination,
                confidence = calculateConfidence(value, distanceKm, estimatedMinutes, origin, destination)
            )
        }
        return null
    }

    private fun collectAllTexts(node: AccessibilityNodeInfo): List<String> {
        val texts = mutableListOf<String>()
        collectTextsRecursive(node, texts)
        return texts.map { it.trim().lowercase() }.filter { it.isNotEmpty() }.distinct()
    }

    private fun collectTextsRecursive(node: AccessibilityNodeInfo, texts: MutableList<String>) {
        // Texto do próprio nó
        node.text?.let { texts.add(it.toString()) }
        node.contentDescription?.let { texts.add(it.toString()) }
        node.hintText?.let { texts.add(it.toString()) }

        // Filhos
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { collectTextsRecursive(it, texts) }
        }
    }

    private fun isOfferScreen(texts: List<String>): Boolean {
        return texts.any { text ->
            OFFER_KEYWORDS.any { keyword -> text.contains(keyword) }
        }
    }

    private fun extractValue(texts: List<String>, root: AccessibilityNodeInfo): Double {
        // 1. Procura por labels de valor + número próximo
        for (label in VALUE_LABELS) {
            val value = findValueNearLabel(root, label)
            if (value > 0) return value
        }

        // 2. Procura maior valor monetário na tela (geralmente é o valor da corrida)
        var maxValue = 0.0
        for (text in texts) {
            CURRENCY_PATTERN.matcher(text).results().forEach { match ->
                val parsed = parseBrazilianCurrency(match.group(1))
                if (parsed > maxValue && parsed <= 500) { // sanity check
                    maxValue = parsed
                }
            }
        }
        return maxValue
    }

    private fun findValueNearLabel(root: AccessibilityNodeInfo, label: String): Double {
        // Busca nó com texto contendo label, depois procura valor nos irmãos/filhos
        val labelNodes = findNodesByText(root, label)
        for (labelNode in labelNodes) {
            // Verifica pai e irmãos
            val parent = labelNode.parent
            parent?.let { p ->
                for (i in 0 until p.childCount) {
                    val sibling = p.getChild(i)
                    sibling?.text?.let { text ->
                        val value = extractCurrencyFromText(text.toString())
                        if (value > 0) return value
                    }
                    sibling?.contentDescription?.let { text ->
                        val value = extractCurrencyFromText(text.toString())
                        if (value > 0) return value
                    }
                }
            }
        }
        return 0.0
    }

    private fun extractDistance(texts: List<String>, root: AccessibilityNodeInfo): Double {
        // 1. Procura por labels de distância
        for (label in DISTANCE_LABELS) {
            val dist = findDistanceNearLabel(root, label)
            if (dist > 0) return dist
        }

        // 2. Procura padrão "X km" ou "X.X km" em qualquer texto
        for (text in texts) {
            DISTANCE_PATTERN.matcher(text).results().forEach { match ->
                val parsed = parseBrazilianNumber(match.group(1))
                if (parsed > 0 && parsed <= 100) return parsed // sanity check
            }
        }
        return 0.0
    }

    private fun findDistanceNearLabel(root: AccessibilityNodeInfo, label: String): Double {
        val labelNodes = findNodesByText(root, label)
        for (labelNode in labelNodes) {
            val parent = labelNode.parent
            parent?.let { p ->
                for (i in 0 until p.childCount) {
                    val sibling = p.getChild(i)
                    sibling?.text?.let { text ->
                        val dist = extractDistanceFromText(text.toString())
                        if (dist > 0) return dist
                    }
                }
            }
        }
        return 0.0
    }

    private fun extractTime(texts: List<String>, root: AccessibilityNodeInfo): Int {
        // 1. Procura por labels de tempo
        for (label in TIME_LABELS) {
            val time = findTimeNearLabel(root, label)
            if (time > 0) return time
        }

        // 2. Procura padrão "X min" ou "X minutos"
        for (text in texts) {
            TIME_PATTERN.matcher(text).results().forEach { match ->
                val parsed = match.group(1).toIntOrNull()
                if (parsed != null && parsed > 0 && parsed <= 180) return parsed // sanity check
            }
        }
        return 0
    }

    private fun findTimeNearLabel(root: AccessibilityNodeInfo, label: String): Int {
        val labelNodes = findNodesByText(root, label)
        for (labelNode in labelNodes) {
            val parent = labelNode.parent
            parent?.let { p ->
                for (i in 0 until p.childCount) {
                    val sibling = p.getChild(i)
                    sibling?.text?.let { text ->
                        val time = extractTimeFromText(text.toString())
                        if (time > 0) return time
                    }
                }
            }
        }
        return 0
    }

    private fun extractOrigin(texts: List<String>, root: AccessibilityNodeInfo): String? {
        return extractLocation(texts, root, ORIGIN_LABELS)
    }

    private fun extractDestination(texts: List<String>, root: AccessibilityNodeInfo): String? {
        return extractLocation(texts, root, DESTINATION_LABELS)
    }

    private fun extractLocation(texts: List<String>, root: AccessibilityNodeInfo, labels: Set<String>): String? {
        for (label in labels) {
            val labelNodes = findNodesByText(root, label)
            for (labelNode in labelNodes) {
                val parent = labelNode.parent
                parent?.let { p ->
                    for (i in 0 until p.childCount) {
                        val sibling = p.getChild(i)
                        if (sibling != labelNode) {
                            val text = sibling.text?.toString()?.trim()
                            if (text != null && text.isNotEmpty() && text.length > 3 && text.length < 100) {
                                // Filtra textos que parecem endereços
                                if (text.any { it.isDigit() } || text.contains("rua") || text.contains("av") || text.contains("r.") || text.contains("av.")) {
                                    return text
                                }
                            }
                        }
                    }
                }
            }
        }
        return null
    }

    private fun findNodesByText(root: AccessibilityNodeInfo, searchText: String): List<AccessibilityNodeInfo> {
        val results = mutableListOf<AccessibilityNodeInfo>()
        findNodesByTextRecursive(root, searchText.lowercase(), results)
        return results
    }

    private fun findNodesByTextRecursive(node: AccessibilityNodeInfo, searchText: String, results: MutableList<AccessibilityNodeInfo>) {
        node.text?.let { text ->
            if (text.toString().lowercase().contains(searchText)) {
                results.add(node)
            }
        }
        node.contentDescription?.let { desc ->
            if (desc.toString().lowercase().contains(searchText)) {
                results.add(node)
            }
        }
        for (i in 0 until node.childCount) {
            node.getChild(i)?.let { findNodesByTextRecursive(it, searchText, results) }
        }
    }

    private fun extractCurrencyFromText(text: String): Double {
        CURRENCY_PATTERN.matcher(text).results()
            .map { parseBrazilianCurrency(it.group(1)) }
            .filter { it > 0 && it <= 500 }
            .maxOrNull() ?: 0.0
    }

    private fun extractDistanceFromText(text: String): Double {
        DISTANCE_PATTERN.matcher(text).results()
            .map { parseBrazilianNumber(it.group(1)) }
            .filter { it > 0 && it <= 100 }
            .maxOrNull() ?: 0.0
    }

    private fun extractTimeFromText(text: String): Int {
        TIME_PATTERN.matcher(text).results()
            .map { it.group(1).toIntOrNull() ?: 0 }
            .filter { it > 0 && it <= 180 }
            .maxOrNull() ?: 0
    }

    private fun parseBrazilianCurrency(str: String): Double {
        // "18,50" ou "18.50" ou "1.234,56" ou "1,234.56"
        val normalized = str.replace(".", "").replace(",", ".").replace(" ", "")
        return normalized.toDoubleOrNull() ?: 0.0
    }

    private fun parseBrazilianNumber(str: String): Double {
        val normalized = str.replace(",", ".").replace(" ", "")
        return normalized.toDoubleOrNull() ?: 0.0
    }

    private fun calculateConfidence(
        value: Double, distance: Double, time: Int, origin: String?, dest: String?
    ): Double {
        var confidence = 0.5
        if (value > 0) confidence += 0.15
        if (distance > 0) confidence += 0.15
        if (time > 0) confidence += 0.1
        if (origin != null) confidence += 0.05
        if (dest != null) confidence += 0.05
        return confidence.coerceAtMost(1.0)
    }
}