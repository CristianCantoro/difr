case class GitDiff(cmd: String, op: FileOperation, details: Option[GitDiffDetails])

case class GitDiffDetails(oldFile: String, newFile: String, chunks: List[ChangeChunk])
  def gitDiff: Parser[GitDiff] = diffHeader ~ fileOperation ~ (gitDiffDetails | gitDiffDetailsMissing) ^^ {
    case files ~ op ~ details => GitDiff(files, op, details)
  }

  def gitDiffDetails: Parser[Option[GitDiffDetails]] = oldFile ~ newFile ~ diffChunks ^^ {
    case of ~ nf ~ chunks => Some(GitDiffDetails(of, nf, chunks))
  def gitDiffDetailsMissing: Parser[Option[GitDiffDetails]] =
    """Binary[^\n]*\n""".r ^^ {
      case _ => None
    }
