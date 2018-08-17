package player

/**
 * A Playlist may be empty, in which case all methods except songs, size, and isEmpty will throw an exception.
 * The methods aren't total because I'm lazy.
 */
trait Playlist {
  def songs: Seq[Song]
  def currentIndex: Int
  def currentSong: Song = songs(currentIndex)

  def size: Int = songs.size
  def isEmpty: Boolean = size == 0

  def isLastSong: Boolean = currentIndex == size - 1
  def isFirstSong: Boolean = currentIndex == 0
}
