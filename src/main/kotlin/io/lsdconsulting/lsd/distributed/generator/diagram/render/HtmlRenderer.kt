package io.lsdconsulting.lsd.distributed.generator.diagram.render

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import io.lsdconsulting.lsd.distributed.connector.model.InteractionType.*
import j2html.TagCreator
import lsd.format.prettyPrint

private val EVENT_BASED_INTERACTIONS = listOf(CONSUME, PUBLISH)

fun renderHtmlFor(
    path: String,
    requestHeaders: Map<String, Collection<String>>,
    responseHeaders: Map<String, Collection<String>>,
    prettyBody: String,
    duration: Long,
    type: InteractionType
): String {
    return TagCreator.div(
        TagCreator.section(
            TagCreator.h3("Full Path"),
            TagCreator.span(path)
        ),
        if (type == REQUEST) {
            if (requestHeaders.isEmpty()) TagCreator.p() else TagCreator.section(
                TagCreator.h3("Request Headers"),
                TagCreator.p(prettyPrint(requestHeaders))
            )
        } else TagCreator.p(),
        if (type == RESPONSE) {
            if (responseHeaders.isEmpty()) TagCreator.p() else TagCreator.section(
                TagCreator.h3("Response Headers"),
                TagCreator.p(prettyPrint(responseHeaders))
            )
        } else TagCreator.p(),
        if (type in EVENT_BASED_INTERACTIONS) {
            if (requestHeaders.isEmpty()) TagCreator.p() else TagCreator.section(
                TagCreator.h3("Headers"),
                TagCreator.p(prettyPrint(requestHeaders))
            )
        } else TagCreator.p(),
        if (prettyBody.isBlank()) TagCreator.p() else TagCreator.section(
            TagCreator.h3("Body"),
            TagCreator.p(prettyBody),
        ),
        if (type == RESPONSE) TagCreator.section(
            TagCreator.h3("Duration"),
            TagCreator.p("${duration}ms")
        ) else TagCreator.p()
    ).render()
}
