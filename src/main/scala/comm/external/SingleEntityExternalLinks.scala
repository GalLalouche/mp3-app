package comm.external

import java.time.MonthDay

case class SingleEntityExternalLinks(
    entityType: ExternalLinkEntity, links: Seq[ExternalLink], timestamp: MonthDay)
