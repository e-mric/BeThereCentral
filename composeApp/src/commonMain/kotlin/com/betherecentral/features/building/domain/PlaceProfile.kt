package com.betherecentral.features.building.domain

/** Optional, attributable facts that may be shown alongside a source-listed place. */
enum class PlaceKind { COMPANY, MEETING_ROOM, OTHER }

data class PlaceProfile(
    val kind: PlaceKind = PlaceKind.OTHER,
    val mission: String? = null,
    val purpose: String? = null,
    val extraInfo: List<String> = emptyList(),
    val links: List<PlaceLink> = emptyList(),
    val providedBy: String? = null,
) {
    init {
        require(mission == null || mission.isNotBlank()) { "Mission must be nonblank when supplied" }
        require(purpose == null || purpose.isNotBlank()) { "Purpose must be nonblank when supplied" }
        require(extraInfo.all(String::isNotBlank)) { "Extra information must be nonblank" }
        val hasContent = mission != null || purpose != null || extraInfo.isNotEmpty() || links.isNotEmpty()
        require(!hasContent || !providedBy.isNullOrBlank()) {
            "A nonblank provider is required when profile content is supplied"
        }
    }
}

data class PlaceLink(val label: String, val url: String) {
    init {
        require(label.isNotBlank()) { "Link label must be nonblank" }
        require(isValidHttpsUrl(url)) { "Link URL must be a valid HTTPS URL" }
    }
}

private fun isValidHttpsUrl(url: String): Boolean {
    if (!url.startsWith("https://") || url.any(Char::isWhitespace)) return false
    val authority = url.removePrefix("https://").substringBefore('/').substringBefore('?').substringBefore('#')
    if (authority.isBlank() || authority.startsWith('.') || authority.endsWith('.') || '@' in authority) return false
    val host = authority.substringBefore(':')
    return host.isNotBlank() && ('.' in host || host == "localhost")
}
