package comm

import comm.external.ExternalLinksComm
import net.codingwell.scalaguice.ScalaModule
import player.SongFetcher

object CommModule extends ScalaModule {
  override def configure(): Unit = {
    bind[InternetTalker] toInstance ScalaJTalker
    bind[String].annotatedWith[ServerAddress] toInstance "http://5.29.46.8:9000/"
    bind[Communicator].to[Communicator.From]
    bind[SongFetcher].to[RandomSong.From]
    bind[PosterComm].to[PosterComm.From]
    bind[ExternalLinksComm].to[ExternalLinksComm.From]
    bind[LyricsComm].to[LyricsComm.From]
  }
}
