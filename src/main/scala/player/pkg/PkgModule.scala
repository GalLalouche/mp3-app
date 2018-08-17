package player.pkg

import net.codingwell.scalaguice.ScalaModule

object PkgModule extends ScalaModule {
  override def configure(): Unit = {
    bind[AlbumPackager].to[AlbumPackagerImpl]
  }
}
