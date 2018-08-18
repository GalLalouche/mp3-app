package comm.external

import java.time.{Month, MonthDay}

import common.AuxSpecs
import org.scalatest.FreeSpec
import spray.json._

class ExternalLinksTest extends FreeSpec with AuxSpecs {
  "readJson" in {
    val json =
      """
      {
        "Artist links": {
          "MusicBrainz": {
          "host": "MusicBrainz",
          "main": "https://musicbrainz.org/artist/72c536dc-7137-4477-a521-567eeb840fa8",
          "extensions": {
          "edit": "https://musicbrainz.org/artist/72c536dc-7137-4477-a521-567eeb840fa8/edit?edit-artist.url.0.text=https://en.wikipedia.org/wiki/Bob_Dylan&edit-artist.url.0.link_type_id=179",
          "Google": "http://www.google.com/search?q=bob dylan MusicBrainz"
        }
        },
          "Wikipedia": {
          "host": "Wikipedia",
          "main": "https://en.wikipedia.org/wiki/Bob_Dylan",
          "extensions": {}
        },
          "timestamp": "18/08"
        },
        "Album links": {
          "MusicBrainz": {
          "host": "MusicBrainz",
          "main": "https://musicbrainz.org/release-group/32981714-597a-32dd-9f29-b2f7566fed89",
          "extensions": {
          "edit": "https://musicbrainz.org/release-group/32981714-597a-32dd-9f29-b2f7566fed89/edit",
          "Google": "http://www.google.com/search?q=bob dylan - john wesley harding MusicBrainz"
        }
        },
          "Wikipedia": {
          "host": "Wikipedia",
          "main": "https://en.wikipedia.org/wiki/John_Wesley_Harding_(album)",
          "extensions": {}
        },
          "timestamp": "08/08"
        }
      }
  """.parseJson

    val $ = ExternalLinks.JsonReaderEv.read(json)

    $.artistLinks.timestamp shouldReturn MonthDay.of(Month.AUGUST, 18)
    $.albumLinks.timestamp shouldReturn MonthDay.of(Month.AUGUST, 8)

    $.artistLinks.links shouldSetEqual Seq(
      ExternalLink(
        host = "MusicBrainz",
        main = "https://musicbrainz.org/artist/72c536dc-7137-4477-a521-567eeb840fa8",
        extensions = Map(
          "edit" -> "https://musicbrainz.org/artist/72c536dc-7137-4477-a521-567eeb840fa8/edit?edit-artist.url.0.text=https://en.wikipedia.org/wiki/Bob_Dylan&edit-artist.url.0.link_type_id=179",
          "Google" -> "http://www.google.com/search?q=bob dylan MusicBrainz",
        ),
      ),
      ExternalLink(
        host = "Wikipedia",
        main = "https://en.wikipedia.org/wiki/Bob_Dylan",
        extensions = Map(),
      ),
    )

    $.albumLinks.links shouldSetEqual Seq(
      ExternalLink(
        host = "MusicBrainz",
        main = "https://musicbrainz.org/release-group/32981714-597a-32dd-9f29-b2f7566fed89",
        extensions = Map(
          "edit" -> "https://musicbrainz.org/release-group/32981714-597a-32dd-9f29-b2f7566fed89/edit",
          "Google" -> "http://www.google.com/search?q=bob dylan - john wesley harding MusicBrainz",
        ),
      ),
      ExternalLink(
        host = "Wikipedia",
        main = "https://en.wikipedia.org/wiki/John_Wesley_Harding_(album)",
        extensions = Map(),
      ),
    )
  }
}
