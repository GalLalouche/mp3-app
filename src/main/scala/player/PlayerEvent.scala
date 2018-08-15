package player

sealed trait PlayerEvent
case class SongAdded(s: Song, index: Int) extends PlayerEvent
case class CurrentChanged(s: Song, index: Int) extends PlayerEvent
case class SongRemoved(s: Song, index: Int) extends PlayerEvent
case class StatusChanged(newStatus: PlayerStatus) extends PlayerEvent
