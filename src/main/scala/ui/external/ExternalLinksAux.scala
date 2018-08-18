package ui.external

import java.awt.{Desktop, Font}
import java.net.URI

import comm.external.{ExternalLink, SingleEntityExternalLinks}
import common.rich.RichT._
import javax.swing.event.HyperlinkEvent
import javax.swing.text.html.HTMLDocument

import scala.swing.{BoxPanel, Dimension, EditorPane, Font, Label, Orientation}

// For artist/album
private class ExternalLinksAux extends BoxPanel(Orientation.Vertical) {
  private def link(name: String, href: String): String = s"<a href=$href>$name</a>"
  private def editorPane(l: ExternalLink): EditorPane = new EditorPane() {
    maximumSize = new Dimension(300, 30)
    contentType = "text/html"
    peer.setEditable(false)
    peer.setOpaque(false)
    val mainLink = link(l.host, l.main)
    val extensions = s" (${l.extensions.map((link _).tupled).mkString(", ")})".onlyIf(l.extensions.nonEmpty)
    text = s"<html><img src='${getClass.getResource(l.host + "_icon.png")}'>&ensp$mainLink$extensions</html>"
    peer.addHyperlinkListener(hle =>
      if (hle.getEventType == HyperlinkEvent.EventType.ACTIVATED)
        Desktop.getDesktop.browse(hle.getURL.toURI)
    )

    peer.getDocument.asInstanceOf[HTMLDocument].getStyleSheet
        .addRule(s"body { font-family: Arial, font-size: 15pt; }")
  }

  preferredSize = new Dimension(500, 500)

  def update(links: SingleEntityExternalLinks): Unit = {
    _contents.clear()
    _contents += new Label(s"${links.entityType} links (${links.timestamp.getDayOfMonth}/${links.timestamp.getMonthValue})") {
      font = new Font("SansSerif", Font.BOLD, 16)
    }
    contents ++= links.links.map(editorPane)
  }
}
