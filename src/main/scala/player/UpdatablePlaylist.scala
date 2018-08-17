package player

/** To which songs can be added, and the index changed. */
private sealed trait UpdatablePlaylist extends Playlist {
  def add(s: Song): UpdatablePlaylist
  def setIndex(index: Int): UpdatablePlaylist
  def next: UpdatablePlaylist = setIndex(currentIndex + 1)
  def previous: UpdatablePlaylist = setIndex(currentIndex - 1)
}

private object UpdatablePlaylist {
  private object EmptyPlaylist extends UpdatablePlaylist {
    override def songs = Nil
    override def currentIndex = throw new UnsupportedOperationException("Empty playlist")
    override def add(s: Song) = new NonEmptyPlaylist(Vector(s), 0)
    override def setIndex(index: Int) = throw new UnsupportedOperationException("Empty playlist")
    override def isLastSong = throw new UnsupportedOperationException("Empty playlist")
    override def isFirstSong = throw new UnsupportedOperationException("Empty playlist")
  }
  private class NonEmptyPlaylist(override val songs: Vector[Song], override val currentIndex: Int) extends UpdatablePlaylist {
    override val size: Int = songs.size
    assert(currentIndex >= 0 && currentIndex < size)

    override def add(s: Song): UpdatablePlaylist = new NonEmptyPlaylist(songs :+ s, currentIndex)

    override def setIndex(index: Int): UpdatablePlaylist =
      if (index < 0 || index >= size)
        throw new IndexOutOfBoundsException(s"Cannot set index <$index> for playlist of size <$size>")
      else
        new NonEmptyPlaylist(songs, index)
  }

  def empty: UpdatablePlaylist = EmptyPlaylist
}
