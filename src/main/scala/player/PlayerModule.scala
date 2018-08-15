package player

import com.google.inject.Singleton
import net.codingwell.scalaguice.ScalaModule

object PlayerModule extends ScalaModule {
  override def configure(): Unit = {
    bind[UpdatablePlaylist].toInstance(UpdatablePlaylist.empty)
    bind[AudioPlayer].to[StreamPlayerWrapper].in[Singleton]
    bind[MutablePlayer].to[MutablePlayerImpl].in[Singleton]

    requireBinding(classOf[SongFetcher])
  }
}
