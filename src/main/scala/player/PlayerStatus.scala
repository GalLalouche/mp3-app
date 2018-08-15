package player

sealed trait PlayerStatus

case object Playing extends PlayerStatus
case object Paused extends PlayerStatus
case object Stopped extends PlayerStatus
/** Empty playlist. */
case object Initial extends PlayerStatus
