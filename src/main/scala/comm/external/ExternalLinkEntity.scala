package comm.external

sealed trait ExternalLinkEntity
object ExternalLinkEntity {
  case object Artist extends ExternalLinkEntity
  case object Album extends ExternalLinkEntity
}
