case class GitDiff(cmd: String, oldFile: String, newFile: String, op: FileOperation, chunks: List[ChangeChunk])



sealed trait LineChange {
  def line: String
}





/**
 * Git diff parser, originally based on some code found on github, now butchered beyond recognition. (Solving a couple
 * of bugs, including the "No newline at end of file" input, the way it deals with filenames, restrictions on number of
 * characters for hashes, etc.)
 */
  def gitDiff: Parser[GitDiff] = diffHeader ~ fileOperation ~ oldFile ~ newFile ~ diffChunks <~
    opt("\\ No newline at end of file" ~ newline) ^^ {
    case files ~ op ~ of ~ nf ~ chunks => GitDiff(files, of, nf, op, chunks)
  def diffHeader: Parser[String] =
    """diff --git[^\n]*""".r <~ newline
    opt(deletedFileMode | newFileMode) <~ index ^^ {
      _ getOrElse UpdatedFile
    }

  def index: Parser[Any] = ("index " ~ hash ~ ".." ~ hash) ~> opt(" " ~> mode) <~ newline

  def deletedFileMode: Parser[DeletedFile] = "deleted file mode " ~> mode <~ newline ^^ {
    m => DeletedFile(m)
  }

  def newFileMode: Parser[NewFile] = "new file mode " ~> mode <~ newline ^^ {
    m => NewFile(m)
  }
  def mode: Parser[Int] = """\d{6}""".r ^^ {
    _.toInt
  }

  def diffChunks: Parser[List[ChangeChunk]] = rep1(changeChunk)

  def oldFile: Parser[String] = "--- " ~> """[^\n]*""".r <~ newline ^^ {
    s => s.dropWhile(_ != '/').drop(1)
  }

  def newFile: Parser[String] = "+++ " ~> """[^\n]*""".r <~ newline ^^ {
    s => s.dropWhile(_ != '/').drop(1)
  }


  def contextLine: Parser[ContextLine] = " " ~> """.*""".r <~ newline ^^ {
    l => ContextLine(l)
  }

  def addedLine: Parser[LineAdded] = "+" ~> """.*""".r <~ newline ^^ {
    l => LineAdded(l)
  }

  def deletedLine: Parser[LineRemoved] = "-" ~> """.*""".r <~ newline ^^ {
    l => LineRemoved(l)
  }

  def number: Parser[Int] = """\d+""".r ^^ {
    _.toInt
  }
    case NoSuccess(msg, next) => sys.error(msg + " at " + next.pos)