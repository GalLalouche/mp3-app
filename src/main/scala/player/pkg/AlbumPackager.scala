package player.pkg

import java.io.File

trait AlbumPackager {
  def packageFromZip(d: File): PackagedAlbum
}
