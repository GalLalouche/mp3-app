package modules

import com.google.inject.Singleton
import comm.{Communicator, InternetTalker, PosterComm, RandomSong, ScalaJTalker, ServerAddress}
import net.codingwell.scalaguice.ScalaModule
import player.playlist.Playlist
import player.{Player, StreamPlayerWrapper}

object GuiceModule extends ScalaModule {
  override def configure(): Unit = {
    bind[InternetTalker] toInstance ScalaJTalker
    bind[String].annotatedWith[ServerAddress] toInstance "http://localhost:9000/"
    bind[Communicator].to[Communicator.From].in[Singleton]
    bind[RandomSong].to[RandomSong.From].in[Singleton]
    bind[PosterComm].to[PosterComm.From].in[Singleton]
    bind[Player].to[StreamPlayerWrapper].in[Singleton]
    bind[Playlist].to[Playlist.From].in[Singleton]
  }
}
