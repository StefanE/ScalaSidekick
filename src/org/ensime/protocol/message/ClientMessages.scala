package org.ensime.protocol.message

import org.ensime.util.WireFormat

case class GetConnectionInfo(msgID: Int)
        extends WireFormat with MessageID {
  def toWireString = toString()
}

case class InitProject(srvRoot: String, projectPackage: String, sbt: String, rootDir: String, msgID:Int )
        extends WireFormat with MessageID {
  def toWireString = toString()
}

case class TypeAtPoint(filePath: String, offset: Int,msgID: Int)
        extends WireFormat with MessageID {
  def toWireString = toString()
}

case class InspectTypeAtPoint(filePath: String, offset: Int,msgID: Int)
        extends WireFormat with MessageID{
  def toWireString = toString()
}

case class TypeCompletion(filePath: String, offset: Int, word:String, msgID: Int)
        extends WireFormat with MessageID{
  def toWireString = toString()
}

case class ScopeCompletion(filePath: String, offset: Int, word:String, msgID: Int)
        extends WireFormat with MessageID{
  def toWireString = toString()
}

case class OrganizeImports(filePath: String, msgID:Int, start:Int, end:Int) extends WireFormat with MessageID{
  def toWireString = toString()
}

case class ExecRefactoring()

