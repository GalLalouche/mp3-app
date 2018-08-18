package comm

import com.google.inject.Singleton
import net.codingwell.scalaguice.ScalaModule
import player.SongFetcher

object CommModule extends ScalaModule {
  override def configure(): Unit = {
    bind[InternetTalker] toInstance ScalaJTalker
    bind[String].annotatedWith[ServerAddress] toInstance "http://localhost:9000/"
    bind[Communicator].to[Communicator.From]
    bind[SongFetcher].to[RandomSong.From]
    bind[PosterComm].to[PosterComm.From]
  }
}
