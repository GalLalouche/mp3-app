package modules

import comm.CommModule
import net.codingwell.scalaguice.ScalaModule
import player.PlayerModule
import player.pkg.PkgModule

object GuiceModule extends ScalaModule {
  override def configure(): Unit = {
    install(CommModule)
    install(PlayerModule)
    install(PkgModule)
  }
}
