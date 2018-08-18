package comm

sealed trait Lyrics
case class WordLyrics(lyricHtml: String) extends Lyrics
case object Instrumental extends Lyrics
