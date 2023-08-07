package io.lsdconsulting.lsd.distributed.generator.diagram.render

import io.lsdconsulting.lsd.distributed.connector.model.InteractionType
import j2html.TagCreator
import lsd.format.prettyPrint

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
        if (type == InteractionType.REQUEST) {
            if (requestHeaders.isEmpty()) TagCreator.p() else TagCreator.section(
                TagCreator.h3("Request Headers"),
                TagCreator.p(prettyPrint(requestHeaders))
            )
        } else TagCreator.p(),
        if (type == InteractionType.RESPONSE) {
            if (responseHeaders.isEmpty()) TagCreator.p() else TagCreator.section(
                TagCreator.h3("Response Headers"),
                TagCreator.p(prettyPrint(responseHeaders))
            )
        } else TagCreator.p(),
        if (type in listOf(InteractionType.CONSUME, InteractionType.PUBLISH)) {
            if (requestHeaders.isEmpty()) TagCreator.p() else TagCreator.section(
                TagCreator.h3("Headers"),
                TagCreator.p(prettyPrint(requestHeaders))
            )
        } else TagCreator.p(),
        if (prettyBody.isBlank()) TagCreator.p() else TagCreator.section(
            TagCreator.h3("Body"),
            TagCreator.p(prettyBody),
        ),
        if (type == InteractionType.RESPONSE) TagCreator.section(
            TagCreator.h3("Duration"),
            TagCreator.p("${duration}ms")
        ) else TagCreator.p()
    ).render()
}
