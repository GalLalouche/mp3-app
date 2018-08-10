package player.playlist

import player.Song

sealed trait PlaylistEvent

case class SongAdded(s: Song, index: Int) extends PlaylistEvent
case class CurrentChanged(s: Song, index: Int) extends PlaylistEvent
case class SongRemoved(s: Song, index: Int) extends PlaylistEvent
