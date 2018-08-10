package ui.playlist

private sealed trait PlaylistElementEvent {
  def source: PlaylistElement
}

private case class PlayElement(source: PlaylistElement) extends PlaylistElementEvent
private case class RemoveElement(source: PlaylistElement) extends PlaylistElementEvent
