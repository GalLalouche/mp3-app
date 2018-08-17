package player

import com.google.inject.{Provides, Singleton}
import net.codingwell.scalaguice.ScalaModule
import rx.lang.scala.Observable

object PlayerModule extends ScalaModule {
  override def configure(): Unit = {
    bind[UpdatablePlaylist].toInstance(UpdatablePlaylist.empty)
    bind[AudioPlayer].to[StreamPlayerWrapper].in[Singleton]
    bind[MutablePlayer].to[MutablePlayerImpl].in[Singleton]

    requireBinding(classOf[SongFetcher])
  }

  @Provides
  def providePlayerEventObservable(mp: MutablePlayer): Observable[PlayerEvent] = mp.events
}
